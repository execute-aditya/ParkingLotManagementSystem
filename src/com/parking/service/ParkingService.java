package com.parking.service;

import com.parking.dao.*;
import com.parking.model.*;
import com.parking.model.Booking.BookingStatus;
import com.parking.model.Booking.FareType;
import com.parking.model.Payment.PaymentMode;
import com.parking.model.Payment.PaymentStatus;
import com.parking.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Core service handling all business logic:
 *  - Vehicle entry (find spot, create booking, log entry)
 *  - Vehicle exit (calculate fare, process payment, release spot)
 *  - Monthly pass management
 *  - Dynamic surge pricing
 */
public class ParkingService {

    private static final int DEFAULT_LOT_ID = 1;

    // Surge thresholds
    private static final double SURGE_THRESHOLD_1 = 70.0;  // ≥70% → 1.25×
    private static final double SURGE_THRESHOLD_2 = 85.0;  // ≥85% → 1.50×
    private static final double SURGE_THRESHOLD_3 = 95.0;  // ≥95% → 2.00×

    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final SpotDAO    spotDAO    = new SpotDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final RateDAO    rateDAO    = new RateDAO();

    // ────────────────────────────────────────────────────────────
    //  VEHICLE ENTRY
    // ────────────────────────────────────────────────────────────

    /**
     * Handles vehicle entry:
     *  1. Register vehicle if not already registered.
     *  2. Find nearest available spot.
     *  3. Create booking and entry/exit log entry.
     *  4. Mark spot as occupied.
     *
     * @return Booking created, or null if no spot available.
     */
    public Booking vehicleEntry(String licensePlate, String vehicleTypeStr,
                                String ownerName, String ownerPhone,
                                String fareTypeStr) throws SQLException {

        // 1. Get or register vehicle
        Vehicle vehicle = vehicleDAO.findByLicensePlate(licensePlate);
        if (vehicle == null) {
            vehicle = createVehicleByType(vehicleTypeStr, licensePlate, ownerName, ownerPhone);
            int id = vehicleDAO.insertVehicle(vehicle);
            vehicle.setVehicleId(id);
            System.out.println("  [+] New vehicle registered. ID: " + id);
        } else {
            System.out.println("  [i] Existing vehicle found. ID: " + vehicle.getVehicleId());
        }

        // 2. Check if already parked
        Booking existing = bookingDAO.findActiveByVehicle(vehicle.getVehicleId());
        if (existing != null) {
            System.out.println("  [!] Vehicle already parked in spot " + existing.getSpotNumber() + " (Booking #" + existing.getBookingId() + ")");
            return existing;
        }

        // 3. Find nearest spot
        String requiredSpotType = vehicle.getRequiredSpotType();
        Spot spot = spotDAO.findNearestAvailableSpot(DEFAULT_LOT_ID, requiredSpotType);
        if (spot == null) {
            System.out.println("  [!] No available " + requiredSpotType + " spots!");
            return null;
        }

        // 4. Create booking
        FareType fareType = FareType.valueOf(fareTypeStr.toUpperCase());
        Booking booking = new Booking(vehicle.getVehicleId(), spot.getSpotId(), DEFAULT_LOT_ID, fareType);
        int bookingId = bookingDAO.insertBooking(booking);
        booking.setBookingId(bookingId);
        booking.setSpotNumber(spot.getSpotNumber());

        // 5. Log entry
        logEntry(bookingId, vehicle.getVehicleId(), spot.getSpotId());

        // 6. Mark spot occupied
        spotDAO.setOccupied(spot.getSpotId(), true);

        // 7. Auto-update surge pricing
        updateSurgePricing();

        return booking;
    }

    // ────────────────────────────────────────────────────────────
    //  VEHICLE EXIT
    // ────────────────────────────────────────────────────────────

    /**
     * Handles vehicle exit:
     *  1. Find active booking.
     *  2. Calculate duration and fare.
     *  3. Record payment.
     *  4. Release spot.
     *  5. Complete booking and log exit.
     *
     * @return Payment object with fare details.
     */
    public Payment vehicleExit(String licensePlate, String paymentModeStr) throws SQLException {
        // Find vehicle
        Vehicle vehicle = vehicleDAO.findByLicensePlate(licensePlate);
        if (vehicle == null) {
            System.out.println("  [!] Vehicle not found: " + licensePlate);
            return null;
        }

        // Find active booking
        Booking booking = bookingDAO.findActiveByVehicle(vehicle.getVehicleId());
        if (booking == null) {
            System.out.println("  [!] No active parking session for " + licensePlate);
            return null;
        }

        // Get entry log
        EntryExitRecord entryRecord = getEntryRecord(booking.getBookingId());
        LocalDateTime entryTime  = (entryRecord != null) ? entryRecord.entryTime  : booking.getCreatedAt();
        LocalDateTime exitTime   = LocalDateTime.now();
        long durationMinutes     = ChronoUnit.MINUTES.between(entryTime, exitTime);

        // Get rate and compute fare
        Rate rate = rateDAO.findRate(DEFAULT_LOT_ID, vehicle.getVehicleType().name(), booking.getFareType().name());
        double surgeMultiplier = getCurrentSurgeMultiplier(DEFAULT_LOT_ID);

        FareCalculator calculator = FareCalculator.of(rate);
        double total = calculator.calculateFare(durationMinutes, surgeMultiplier, vehicle.isMonthlyPass());

        System.out.println("\n" + calculator.getFareBreakdown(durationMinutes, surgeMultiplier, vehicle.isMonthlyPass()));

        // Build payment
        double base     = calculator.calculateFare(durationMinutes, 1.0, false);
        double surge    = total - base;
        double discount = vehicle.isMonthlyPass() ? base : 0.0;

        PaymentMode mode = PaymentMode.valueOf(paymentModeStr.toUpperCase());
        Payment payment = new Payment(booking.getBookingId(), vehicle.getVehicleId(),
                                      base, Math.max(0, surge), discount, total, mode);
        payment.setPaymentStatus(PaymentStatus.PAID);
        int paymentId = paymentDAO.insertPayment(payment);
        payment.setPaymentId(paymentId);

        // Log exit
        updateExitLog(booking.getBookingId(), exitTime, (int) durationMinutes);

        // Release spot
        spotDAO.setOccupied(booking.getSpotId(), false);

        // Complete booking
        bookingDAO.updateStatus(booking.getBookingId(), BookingStatus.COMPLETED);

        // Update surge after release
        updateSurgePricing();

        return payment;
    }

