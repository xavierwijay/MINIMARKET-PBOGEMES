package fafbar.repository;

import fafbar.config.DBConnection;
import fafbar.model.SaleDetail; 
import fafbar.model.User;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class SaleRepository {

    public String getLastInvoiceNumber() {
        // FIX KRITIS #1: Mengubah ORDER BY dari 'id' menjadi 'created_at' 
        String sql = "SELECT receipt_number FROM sales ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString("receipt_number");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // =================================
    // SAVE TRANSAKSI LENGKAP (Header + Detail + Stok Update)
    // =================================
    public boolean saveTransaction(String invoiceNumber, User kasir, 
                                   BigDecimal subtotal, BigDecimal discountTotal, 
                                   BigDecimal totalAmount, BigDecimal amountPaid, 
                                   BigDecimal changeAmount, List<SaleDetail> details) {
        
        // 1. QUERY SALES (Header)
        String sqlSale = "INSERT INTO SALES (receipt_number, user_id, subtotal, discount_total, total_amount, amount_paid, change_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        // 2. QUERY SALES_DETAILS (Detail)
        String sqlDetail = "INSERT INTO sales_details (receipt_number, product_code, product_name, qty, price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        
        // 3. QUERY UPDATE STOCK
        // FIX KRITIS #2: Menggunakan kolom 'Code' (ejaan database Anda)
        String sqlStockUpdate = "UPDATE tbproduct SET stock = stock - ? WHERE Code = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION
            
            // --- A. Simpan Header Transaksi (SALES) ---
            try (PreparedStatement psSale = conn.prepareStatement(sqlSale)) { 
                psSale.setString(1, invoiceNumber);
                psSale.setInt(2, kasir.getId());
                psSale.setBigDecimal(3, subtotal);
                psSale.setBigDecimal(4, discountTotal);
                psSale.setBigDecimal(5, totalAmount);
                psSale.setBigDecimal(6, amountPaid);
                psSale.setBigDecimal(7, changeAmount);
                
                if (psSale.executeUpdate() == 0) {
                    throw new SQLException("Gagal menyimpan header transaksi. No rows affected.");
                }
            }
            
            // --- B. Simpan Detail Item dan Update Stok ---
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                 PreparedStatement psStock = conn.prepareStatement(sqlStockUpdate)) {

                for (SaleDetail detail : details) {
                    
                    // 1. Simpan Detail Item
                    psDetail.setString(1, invoiceNumber); 
                    psDetail.setString(2, detail.getProductCode());
                    psDetail.setString(3, detail.getProductName());
                    psDetail.setInt(4, detail.getQuantity());
                    psDetail.setBigDecimal(5, detail.getPriceAtSale()); 
                    psDetail.setBigDecimal(6, detail.getSubtotal());
                    psDetail.addBatch(); 
                    
                    // 2. Kurangi Stok
                    psStock.setInt(1, detail.getQuantity());
                    psStock.setString(2, detail.getProductCode());
                    psStock.addBatch();
                }
                
                psDetail.executeBatch(); 
                psStock.executeBatch(); 
            }
            
            conn.commit(); // COMMIT!
            return true; 

        } catch (SQLException e) {
            System.err.println("SQL ERROR saat menyimpan transaksi: " + e.getMessage());
            e.printStackTrace(); 
            if (conn != null) {
                try {
                    conn.rollback(); 
                } catch (SQLException excep) {}
            }
            return false;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {}
            }
        }
    }
}