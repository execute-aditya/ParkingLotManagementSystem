package com.parking.dao;

import com.parking.model.Vehicle;
import com.parking.model.Vehicle.VehicleType;
import com.parking.model.Bike;
import com.parking.model.Car;
import com.parking.model.Van;
import com.parking.model.Truck;
import com.parking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Vehicle table — CRUD operations.
 */
public class VehicleDAO {

    // ── CREATE ──────────────────────────────────────────────────

    public int insertVehicle(Vehicle v) throws SQLException {
        String sql = "INSERT INTO vehicle (license_plate, vehicle_type, owner_name, owner_phone, " +
                     "is_monthly_pass, pass_expiry) VALUES (?,?,?,?,?,?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getLicensePlate());
            ps.setString(2, v.getVehicleType().name());
            ps.setString(3, v.getOwnerName());
            ps.setString(4, v.getOwnerPhone());
            ps.setBoolean(5, v.isMonthlyPass());
            if (v.getPassExpiry() != null)
                ps.setDate(6, Date.valueOf(v.getPassExpiry().toLocalDate()));
            else
                ps.setNull(6, Types.DATE);

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    // ── READ ────────────────────────────────────────────────────

    public Vehicle findByLicensePlate(String plate) throws SQLException {
        String sql = "SELECT * FROM vehicle WHERE license_plate = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public Vehicle findById(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vehicle WHERE vehicle_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Vehicle> findAll() throws SQLException {
        String sql = "SELECT * FROM vehicle ORDER BY registered_at DESC";
        List<Vehicle> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        try (Statement st = conn.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── UPDATE ──────────────────────────────────────────────────

    public void updateMonthlyPass(int vehicleId, boolean active, java.time.LocalDate expiry) throws SQLException {
        String sql = "UPDATE vehicle SET is_monthly_pass=?, pass_expiry=? WHERE vehicle_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setDate(2, expiry != null ? Date.valueOf(expiry) : null);
            ps.setInt(3, vehicleId);
            ps.executeUpdate();
        }
    }

    // ── DELETE ──────────────────────────────────────────────────

    public void deleteVehicle(int vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicle WHERE vehicle_id=?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.executeUpdate();
        }
    }

    // ── HELPER ──────────────────────────────────────────────────

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        VehicleType type = VehicleType.valueOf(rs.getString("vehicle_type"));
        Vehicle v;
        switch (type) {
            case BIKE:  v = new Bike();  break;
            case CAR:   v = new Car();   break;
            case VAN:   v = new Van();   break;
            case TRUCK: v = new Truck(); break;
            default:    v = new Car();
        }
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setLicensePlate(rs.getString("license_plate"));
        v.setVehicleType(type);
        v.setOwnerName(rs.getString("owner_name"));
        v.setOwnerPhone(rs.getString("owner_phone"));
        v.setMonthlyPass(rs.getBoolean("is_monthly_pass"));
        Date expiry = rs.getDate("pass_expiry");
        if (expiry != null) v.setPassExpiry(expiry.toLocalDate().atStartOfDay());
        Timestamp reg = rs.getTimestamp("registered_at");
        if (reg != null) v.setRegisteredAt(reg.toLocalDateTime());
        return v;
    }
}
