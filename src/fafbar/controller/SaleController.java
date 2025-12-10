package fafbar.controller;

import fafbar.model.Product;  
import fafbar.config.DBConnection;  
import fafbar.model.SaleDetail; // Import model baru
import fafbar.model.User; // Import User
import fafbar.repository.SaleRepository; // Import SaleRepository
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

public class SaleController {
    
    private final SaleRepository saleRepo = new SaleRepository();
    
    // -------------------------------------------------------------------------
    // METHOD 1: cariProdukByKodeAtauNama() (SUDH ADA)
    // -------------------------------------------------------------------------
    
    /**
     * Mencari produk berdasarkan kode, barcode, atau nama dari database.
     * @param input Kode produk (barcode) atau Nama Produk.
     * @return Objek Product jika ditemukan, null jika tidak.
     */
    // [KODE cariProdukByKodeAtauNama tidak diubah] ...
        public Product cariProdukByKodeAtauNama(String input) {
        // FIX KRITIS: Menggunakan tbproduct dan huruf kecil untuk nama kolom di DB
        String sql = "SELECT id, code, name, price, stock FROM tbproduct WHERE code = ? OR name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, input);
            ps.setString(2, "%" + input + "%"); 

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setCode(rs.getString("code")); // Ambil dari kolom 'code'
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

    // -------------------------------------------------------------------------
    // METHOD 2: generateInvoice() (SUDH ADA)
    // -------------------------------------------------------------------------
    
    public String generateInvoice() {
    String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    
    String lastInvoice = saleRepo.getLastInvoiceNumber();
    
    int newId = 1;
    
    if (lastInvoice != null && lastInvoice.length() > 4) {
        try {
            String numPart = lastInvoice.substring(lastInvoice.lastIndexOf("-") + 1);
            newId = Integer.parseInt(numPart) + 1;
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            // Biarkan newId tetap 1 jika parsing gagal
        }
    }
    
    String formattedId = String.format("%04d", newId); // Format menjadi 0001
    
    return "INV-" + datePart + "-" + formattedId;
}

    // -------------------------------------------------------------------------
    // METHOD 3: saveSale() - CORE LOGIC BARU
    // -------------------------------------------------------------------------
    
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
                
                // Asumsi kolom tabel: 0=ID Barang, 1=Nama, 2=Qty, 3=Harga, 4=Disc Item, 5=Total
                
                String productCode = tableModel.getValueAt(i, 0).toString();
                String productName = tableModel.getValueAt(i, 1).toString();
                int qty = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
                BigDecimal priceAtSale = BigDecimal.valueOf(Double.parseDouble(tableModel.getValueAt(i, 3).toString()));
                BigDecimal discPerItem = BigDecimal.valueOf(Double.parseDouble(tableModel.getValueAt(i, 4).toString()));
                BigDecimal subtotalItem = BigDecimal.valueOf(Double.parseDouble(tableModel.getValueAt(i, 5).toString()));
                
                detail.setProductCode(productCode);
                detail.setProductName(productName);
                detail.setQuantity(qty);
                detail.setPriceAtSale(priceAtSale);
                detail.setDiscountPerItem(discPerItem);
                detail.setSubtotal(subtotalItem);
                
                details.add(detail);
                
            } catch (Exception e) {
                System.err.println("Error saat memproses baris detail penjualan: " + e.getMessage());
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
            // Jika sukses, coba cetak struk (simulasi)
            cetakStruk(invoice, kasir.getFullName());
            return "Transaksi Sukses! Invoice: " + invoice;
        } else {
            return "Transaksi Gagal! Terjadi kesalahan pada database.";
        }
    }
    
    // -------------------------------------------------------------------------
    // METHOD 4: cetakStruk() (SUDH ADA)
    // -------------------------------------------------------------------------
    
    // [KODE cetakStruk tidak diubah] ...
    public boolean cetakStruk(String invoiceNumber, String kasirName) {
        try {

            // SIMULASI DATA YANG SEHARUSNYA DIAMBIL DARI DB:
            String namaToko = "FAFBAR JAYA SHOP"; 
            String alamatToko = "Jl. Diponegoro No. 123, Salatiga";

            // Data Transaksi
            double total = 75000.00; // HARUS DIAMBIL DARI DB
            double diskon = 5000.00; // HARUS DIAMBIL DARI DB
            double grandTotal = total - diskon;
            double cash = 100000.00; // HARUS DIAMBIL DARI DB
            double change = cash - grandTotal; // HARUS DIAMBIL DARI DB

            // Simulasi Item Penjualan (HARUS DIAMBIL DARI DB)
            List<String> items = new ArrayList<>();
            items.add("Indomie Goreng @ 3.000 x 2 =  6.000");
            items.add("Belfood Sosis  @ 27.000 x 1 = 27.000");
            items.add("Fruit Tea 500ml @ 7.000 x 6 =  42.000");

            // --- PROSES PEMBENTUKAN STRUK MEMANJANG ---

            StringBuilder struk = new StringBuilder();

            // Header
            struk.append("---------------------------------------\n");
            struk.append(String.format("       %s\n", namaToko));
            struk.append(String.format("       %s\n", alamatToko));
            struk.append("---------------------------------------\n");

            // Detail Transaksi
            struk.append(String.format("Invoice : %s\n", invoiceNumber));
            struk.append(String.format("Tanggal : %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
            struk.append(String.format("Kasir   : %s\n", kasirName)); // Menggunakan parameter yang dikirim dari SalesFrame
            struk.append("---------------------------------------\n");

            // Detail Item
            for (String item : items) {
                struk.append(String.format("%s\n", item));
            }
            struk.append("---------------------------------------\n");

            // Total dan Pembayaran
            struk.append(String.format("SUB TOTAL: %29.2f\n", total));
            struk.append(String.format("DISKON  : %29.2f\n", diskon));
            struk.append(String.format("GRAND TOTAL: %26.2f\n", grandTotal));
            struk.append("---------------------------------------\n");
            struk.append(String.format("BAYAR (CASH): %25.2f\n", cash));
            struk.append(String.format("KEMBALIAN   : %25.2f\n", change));
            struk.append("---------------------------------------\n");

            // Footer
            struk.append(String.format("    *** Terima Kasih Telah Berbelanja ***\n"));
            struk.append("\n\n"); 

            // --- SIMULASI PENCETAKAN KE KONSOLE ---
            System.out.println("\n===== PREVIEW STRUK (FORMAT MENTAH) =====\n");
            System.out.println(struk.toString());
            System.out.println("========================================\n");

            return true; 

        } catch (Exception e) {
            System.err.println("Error saat mencetak struk: " + e.getMessage());
            return false; 
        }
    }
}