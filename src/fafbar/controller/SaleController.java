package fafbar.controller;

import fafbar.model.Product;  
import fafbar.config.DBConnection;  
import java.util.List;
import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel; 

public class SaleController {
    
    // -------------------------------------------------------------------------
    // METHOD 1: cariProdukByKodeAtauNama()
    // -------------------------------------------------------------------------
    
    /**
     * Mencari produk berdasarkan kode, barcode, atau nama dari database.
     * @param input Kode produk (barcode) atau Nama Produk.
     * @return Objek Product jika ditemukan, null jika tidak.
     */
        public Product cariProdukByKodeAtauNama(String input) {
        // FIX KRITIS: Menggunakan tbproduct dan huruf kecil untuk nama kolom di DB
        String sql = "SELECT id, kode, name, price, stock FROM tbproduct WHERE kode = ? OR name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, input);
            ps.setString(2, "%" + input + "%"); 

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setCode(rs.getString("kode")); // Ambil dari kolom 'kode'
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price")); 
                    p.setStock(rs.getInt("stock"));
                    // p.setUnit(rs.getString("unit")); // Hapus ini jika Model Product tidak punya setUnit()
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

    // ... (Method generateInvoice, saveSale, cetakStruk, dll) ...
    
    public String generateInvoice() {
    String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    
    // START: Logika untuk mengambil invoice terakhir dari database
    String lastInvoice = null;
    String sql = "SELECT invoice_number FROM sales ORDER BY id DESC LIMIT 1";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            lastInvoice = rs.getString("invoice_number");
        }
    } catch (SQLException e) {
        System.err.println("Gagal ambil invoice terakhir: " + e.getMessage());
    }
    // END: Logika untuk mengambil invoice terakhir dari database
    
    int newId = 1;
    
    if (lastInvoice != null && lastInvoice.length() > 4) {
        // Asumsi format: INV-YYYYMMDD-XXXX. Ambil XXXX dan tambahkan 1.
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