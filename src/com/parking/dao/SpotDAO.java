package com.parking.dao;

import com.parking.model.Spot;
import com.parking.model.Spot.SpotType;
import com.parking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Spot table — find, update occupancy.
 */
public class SpotDAO {

    // ── FIND NEAREST AVAILABLE SPOT ─────────────────────────────
    // Returns the spot with smallest floor number (nearest to entrance) for the given type

    public Spot findNearestAvailableSpot(int lotId, String spotType) throws SQLException {
        String sql = "SELECT * FROM spot WHERE lot_id=? AND spot_type=? AND is_occupied=0 " +
                     "ORDER BY floor ASC, spot_number ASC LIMIT 1";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lotId);
            ps.setString(2, spotType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public Spot findById(int spotId) throws SQLException {
        String sql = "SELECT * FROM spot WHERE spot_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, spotId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Spot> findAllByLot(int lotId) throws SQLException {
        String sql = "SELECT * FROM spot WHERE lot_id=? ORDER BY spot_type, floor, spot_number";
        List<Spot> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lotId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── OCCUPANCY STATS ─────────────────────────────────────────

    public int countOccupied(int lotId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM spot WHERE lot_id=? AND is_occupied=1";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lotId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countTotal(int lotId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM spot WHERE lot_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lotId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── UPDATE OCCUPANCY ────────────────────────────────────────

    public void setOccupied(int spotId, boolean occupied) throws SQLException {
        String sql = "UPDATE spot SET is_occupied=? WHERE spot_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, occupied);
            ps.setInt(2, spotId);
            ps.executeUpdate();
        }
    }

    // ── SURGE: % occupancy ──────────────────────────────────────

    public double getOccupancyPercent(int lotId) throws SQLException {
        int total    = countTotal(lotId);
        int occupied = countOccupied(lotId);
        if (total == 0) return 0.0;
        return (occupied * 100.0) / total;
    }

    // ── HELPER ──────────────────────────────────────────────────

    private Spot mapRow(ResultSet rs) throws SQLException {
        Spot spot = new Spot();
        spot.setSpotId(rs.getInt("spot_id"));
        spot.setLotId(rs.getInt("lot_id"));
        spot.setSpotNumber(rs.getString("spot_number"));
        spot.setSpotType(SpotType.valueOf(rs.getString("spot_type")));
        spot.setFloor(rs.getInt("floor"));
        spot.setOccupied(rs.getBoolean("is_occupied"));
        return spot;
    }
}
