package fafbar.repository;

import fafbar.config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.util.Calendar;

public class ReportRepository {
    
    // Format untuk menampilkan tanggal di View
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public List<Object[]> getFilteredSalesData(Date dateFrom, Date dateTo, int userId) {
        List<Object[]> reportData = new ArrayList<>();
        
        String sql = "SELECT s.receipt_number, s.created_at, s.subtotal, s.discount_total, " +
                     "s.total_amount, s.amount_paid, s.change_amount, u.name AS cashier_name " +
                     "FROM SALES s JOIN USERS u ON s.user_id = u.id " +
                     "WHERE s.created_at >= ? AND s.created_at < ?"; // Filter menggunakan created_at
        
        if (userId > 0) {
            sql += " AND s.user_id = ?";
        }
        
        sql += " ORDER BY s.created_at DESC"; // Sorting menggunakan created_at
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            // --- 1. SET TANGGAL MULAI (tsFrom): Tepat pada 00:00:00 ---
            Calendar calFrom = Calendar.getInstance();
            calFrom.setTime(dateFrom);
            calFrom.set(Calendar.HOUR_OF_DAY, 0);
            calFrom.set(Calendar.MINUTE, 0);
            calFrom.set(Calendar.SECOND, 0);
            calFrom.set(Calendar.MILLISECOND, 0);
            Timestamp tsFrom = new Timestamp(calFrom.getTimeInMillis());
            
            // --- 2. SET TANGGAL AKHIR (tsTo): Tepat pada 00:00:00 HARI BERIKUTNYA ---
            Calendar calTo = Calendar.getInstance();
            calTo.setTime(dateTo);
            calTo.add(Calendar.DAY_OF_MONTH, 1); // Geser ke hari berikutnya
            calTo.set(Calendar.HOUR_OF_DAY, 0);
            calTo.set(Calendar.MINUTE, 0);
            calTo.set(Calendar.SECOND, 0);
            calTo.set(Calendar.MILLISECOND, 0);
            Timestamp tsTo = new Timestamp(calTo.getTimeInMillis());
            
            // --- 3. BINDING PARAMETER ---
            pst.setTimestamp(1, tsFrom);
            pst.setTimestamp(2, tsTo);
            
            int paramIndex = 3;
            if (userId > 0) {
                pst.setInt(paramIndex, userId);
            }
            
            // --- 4. EKSEKUSI QUERY DAN MAPPING ---
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[]{
                        rs.getString("receipt_number"),
                        DATETIME_FORMAT.format(rs.getTimestamp("created_at")), 
                        rs.getString("cashier_name"),
                        rs.getBigDecimal("subtotal").intValue(),
                        rs.getBigDecimal("discount_total").intValue(),
                        rs.getBigDecimal("total_amount").intValue(),
                        rs.getBigDecimal("amount_paid").intValue(),
                        rs.getBigDecimal("change_amount").intValue()
                    };
                    reportData.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL ERROR: Gagal mengambil laporan penjualan: " + e.getMessage());
            e.printStackTrace(); 
        }
        return reportData;
    }
}