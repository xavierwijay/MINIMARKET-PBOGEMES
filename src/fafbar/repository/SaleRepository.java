package fafbar.repository;

import fafbar.config.DBConnection;
import fafbar.model.SaleDetail; 
import fafbar.model.User;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class SaleRepository {

    // Metode untuk mengambil nomor invoice terakhir (sudah benar)
    public String getLastInvoiceNumber() {
        String sql = "SELECT receipt_number FROM sales ORDER BY id DESC LIMIT 1";
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
    // FITUR: SAVE TRANSAKSI (Transactional)
    // =================================
    public boolean saveTransaction(String invoiceNumber, User kasir, 
                                   BigDecimal subtotal, BigDecimal discountTotal, 
                                   BigDecimal totalAmount, BigDecimal amountPaid, 
                                   BigDecimal changeAmount, List<SaleDetail> details) {
        
        // 1. Query untuk menyimpan Header Penjualan (SALES)
        String sqlSale = "INSERT INTO SALES (receipt_number, user_id, subtotal, discount_total, total_amount, amount_paid, change_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        // 2. Query untuk menyimpan Detail Item (SALES_DETAILS)
        // FIX KRITIS: Menghapus kolom sale_id karena diasumsikan tabel Anda hanya menggunakan receipt_number sebagai FK
        String sqlDetail = "INSERT INTO sales_details (receipt_number, product_code, product_name, qty, price, discount_per_item, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // MULAI TRANSAKSI (Wajib untuk OOP Transaction Management)
            
            // --- A. Simpan Header Transaksi (SALES) ---
            // Kita tidak perlu mendapatkan generated keys (saleId) karena tidak dipakai di tabel detail
            try (PreparedStatement psSale = conn.prepareStatement(sqlSale)) { 
                psSale.setString(1, invoiceNumber);
                psSale.setInt(2, kasir.getId());
                psSale.setBigDecimal(3, subtotal);
                psSale.setBigDecimal(4, discountTotal);
                psSale.setBigDecimal(5, totalAmount);
                psSale.setBigDecimal(6, amountPaid);
                psSale.setBigDecimal(7, changeAmount);
                
                int rowsAffected = psSale.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Gagal menyimpan header transaksi, tidak ada baris yang terpengaruh.");
                }
            }
            
            // --- B. Simpan Detail Item (SALES_DETAILS) ---
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                for (SaleDetail detail : details) {
                    // FIX: BINDING 7 PARAMETER (receipt_number, product_code, product_name, qty, price, discount_per_item, subtotal)
                    psDetail.setString(1, invoiceNumber); 
                    psDetail.setString(2, detail.getProductCode());
                    psDetail.setString(3, detail.getProductName());
                    psDetail.setInt(4, detail.getQuantity());
                    psDetail.setBigDecimal(5, detail.getPriceAtSale()); 
                    psDetail.setBigDecimal(6, detail.getDiscountPerItem()); // discount_per_item
                    psDetail.setBigDecimal(7, detail.getSubtotal());
                    
                    psDetail.addBatch(); 
                }
                psDetail.executeBatch(); 
            }
            
            // --- C. Commit Transaksi ---
            conn.commit();
            return true; 

        } catch (SQLException e) {
            System.err.println("SQL ERROR saat menyimpan transaksi: " + e.getMessage());
            e.printStackTrace(); // Tampilkan error di konsol
            if (conn != null) {
                try {
                    System.err.print("Transaction is being rolled back");
                    conn.rollback(); // BATALKAN jika ada error
                } catch (SQLException excep) {
                    System.err.println("Rollback gagal: " + excep.getMessage());
                }
            }
            return false;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Gagal menutup koneksi: " + e.getMessage());
                }
            }
        }
    }
}