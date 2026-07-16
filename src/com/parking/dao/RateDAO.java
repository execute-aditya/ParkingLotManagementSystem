package com.parking.dao;

import com.parking.model.Rate;
import com.parking.model.Rate.FareType;
import com.parking.model.Rate.VehicleType;
import com.parking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Rate table — read and update pricing.
 */
public class RateDAO {

    // ── READ ────────────────────────────────────────────────────

    public Rate findRate(int lotId, String vehicleType, String fareType) throws SQLException {
        String sql = "SELECT * FROM rate WHERE lot_id=? AND vehicle_type=? AND fare_type=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lotId);
            ps.setString(2, vehicleType);
            ps.setString(3, fareType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Rate> findAllByLot(int lotId) throws SQLException {
        String sql = "SELECT * FROM rate WHERE lot_id=? ORDER BY vehicle_type, fare_type";
        List<Rate> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lotId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── UPDATE SURGE MULTIPLIER ─────────────────────────────────

    public void updateSurgeMultiplier(int rateId, double surge) throws SQLException {
        String sql = "UPDATE rate SET surge_multiplier=? WHERE rate_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, surge);
            ps.setInt(2, rateId);
            ps.executeUpdate();
        }
    }

    // ── UPDATE BASE RATE ────────────────────────────────────────

    public void updateBaseRate(int rateId, double newRate) throws SQLException {
        String sql = "UPDATE rate SET base_rate=? WHERE rate_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newRate);
            ps.setInt(2, rateId);
            ps.executeUpdate();
        }
    }

    // ── HELPER ──────────────────────────────────────────────────

    private Rate mapRow(ResultSet rs) throws SQLException {
        Rate r = new Rate();
        r.setRateId(rs.getInt("rate_id"));
        r.setLotId(rs.getInt("lot_id"));
        r.setVehicleType(VehicleType.valueOf(rs.getString("vehicle_type")));
        r.setFareType(FareType.valueOf(rs.getString("fare_type")));
        r.setBaseRate(rs.getDouble("base_rate"));
        r.setGracePeriodMin(rs.getInt("grace_period_min"));
        r.setSurgeMultiplier(rs.getDouble("surge_multiplier"));
        return r;
    }
}
