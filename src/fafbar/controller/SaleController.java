package fafbar.controller;

import fafbar.model.Product;
import fafbar.config.DBConnection;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel;

public class SaleController {
    
    public Product cariProdukByKodeAtauNama(String input) {
        // PERBAIKAN: Menggunakan nama kolom sesuai database (Huruf Besar Awal)
        // Database kamu: ID, Code, Name, Price, Stock
        String sql = "SELECT ID, Code, Name, Price, Stock FROM tbproduct WHERE Code = ? OR Name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, input);
            ps.setString(2, "%" + input + "%"); 

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    // PERBAIKAN: Ambil data sesuai nama kolom di DB (Case Sensitive)
                    p.setId(rs.getInt("ID"));
                    p.setCode(rs.getString("Code")); 
                    p.setName(rs.getString("Name"));
                    p.setPrice(rs.getDouble("Price")); 
                    p.setStock(rs.getInt("Stock"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error saat mencari produk: " + e.getMessage());
            e.printStackTrace();
        }
        return null; 
    }

    // -------------------------------------------------------------------------
    // METHOD 2: Simpan Transaksi (SUDAH DIPERBAIKI NAMA KOLOMNYA)
    // -------------------------------------------------------------------------
    public boolean saveSale(String invoiceNumber, int userId, double subtotal, 
                            double discount, double grandTotal, double cash, 
                            double change, DefaultTableModel tableModel) {
        
        Connection conn = null;
        PreparedStatement psSale = null;
        PreparedStatement psItem = null;
        PreparedStatement psUpdateStock = null;
        PreparedStatement psGetProductId = null; 

        // Query SQL tetap sama
        String sqlSale = "INSERT INTO sales (receipt_number, user_id, subtotal, discount_total, total_amount, amount_paid, change_amount, sale_datetime, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), NOW())";
        String sqlItem = "INSERT INTO sale_items (sale_id, product_id, quantity, price, discount_item, final_price_per_unit, line_total, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        String sqlStock = "UPDATE tbproduct SET Stock = Stock - ? WHERE Code = ?";
        String sqlCariId = "SELECT ID FROM tbproduct WHERE Code = ?";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 

            // A. Simpan Header Sales
            psSale = conn.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS);
            psSale.setString(1, invoiceNumber);
            psSale.setInt(2, userId);
            psSale.setBigDecimal(3, BigDecimal.valueOf(subtotal));
            psSale.setBigDecimal(4, BigDecimal.valueOf(discount));
            psSale.setBigDecimal(5, BigDecimal.valueOf(grandTotal));
            psSale.setBigDecimal(6, BigDecimal.valueOf(cash));
            psSale.setBigDecimal(7, BigDecimal.valueOf(change));
            psSale.executeUpdate();

            int saleId = 0;
            try (ResultSet rs = psSale.getGeneratedKeys()) {
                if (rs.next()) saleId = rs.getInt(1);
            }

            psGetProductId = conn.prepareStatement(sqlCariId);
            psItem = conn.prepareStatement(sqlItem);
            psUpdateStock = conn.prepareStatement(sqlStock);

            // B. Loop Item (DENGAN PENGECEKAN NULL)
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                
                // PERBAIKAN UTAMA: Cek dulu apakah sel ID Barang (kolom 0) ada isinya
                Object codeObj = tableModel.getValueAt(i, 0);
                if (codeObj == null || codeObj.toString().trim().isEmpty()) {
                    continue; // Jika kosong, lewati baris ini (jangan diproses)
                }

                String code = codeObj.toString();
                
                // Ambil nilai lain dengan aman (pakai toString() lalu parse)
                int qty = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
                double price = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
                double discItem = Double.parseDouble(tableModel.getValueAt(i, 4).toString());
                double totalLine = Double.parseDouble(tableModel.getValueAt(i, 5).toString());

                // Cari ID Produk
                int productId = 0;
                psGetProductId.setString(1, code);
                try (ResultSet rsProd = psGetProductId.executeQuery()) {
                    if (rsProd.next()) {
                        productId = rsProd.getInt("ID");
                    } else {
                        throw new SQLException("Produk kode " + code + " tidak valid!");
                    }
                }

                // Batch Insert Item
                psItem.setInt(1, saleId);
                psItem.setInt(2, productId);
                psItem.setInt(3, qty);
                psItem.setBigDecimal(4, BigDecimal.valueOf(price));
                psItem.setBigDecimal(5, BigDecimal.valueOf(discItem));
                psItem.setBigDecimal(6, BigDecimal.valueOf(price - discItem));
                psItem.setBigDecimal(7, BigDecimal.valueOf(totalLine));
                psItem.addBatch();

                // Batch Update Stok
                psUpdateStock.setInt(1, qty);
                psUpdateStock.setString(2, code);
                psUpdateStock.addBatch();
            }

            psItem.executeBatch();
            psUpdateStock.executeBatch();

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try {
                if (psSale != null) psSale.close();
                if (psItem != null) psItem.close();
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // -------------------------------------------------------------------------
    // METHOD 3: Generate Invoice
    // -------------------------------------------------------------------------
    public String generateInvoice() {
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String lastInvoice = null;
        
        // PERBAIKAN: Sesuaikan dengan nama tabel 'sales' dan kolom 'receipt_number'
        String sql = "SELECT receipt_number FROM sales ORDER BY id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lastInvoice = rs.getString("receipt_number");
            }
        } catch (SQLException e) {
            System.err.println("Gagal ambil invoice terakhir: " + e.getMessage());
        }
        
        int newId = 1;
        if (lastInvoice != null && lastInvoice.length() > 4) {
            try {
                String numPart = lastInvoice.substring(lastInvoice.lastIndexOf("-") + 1);
                newId = Integer.parseInt(numPart) + 1;
            } catch (Exception e) {}
        }
        return "INV-" + datePart + "-" + String.format("%04d", newId);
    }
    
    // -------------------------------------------------------------------------
    // METHOD 4: Cetak Struk (Sederhana)
    // -------------------------------------------------------------------------
    public boolean cetakStruk(String invoiceNumber, String kasirName) {
        System.out.println("mencetak struk: " + invoiceNumber + " oleh " + kasirName);
        return true; 
    }
}