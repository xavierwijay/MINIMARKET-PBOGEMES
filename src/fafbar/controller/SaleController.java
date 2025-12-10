package fafbar.controller;

import fafbar.model.Product;  
import fafbar.config.DBConnection;  
import fafbar.model.SaleDetail; 
import fafbar.model.User; 
import fafbar.repository.SaleRepository; 
import java.util.List;
import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal; 
import javax.swing.table.DefaultTableModel; 
import javax.swing.JOptionPane; 

public class SaleController {
    
    private final SaleRepository saleRepo = new SaleRepository(); 
    
    // METHOD 1: cariProdukByKodeAtauNama() - FIX: Menggunakan kolom 'code'
    public Product cariProdukByKodeAtauNama(String input) {
        // FIX: Menggunakan kolom 'code' (sesuai model Product)
        String sql = "SELECT id, code, name, price, stock FROM tbproduct WHERE code = ? OR name LIKE ?"; 

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, input);
            ps.setString(2, "%" + input + "%"); 

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setCode(rs.getString("code")); // FIX: Mengambil dari kolom 'code'
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price")); 
                    p.setStock(rs.getInt("stock"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error saat mencari produk: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error umum saat mencari produk: " + e.getMessage());
            e.printStackTrace();
        }
        return null; 
    }
    
    // METHOD 2: generateInvoice()
    public String generateInvoice() {
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String lastInvoice = saleRepo.getLastInvoiceNumber();
        int newId = 1;
        
        if (lastInvoice != null && lastInvoice.length() > 4) {
            try {
                String numPart = lastInvoice.substring(lastInvoice.lastIndexOf("-") + 1);
                newId = Integer.parseInt(numPart) + 1;
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            }
        }
        String formattedId = String.format("%04d", newId); 
        return "INV-" + datePart + "-" + formattedId;
    }

    // METHOD 3: saveSale() - FULL TRANSACTIONAL LOGIC
    public String saveSale(String invoice, User kasir, double total, double discount, double grandTotal, double cash, double change, DefaultTableModel tableModel) {

        if (tableModel.getRowCount() == 0) {
            return "Gagal: Keranjang belanja kosong.";
        }
        if (cash < grandTotal) {
            return "Gagal: Uang tunai kurang dari Grand Total.";
        }

        List<SaleDetail> details = new ArrayList<>();
        
        // 1. Konversi Data Tabel ke List<SaleDetail>
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                SaleDetail detail = new SaleDetail();
                
                String productCode = tableModel.getValueAt(i, 0).toString();
                String productName = tableModel.getValueAt(i, 1).toString();
                int qty = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
                BigDecimal priceAtSale = BigDecimal.valueOf(Double.parseDouble(tableModel.getValueAt(i, 3).toString()));
                
                // Diskon Item diabaikan di DB (set ke ZERO)
                detail.setDiscountPerItem(BigDecimal.ZERO); 
                
                BigDecimal subtotalItem = BigDecimal.valueOf(Double.parseDouble(tableModel.getValueAt(i, 5).toString()));

                detail.setProductCode(productCode);
                detail.setProductName(productName);
                detail.setQuantity(qty);
                detail.setPriceAtSale(priceAtSale);
                detail.setSubtotal(subtotalItem);
                
                details.add(detail);
                
            } catch (Exception e) {
                System.err.println("Error saat memproses baris detail penjualan: " + e.getMessage());
                e.printStackTrace();
                return "Error data item di baris " + (i + 1) + ".";
            }
        }

        // 2. Konversi Total, Diskon, Cash ke BigDecimal
        BigDecimal bdSubtotal = BigDecimal.valueOf(total);
        BigDecimal bdDiscount = BigDecimal.valueOf(discount);
        BigDecimal bdGrandTotal = BigDecimal.valueOf(grandTotal);
        BigDecimal bdCash = BigDecimal.valueOf(cash);
        BigDecimal bdChange = BigDecimal.valueOf(change);

        // 3. Panggil Repository untuk menyimpan secara Transaksional
        boolean success = saleRepo.saveTransaction(
            invoice, kasir, bdSubtotal, bdDiscount, bdGrandTotal, bdCash, bdChange, details
        );

        if (success) {
            // Kita cetak struk dengan data real (simulasi text)
            cetakStruk(invoice, kasir.getFullName(), total, discount, grandTotal, cash, change);
            return "Transaksi Sukses! Data tersimpan & Stok diperbarui.";
        } else {
            return "Transaksi Gagal! Terjadi kesalahan pada database.";
        }
    }
    
    // METHOD 4: cetakStruk() - VERSI SEDERHANA UNTUK TUGAS
    public boolean cetakStruk(String invoiceNumber, String kasirName, double total, double diskon, double grandTotal, double cash, double change) {
        try {
            String namaToko = "FAFBAR JAYA SHOP"; 
            String alamatToko = "Jl. Diponegoro No. 123, Salatiga";
            
            // Simulasikan Struk Pop-up
            StringBuilder struk = new StringBuilder();
            struk.append("           *** ").append(namaToko).append(" ***\n");
            struk.append("---------------------------------------\n");
            struk.append(String.format("Invoice : %s\n", invoiceNumber));
            struk.append(String.format("Kasir   : %s\n", kasirName));
            struk.append("---------------------------------------\n");
            struk.append("--- Detail Item harus diambil dari DB --- \n");
            struk.append("---------------------------------------\n");
            struk.append(String.format("SUB TOTAL: %29.2f\n", total));
            struk.append(String.format("DISKON  : %29.2f\n", diskon));
            struk.append(String.format("GRAND TOTAL: %26.2f\n", grandTotal));
            struk.append(String.format("BAYAR (CASH): %25.2f\n", cash));
            struk.append(String.format("KEMBALIAN   : %25.2f\n", change));
            struk.append("---------------------------------------\n");
            struk.append(String.format("    *** Terima Kasih Telah Berbelanja ***\n"));

            // Tampilkan sebagai pop-up
            JOptionPane.showMessageDialog(null, struk.toString(), "Struk Penjualan", JOptionPane.PLAIN_MESSAGE);

            return true; 
        } catch (Exception e) {
            System.err.println("Error saat mencetak struk: " + e.getMessage());
            return false; 
        }
    }
    
    // STUB METHOD (untuk jaga-jaga kalau ada pemanggilan cetakStruk lama)
    public boolean cetakStruk(String invoiceNumber, String kasirName) { 
        return true; 
    }

    // STUB METHOD (untuk jaga-jaga kalau ada pemanggilan saveTransactionHeader lama)
    public String saveTransactionHeader(String invoice, fafbar.model.User kasir, double total, double discount, double grandTotal, double cash, double change) {
        return "Gagal! Fungsi ini sudah diganti oleh saveSale.";
    }
}