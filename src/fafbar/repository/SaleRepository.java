package fafbar.repository;

import fafbar.config.DBConnection;
import java.sql.*;
import java.util.List;

public class SaleRepository {
    
    // Ambil nomor invoice terakhir untuk generate nomor baru
    public String getLastInvoiceNumber() {
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

    /**
     * MENYIMPAN TRANSAKSI (Header + Detail + Potong Stok)
     * Menggunakan Transaction agar data konsisten.
     */
    public boolean saveTransaction(String invoice, double subtotal, double discount, 
                                   double grandTotal, double cash, double change, 
                                   int userId, List<Object[]> items) {
        
        Connection conn = null;
        PreparedStatement psSale = null;
        PreparedStatement psDetail = null;
        PreparedStatement psStock = null;

        // Query SQL
        String sqlSale = "INSERT INTO sales (receipt_number, subtotal, discount_total, total_amount, amount_paid, change_amount, user_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        String sqlDetail = "INSERT INTO sales_details (receipt_number, product_code, product_name, qty, price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlStock = "UPDATE tbproduct SET stock = stock - ? WHERE Code = ?"; 

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 1. Mulai Transaksi (Matikan Auto Save)

            // --- A. Simpan Header (Tabel Sales) ---
            psSale = conn.prepareStatement(sqlSale);
            psSale.setString(1, invoice);
            psSale.setDouble(2, subtotal);
            psSale.setDouble(3, discount);
            psSale.setDouble(4, grandTotal);
            psSale.setDouble(5, cash);
            psSale.setDouble(6, change);
            psSale.setInt(7, userId);
            psSale.executeUpdate();

            // --- B. Simpan Detail & Potong Stok ---
            psDetail = conn.prepareStatement(sqlDetail);
            psStock = conn.prepareStatement(sqlStock);

            for (Object[] item : items) {
                // Struktur item: [0:Kode, 1:Nama, 2:Qty, 3:Harga, 4:Disc, 5:Total]
                String code = (String) item[0];
                String name = (String) item[1];
                int qty = (int) item[2];
                double price = (double) item[3];
                double lineTotal = (double) item[5];

                // Detail
                psDetail.setString(1, invoice);
                psDetail.setString(2, code);
                psDetail.setString(3, name);
                psDetail.setInt(4, qty);
                psDetail.setDouble(5, price);
                psDetail.setDouble(6, lineTotal);
                psDetail.addBatch();

                // Stok
                psStock.setInt(1, qty);
                psStock.setString(2, code);
                psStock.addBatch();
            }

            psDetail.executeBatch();
            psStock.executeBatch();

            conn.commit(); // 2. Simpan Permanen (Commit)
            return true;

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {} // Batalkan jika error
            System.err.println("Transaction Failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Tutup Koneksi
            try { if (psSale != null) psSale.close(); } catch (Exception e) {}
            try { if (psDetail != null) psDetail.close(); } catch (Exception e) {}
            try { if (psStock != null) psStock.close(); } catch (Exception e) {}
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception e) {}
        }
    }
}