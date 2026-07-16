package com.parking.main;

import com.parking.dao.*;
import com.parking.model.*;
import com.parking.service.ParkingService;
import com.parking.service.RevenueService;
import com.parking.util.ConsoleHelper;
import com.parking.util.DBConnection;

import java.sql.SQLException;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════
 *   PARKING LOT MANAGEMENT SYSTEM — Terminal Application
 *   Author  : Student Project
 *   Version : 1.0
 * ════════════════════════════════════════════════════════
 *
 * OOP Concepts Demonstrated:
 *   • Inheritance   → Car / Bike / Van / Truck extend Vehicle
 *   • Polymorphism  → FareCalculator.of() returns Hourly/Daily/Monthly
 *   • Encapsulation → all model fields are private with getters/setters
 *   • Abstraction   → abstract Vehicle.getRequiredSpotType()
 *   • DAO Pattern   → VehicleDAO, SpotDAO, BookingDAO, PaymentDAO, RateDAO
 */
public class Main {

    private static final ParkingService parkingService = new ParkingService();
    private static final RevenueService revenueService = new RevenueService();
    private static final SpotDAO        spotDAO        = new SpotDAO();
    private static final BookingDAO     bookingDAO     = new BookingDAO();
    private static final VehicleDAO     vehicleDAO     = new VehicleDAO();
    private static final RateDAO        rateDAO        = new RateDAO();

