package com.parking.service;

import com.parking.dao.BookingDAO;
import com.parking.dao.PaymentDAO;
import com.parking.dao.SpotDAO;
import com.parking.dao.RateDAO;
import com.parking.model.*;

import java.sql.*;
import java.util.List;

/**
 * Revenue and occupancy analytics service.
 */
public class RevenueService {

    private static final int DEFAULT_LOT_ID = 1;

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final SpotDAO    spotDAO    = new SpotDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RateDAO    rateDAO    = new RateDAO();

    public void printRevenueReport() throws SQLException {
        System.out.println("\n  ══════════════════════════════════════════════");
        System.out.println("          REVENUE & OCCUPANCY REPORT");
        System.out.println("  ══════════════════════════════════════════════");
        System.out.printf("  Today's Revenue    : ₹%.2f%n", paymentDAO.getTodayRevenue());
        System.out.printf("  Total Revenue      : ₹%.2f%n", paymentDAO.getTotalRevenue());
        System.out.printf("  Active Bookings    : %d%n",    bookingDAO.findAllActive().size());

        int total    = spotDAO.countTotal(DEFAULT_LOT_ID);
        int occupied = spotDAO.countOccupied(DEFAULT_LOT_ID);
        double pct   = total > 0 ? (occupied * 100.0 / total) : 0;
        System.out.printf("  Occupancy          : %d / %d (%.1f%%)%n", occupied, total, pct);

        double surge = 1.0;
        List<Rate> rates = rateDAO.findAllByLot(DEFAULT_LOT_ID);
        if (!rates.isEmpty()) surge = rates.get(0).getSurgeMultiplier();
        System.out.printf("  Surge Multiplier   : ×%.2f%n", surge);
        System.out.println("  ══════════════════════════════════════════════");
    }

    public void printOccupancyByType() throws SQLException {
        System.out.println("\n  ══════════════════════════════════════════════");
        System.out.println("          OCCUPANCY BY SPOT TYPE");
        System.out.println("  ══════════════════════════════════════════════");
        String[] types = {"BIKE", "CAR", "VAN", "TRUCK"};
        Connection conn = com.parking.util.DBConnection.getConnection();
        for (String type : types) {
            String sql = "SELECT COUNT(*) AS total, SUM(is_occupied) AS occ " +
                         "FROM spot WHERE lot_id=? AND spot_type=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, DEFAULT_LOT_ID);
                ps.setString(2, type);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int occ   = rs.getInt("occ");
                    System.out.printf("  %-6s : %2d / %2d occupied%n", type, occ, total);
                }
            }
        }
        System.out.println("  ══════════════════════════════════════════════");
    }

    public void printRecentPayments() throws SQLException {
        System.out.println("\n  ══════════════════════════════════════════════");
        System.out.println("          RECENT PAYMENT HISTORY (last 10)");
        System.out.println("  ══════════════════════════════════════════════");
        System.out.printf("  %-6s %-8s %-8s %-10s %-8s %-10s%n",
                "PayID","BookID","VehicID","Total(₹)","Mode","Status");
        System.out.println("  " + "─".repeat(56));
        List<Payment> payments = paymentDAO.findAll();
        int count = 0;
        for (Payment p : payments) {
            if (count++ >= 10) break;
            System.out.printf("  %-6d %-8d %-8d ₹%-9.2f %-8s %-10s%n",
                    p.getPaymentId(), p.getBookingId(), p.getVehicleId(),
                    p.getTotalAmount(), p.getPaymentMode(), p.getPaymentStatus());
        }
        System.out.println("  ══════════════════════════════════════════════");
    }
}