    // ────────────────────────────────────────────────────────────
    //  MONTHLY PASS MANAGEMENT
    // ────────────────────────────────────────────────────────────

    public void purchaseMonthlyPass(String licensePlate) throws SQLException {
        Vehicle vehicle = vehicleDAO.findByLicensePlate(licensePlate);
        if (vehicle == null) {
            System.out.println("  [!] Vehicle not registered: " + licensePlate);
            return;
        }

        Rate rate = rateDAO.findRate(DEFAULT_LOT_ID, vehicle.getVehicleType().name(), "MONTHLY");
        if (rate == null) {
            System.out.println("  [!] Monthly rate not configured for this vehicle type.");
            return;
        }

        java.time.LocalDate expiry = java.time.LocalDate.now().plusMonths(1);
        vehicleDAO.updateMonthlyPass(vehicle.getVehicleId(), true, expiry);

        System.out.printf("  [✔] Monthly pass activated for %s%n", licensePlate);
        System.out.printf("      Valid until : %s%n", expiry);
        System.out.printf("      Pass fee    : ₹%.2f (pay at counter)%n", rate.getBaseRate());
    }

    public void cancelMonthlyPass(String licensePlate) throws SQLException {
        Vehicle vehicle = vehicleDAO.findByLicensePlate(licensePlate);
        if (vehicle == null) { System.out.println("  [!] Vehicle not found."); return; }
        vehicleDAO.updateMonthlyPass(vehicle.getVehicleId(), false, null);
        System.out.println("  [✔] Monthly pass cancelled for " + licensePlate);
    }

    // ────────────────────────────────────────────────────────────
    //  DYNAMIC / SURGE PRICING
    // ────────────────────────────────────────────────────────────

    /**
     * Recomputes surge multipliers for ALL rates in a lot based on current occupancy.
     */
    public void updateSurgePricing() throws SQLException {
        double occupancy = spotDAO.getOccupancyPercent(DEFAULT_LOT_ID);
        double surge;
        if      (occupancy >= SURGE_THRESHOLD_3) surge = 2.00;
        else if (occupancy >= SURGE_THRESHOLD_2) surge = 1.50;
        else if (occupancy >= SURGE_THRESHOLD_1) surge = 1.25;
        else                                     surge = 1.00;

        List<Rate> rates = rateDAO.findAllByLot(DEFAULT_LOT_ID);
        for (Rate r : rates) {
            rateDAO.updateSurgeMultiplier(r.getRateId(), surge);
        }

        if (surge > 1.0) {
            System.out.printf("  [SURGE] Occupancy %.1f%% → Surge pricing active: ×%.2f%n", occupancy, surge);
        }
    }

    public double getCurrentSurgeMultiplier(int lotId) throws SQLException {
        List<Rate> rates = rateDAO.findAllByLot(lotId);
        if (!rates.isEmpty()) return rates.get(0).getSurgeMultiplier();
        return 1.0;
    }

    // ────────────────────────────────────────────────────────────
    //  ENTRY / EXIT LOG HELPERS  (raw JDBC — no separate DAO needed)
    // ────────────────────────────────────────────────────────────

    private void logEntry(int bookingId, int vehicleId, int spotId) throws SQLException {
        String sql = "INSERT INTO entry_exit_log (booking_id, vehicle_id, spot_id, entry_time) VALUES (?,?,?,?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, vehicleId);
            ps.setInt(3, spotId);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    private void updateExitLog(int bookingId, LocalDateTime exitTime, int durationMin) throws SQLException {
        String sql = "UPDATE entry_exit_log SET exit_time=?, duration_minutes=? WHERE booking_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(exitTime));
            ps.setInt(2, durationMin);
            ps.setInt(3, bookingId);
            ps.executeUpdate();
        }
    }

    private EntryExitRecord getEntryRecord(int bookingId) throws SQLException {
        String sql = "SELECT entry_time FROM entry_exit_log WHERE booking_id=? LIMIT 1";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                EntryExitRecord r = new EntryExitRecord();
                Timestamp ts = rs.getTimestamp("entry_time");
                if (ts != null) r.entryTime = ts.toLocalDateTime();
                return r;
            }
        }
        return null;
    }

    // ── Tiny inner record ────────────────────────────────────────
    private static class EntryExitRecord {
        LocalDateTime entryTime;
    }

    // ────────────────────────────────────────────────────────────
    //  FACTORY: Create Vehicle by type string
    // ────────────────────────────────────────────────────────────

    public static Vehicle createVehicleByType(String typeStr, String plate, String owner, String phone) {
        switch (typeStr.toUpperCase()) {
            case "BIKE":  return new Bike(plate, owner, phone);
            case "CAR":   return new Car(plate, owner, phone);
            case "VAN":   return new Van(plate, owner, phone);
            case "TRUCK": return new Truck(plate, owner, phone);
            default:      return new Car(plate, owner, phone);
        }
    }
}
