package com.parking.dao;

import com.parking.model.Booking;
import com.parking.model.Booking.BookingStatus;
import com.parking.model.Booking.FareType;
import com.parking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Booking table — create, find, complete, cancel.
 */
public class BookingDAO {

    // ── CREATE ──────────────────────────────────────────────────

    public int insertBooking(Booking b) throws SQLException {
        String sql = "INSERT INTO booking (vehicle_id, spot_id, lot_id, fare_type, status) VALUES (?,?,?,?,?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getVehicleId());
            ps.setInt(2, b.getSpotId());
            ps.setInt(3, b.getLotId());
            ps.setString(4, b.getFareType().name());
            ps.setString(5, b.getStatus().name());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    // ── READ ────────────────────────────────────────────────────

    public Booking findById(int bookingId) throws SQLException {
        String sql = "SELECT b.*, v.license_plate, s.spot_number " +
                     "FROM booking b " +
                     "JOIN vehicle v ON b.vehicle_id = v.vehicle_id " +
                     "JOIN spot    s ON b.spot_id    = s.spot_id " +
                     "WHERE b.booking_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public Booking findActiveByVehicle(int vehicleId) throws SQLException {
        String sql = "SELECT b.*, v.license_plate, s.spot_number " +
                     "FROM booking b " +
                     "JOIN vehicle v ON b.vehicle_id = v.vehicle_id " +
                     "JOIN spot    s ON b.spot_id    = s.spot_id " +
                     "WHERE b.vehicle_id=? AND b.status='ACTIVE' LIMIT 1";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Booking> findAllActive() throws SQLException {
        String sql = "SELECT b.*, v.license_plate, s.spot_number " +
                     "FROM booking b " +
                     "JOIN vehicle v ON b.vehicle_id = v.vehicle_id " +
                     "JOIN spot    s ON b.spot_id    = s.spot_id " +
                     "WHERE b.status='ACTIVE' ORDER BY b.created_at";
        List<Booking> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Booking> findAll() throws SQLException {
        String sql = "SELECT b.*, v.license_plate, s.spot_number " +
                     "FROM booking b " +
                     "JOIN vehicle v ON b.vehicle_id = v.vehicle_id " +
                     "JOIN spot    s ON b.spot_id    = s.spot_id " +
                     "ORDER BY b.created_at DESC LIMIT 50";
        List<Booking> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── UPDATE STATUS ───────────────────────────────────────────

    public void updateStatus(int bookingId, BookingStatus status) throws SQLException {
        String sql = "UPDATE booking SET status=? WHERE booking_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    // ── HELPER ──────────────────────────────────────────────────

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setVehicleId(rs.getInt("vehicle_id"));
        b.setSpotId(rs.getInt("spot_id"));
        b.setLotId(rs.getInt("lot_id"));
        b.setFareType(FareType.valueOf(rs.getString("fare_type")));
        b.setStatus(BookingStatus.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(ts.toLocalDateTime());
        // transient
        try { b.setLicensePlate(rs.getString("license_plate")); } catch (Exception ignored) {}
        try { b.setSpotNumber(rs.getString("spot_number")); }     catch (Exception ignored) {}
        return b;
    }
}