    public static void main(String[] args) {

        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════╗");
        System.out.println("  ║        🅿   PARKING LOT MANAGEMENT SYSTEM   🅿           ║");
        System.out.println("  ║           City Centre Parking — Bangalore               ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════╝");
        System.out.println();

        // Test DB connection
        try {
            DBConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("[FATAL] Cannot connect to database: " + e.getMessage());
            System.err.println("  → Make sure MySQL is running and schema.sql has been executed.");
            System.err.println("  → Update DB_USER and DB_PASSWORD in DBConnection.java");
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = ConsoleHelper.readInt("  Enter choice: ");
            System.out.println();
            try {
                switch (choice) {
                    case 1:  vehicleEntryFlow();           break;
                    case 2:  vehicleExitFlow();            break;
                    case 3:  checkSpotAvailability();      break;
                    case 4:  viewActiveBookings();         break;
                    case 5:  monthlyPassMenu();            break;
                    case 6:  viewEntryExitLog();           break;
                    case 7:  revenueService.printRevenueReport();
                             revenueService.printOccupancyByType();
                             revenueService.printRecentPayments();  break;
                    case 8:  viewRates();                  break;
                    case 9:  registerVehicleOnly();        break;
                    case 10: viewAllVehicles();            break;
                    case 0:  running = false;
                             System.out.println("  Goodbye! 🅿");
                             DBConnection.closeConnection();        break;
                    default: System.out.println("  [!] Invalid choice. Try again.");
                }
            } catch (SQLException e) {
                ConsoleHelper.printError("Database error: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println();
        }
    }

    // ── MENU ─────────────────────────────────────────────────────

    private static void printMainMenu() {
        ConsoleHelper.printLine();
        System.out.println("  MAIN MENU");
        ConsoleHelper.printLine();
        System.out.println("  1.  Vehicle Entry     (Park a vehicle)");
        System.out.println("  2.  Vehicle Exit      (Checkout & pay)");
        System.out.println("  3.  Check Spot Availability");
        System.out.println("  4.  View Active Bookings");
        System.out.println("  5.  Monthly Pass Management");
        System.out.println("  6.  Entry/Exit Log");
        System.out.println("  7.  Revenue & Occupancy Report");
        System.out.println("  8.  View Rate Card");
        System.out.println("  9.  Register Vehicle");
        System.out.println("  10. View All Vehicles");
        System.out.println("  0.  Exit");
        ConsoleHelper.printLine();
    }

    // ── 1. VEHICLE ENTRY ─────────────────────────────────────────

    private static void vehicleEntryFlow() throws SQLException {
        ConsoleHelper.printHeader("VEHICLE ENTRY");

        String plate  = ConsoleHelper.readString("  License Plate        : ").toUpperCase();
        String type   = pickVehicleType();
        String owner  = ConsoleHelper.readString("  Owner Name           : ");
        String phone  = ConsoleHelper.readString("  Owner Phone          : ");
        String fare   = pickFareType();

        System.out.println();
        Booking booking = parkingService.vehicleEntry(plate, type, owner, phone, fare);
        if (booking != null) {
            System.out.println();
            ConsoleHelper.printSuccess("Vehicle parked successfully!");
            System.out.printf("  Booking ID  : #%d%n",    booking.getBookingId());
            System.out.printf("  Spot        : %s%n",     booking.getSpotNumber());
            System.out.printf("  Fare Type   : %s%n",     booking.getFareType());
            System.out.printf("  Entry Time  : %s%n",     booking.getCreatedAt() != null
                                                             ? booking.getCreatedAt() : "Now");
        } else {
            ConsoleHelper.printError("Could not park vehicle — no available spot!");
        }
    }

    // ── 2. VEHICLE EXIT ──────────────────────────────────────────

    private static void vehicleExitFlow() throws SQLException {
        ConsoleHelper.printHeader("VEHICLE EXIT");

        String plate = ConsoleHelper.readString("  License Plate  : ").toUpperCase();
        String mode  = pickPaymentMode();

        System.out.println();
        System.out.println("  ── FARE BREAKDOWN ──────────────────────");
        Payment payment = parkingService.vehicleExit(plate, mode);

        if (payment != null) {
            System.out.println();
            ConsoleHelper.printSuccess("Checkout complete!");
            System.out.printf("  Payment ID  : #%d%n",       payment.getPaymentId());
            System.out.printf("  Total Paid  : ₹%.2f%n",     payment.getTotalAmount());
            System.out.printf("  Mode        : %s%n",         payment.getPaymentMode());
            System.out.printf("  Status      : %s%n",         payment.getPaymentStatus());
        }
    }

    // ── 3. SPOT AVAILABILITY ─────────────────────────────────────

    private static void checkSpotAvailability() throws SQLException {
        ConsoleHelper.printHeader("SPOT AVAILABILITY");
        int lotId = 1;
        String[] types = {"BIKE", "CAR", "VAN", "TRUCK"};
        System.out.printf("  %-8s %-10s %-10s %-10s%n", "Type", "Available", "Occupied", "Total");
        System.out.println("  " + "─".repeat(40));
        for (String type : types) {
            int total    = countByType(lotId, type);
            int occupied = countOccupiedByType(lotId, type);
            int avail    = total - occupied;
            System.out.printf("  %-8s %-10d %-10d %-10d%n", type, avail, occupied, total);
        }
        System.out.println();

        // Nearest available for a type
        String choice = ConsoleHelper.readString("  Find nearest spot for which type? (BIKE/CAR/VAN/TRUCK/SKIP): ").toUpperCase();
        if (!choice.equals("SKIP") && !choice.isEmpty()) {
            Spot spot = spotDAO.findNearestAvailableSpot(lotId, choice);
            if (spot != null) {
                System.out.printf("  Nearest %-5s spot : %s (Floor %d)%n",
                        choice, spot.getSpotNumber(), spot.getFloor());
            } else {
                System.out.println("  No available " + choice + " spots.");
            }
        }
    }

    // ── 4. ACTIVE BOOKINGS ───────────────────────────────────────

    private static void viewActiveBookings() throws SQLException {
        ConsoleHelper.printHeader("ACTIVE BOOKINGS");
        List<Booking> bookings = bookingDAO.findAllActive();
        if (bookings.isEmpty()) {
            ConsoleHelper.printInfo("No active bookings.");
            return;
        }
        System.out.printf("  %-8s %-15s %-8s %-10s %-20s%n",
                "BookID", "Plate", "Spot", "FareType", "Entry Time");
        System.out.println("  " + "─".repeat(62));
        for (Booking b : bookings) {
            System.out.printf("  %-8d %-15s %-8s %-10s %-20s%n",
                    b.getBookingId(), b.getLicensePlate(), b.getSpotNumber(),
                    b.getFareType(), b.getCreatedAt());
        }
    }

    // ── 5. MONTHLY PASS MENU ─────────────────────────────────────

    private static void monthlyPassMenu() throws SQLException {
        ConsoleHelper.printHeader("MONTHLY PASS MANAGEMENT");
        System.out.println("  1. Purchase Monthly Pass");
        System.out.println("  2. Cancel Monthly Pass");
        System.out.println("  3. Check Pass Status");
        int choice = ConsoleHelper.readInt("  Choice: ");
        String plate = ConsoleHelper.readString("  License Plate: ").toUpperCase();

        switch (choice) {
            case 1:
                parkingService.purchaseMonthlyPass(plate);
                break;
            case 2:
                parkingService.cancelMonthlyPass(plate);
                break;
            case 3:
                Vehicle v = vehicleDAO.findByLicensePlate(plate);
                if (v == null) { ConsoleHelper.printError("Vehicle not found."); break; }
                System.out.printf("  Plate       : %s%n",  v.getLicensePlate());
                System.out.printf("  Monthly Pass: %s%n",  v.isMonthlyPass() ? "ACTIVE" : "NOT ACTIVE");
                if (v.getPassExpiry() != null)
                    System.out.printf("  Expiry      : %s%n",  v.getPassExpiry().toLocalDate());
                break;
            default:
                System.out.println("  [!] Invalid option.");
        }
    }

    // ── 6. ENTRY/EXIT LOG ────────────────────────────────────────

    private static void viewEntryExitLog() throws SQLException {
        ConsoleHelper.printHeader("ENTRY / EXIT LOG (last 20)");
        String sql = "SELECT l.log_id, v.license_plate, s.spot_number, " +
                     "l.entry_time, l.exit_time, l.duration_minutes " +
                     "FROM entry_exit_log l " +
                     "JOIN vehicle v ON l.vehicle_id = v.vehicle_id " +
                     "JOIN spot    s ON l.spot_id    = s.spot_id " +
                     "ORDER BY l.log_id DESC LIMIT 20";
        try (java.sql.Statement st = DBConnection.getConnection().createStatement();
             java.sql.ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("  %-6s %-14s %-8s %-20s %-20s %-8s%n",
                    "LogID","Plate","Spot","Entry","Exit","Dur(m)");
            System.out.println("  " + "─".repeat(78));
            while (rs.next()) {
                String exit = rs.getTimestamp("exit_time") != null
                        ? rs.getTimestamp("exit_time").toString() : "PARKED";
                int dur  = rs.getInt("duration_minutes");
                System.out.printf("  %-6d %-14s %-8s %-20s %-20s %-8s%n",
                        rs.getInt("log_id"),
                        rs.getString("license_plate"),
                        rs.getString("spot_number"),
                        rs.getTimestamp("entry_time"),
                        exit,
                        dur == 0 ? "-" : String.valueOf(dur));
            }
        }
    }

    // ── 8. RATE CARD ─────────────────────────────────────────────

    private static void viewRates() throws SQLException {
        ConsoleHelper.printHeader("RATE CARD (Lot 1)");
        System.out.printf("  %-8s %-10s %-12s %-8s %-8s%n",
                "Type","FareType","Rate(₹)","Grace","Surge");
        System.out.println("  " + "─".repeat(50));
        for (Rate r : rateDAO.findAllByLot(1)) {
            System.out.printf("  %-8s %-10s ₹%-11.2f %-8d ×%-7.2f%n",
                    r.getVehicleType(), r.getFareType(),
                    r.getBaseRate(), r.getGracePeriodMin(), r.getSurgeMultiplier());
        }
    }

    // ── 9. REGISTER VEHICLE ONLY ─────────────────────────────────

    private static void registerVehicleOnly() throws SQLException {
        ConsoleHelper.printHeader("REGISTER VEHICLE");
        String plate = ConsoleHelper.readString("  License Plate : ").toUpperCase();
        if (vehicleDAO.findByLicensePlate(plate) != null) {
            ConsoleHelper.printInfo("Vehicle already registered.");
            return;
        }
        String type  = pickVehicleType();
        String owner = ConsoleHelper.readString("  Owner Name    : ");
        String phone = ConsoleHelper.readString("  Owner Phone   : ");
        Vehicle v = ParkingService.createVehicleByType(type, plate, owner, phone);
        int id = vehicleDAO.insertVehicle(v);
        ConsoleHelper.printSuccess("Vehicle registered with ID #" + id);
    }

    // ── 10. ALL VEHICLES ─────────────────────────────────────────

    private static void viewAllVehicles() throws SQLException {
        ConsoleHelper.printHeader("REGISTERED VEHICLES");
        List<Vehicle> vehicles = vehicleDAO.findAll();
        System.out.printf("  %-5s %-14s %-7s %-20s %-14s %-5s%n",
                "ID","Plate","Type","Owner","Phone","Pass");
        System.out.println("  " + "─".repeat(65));
        for (Vehicle v : vehicles) {
            System.out.printf("  %-5d %-14s %-7s %-20s %-14s %-5s%n",
                    v.getVehicleId(), v.getLicensePlate(), v.getVehicleType(),
                    v.getOwnerName(), v.getOwnerPhone(),
                    v.isMonthlyPass() ? "YES" : "NO");
        }
    }

    // ── PICKERS ──────────────────────────────────────────────────

    private static String pickVehicleType() {
        System.out.println("  Vehicle Type: 1=BIKE  2=CAR  3=VAN  4=TRUCK");
        int c = ConsoleHelper.readInt("  Choice: ");
        switch (c) {
            case 1: return "BIKE";
            case 3: return "VAN";
            case 4: return "TRUCK";
            default:return "CAR";
        }
    }

    private static String pickFareType() {
        System.out.println("  Fare Type : 1=HOURLY  2=DAILY  3=MONTHLY");
        int c = ConsoleHelper.readInt("  Choice: ");
        switch (c) {
            case 2: return "DAILY";
            case 3: return "MONTHLY";
            default:return "HOURLY";
        }
    }

    private static String pickPaymentMode() {
        System.out.println("  Payment Mode: 1=CASH  2=CARD  3=UPI");
        int c = ConsoleHelper.readInt("  Choice: ");
        switch (c) {
            case 2: return "CARD";
            case 3: return "UPI";
            default:return "CASH";
        }
    }

    // ── DB HELPER ────────────────────────────────────────────────

    private static int countByType(int lotId, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM spot WHERE lot_id=? AND spot_type=?";
        try (java.sql.PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, lotId); ps.setString(2, type);
            java.sql.ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static int countOccupiedByType(int lotId, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM spot WHERE lot_id=? AND spot_type=? AND is_occupied=1";
        try (java.sql.PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, lotId); ps.setString(2, type);
            java.sql.ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
