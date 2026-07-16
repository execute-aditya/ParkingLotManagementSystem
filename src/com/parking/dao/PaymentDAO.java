package com.parking.dao;

import com.parking.model.Payment;
import com.parking.model.Payment.PaymentMode;
import com.parking.model.Payment.PaymentStatus;
import com.parking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Payment table — insert and read payments.
 */
public class PaymentDAO {

    // ── CREATE ──────────────────────────────────────────────────

    public int insertPayment(Payment p) throws SQLException {
        String sql = "INSERT INTO payment (booking_id, vehicle_id, base_amount, surge_amount, " +
                     "discount_amount, total_amount, payment_mode, payment_status) VALUES (?,?,?,?,?,?,?,?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getBookingId());
            ps.setInt(2, p.getVehicleId());
            ps.setDouble(3, p.getBaseAmount());
            ps.setDouble(4, p.getSurgeAmount());
            ps.setDouble(5, p.getDiscountAmount());
            ps.setDouble(6, p.getTotalAmount());
            ps.setString(7, p.getPaymentMode().name());
            ps.setString(8, p.getPaymentStatus().name());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    // ── READ ────────────────────────────────────────────────────

    public Payment findByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT * FROM payment WHERE booking_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT * FROM payment ORDER BY paid_at DESC LIMIT 100";
        List<Payment> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── REVENUE ─────────────────────────────────────────────────

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM payment WHERE payment_status='PAID'";
        Connection conn = DBConnection.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    public double getTodayRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount),0) FROM payment " +
                     "WHERE payment_status='PAID' AND DATE(paid_at)=CURDATE()";
        Connection conn = DBConnection.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    // ── UPDATE STATUS ───────────────────────────────────────────

    public void markPaid(int paymentId) throws SQLException {
        String sql = "UPDATE payment SET payment_status='PAID' WHERE payment_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            ps.executeUpdate();
        }
    }

    // ── HELPER ──────────────────────────────────────────────────

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setBookingId(rs.getInt("booking_id"));
        p.setVehicleId(rs.getInt("vehicle_id"));
        p.setBaseAmount(rs.getDouble("base_amount"));
        p.setSurgeAmount(rs.getDouble("surge_amount"));
        p.setDiscountAmount(rs.getDouble("discount_amount"));
        p.setTotalAmount(rs.getDouble("total_amount"));
        p.setPaymentMode(PaymentMode.valueOf(rs.getString("payment_mode")));
        p.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        Timestamp ts = rs.getTimestamp("paid_at");
        if (ts != null) p.setPaidAt(ts.toLocalDateTime());
        return p;
    }
}
