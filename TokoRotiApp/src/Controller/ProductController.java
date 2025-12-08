package Controller;

import Config.Koneksi;
import Model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductController {
    
    // 1. FITUR TAMBAH ROTI (CREATE)
    public void tambahData(Product p) {
        String sql = "INSERT INTO products (name, price, stock, category) VALUES (?, ?, ?, ?)";
        try {
            Connection c = Koneksi.configDB();
            PreparedStatement pst = c.prepareStatement(sql);
            pst.setString(1, p.getName());
            pst.setDouble(2, p.getPrice());
            pst.setInt(3, p.getStock());
            pst.setString(4, p.getCategory());
            pst.execute();
            System.out.println("Berhasil Tambah Data: " + p.getName());
        } catch (Exception e) {
            System.err.println("Gagal Tambah Data: " + e.getMessage());
        }
    }

    // 2. FITUR EDIT ROTI (UPDATE)
    public void editData(Model.Product p) {
        // Query Update: Ubah nama, harga, stok, kategori BERDASARKAN ID
        String sql = "UPDATE products SET name=?, price=?, stock=?, category=? WHERE product_id=?";
        
        try {
            java.sql.Connection c = Config.Koneksi.configDB();
            java.sql.PreparedStatement pst = c.prepareStatement(sql);
            
            // Masukkan data baru ke tanda tanya (?)
            pst.setString(1, p.getName());
            pst.setDouble(2, p.getPrice());
            pst.setInt(3, p.getStock());
            pst.setString(4, p.getCategory());
            
            // PENTING: ID ditaruh di tanda tanya terakhir (WHERE)
            pst.setInt(5, p.getId());
            
            pst.executeUpdate();
            System.out.println("Berhasil Update ID: " + p.getId());
            
        } catch (Exception e) {
            System.err.println("Gagal Update: " + e.getMessage());
        }
    }

    // 3. FITUR HAPUS ROTI (DELETE)
    public void hapusData(int id) {
        String sql = "DELETE FROM products WHERE product_id=?";
        try {
            Connection c = Koneksi.configDB();
            PreparedStatement pst = c.prepareStatement(sql);
            pst.setInt(1, id);
            pst.execute();
            System.out.println("Berhasil Hapus Data ID: " + id);
        } catch (Exception e) {
            System.err.println("Gagal Hapus Data: " + e.getMessage());
        }
    }

    // 4. FITUR AMBIL SEMUA DATA (READ) -> Buat ditampilkan di Tabel
    public List<Product> ambilSemuaData() {
        List<Product> listProduct = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try {
            Connection c = Koneksi.configDB();
            Statement stm = c.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("product_id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock"));
                p.setCategory(rs.getString("category"));
                
                listProduct.add(p);
            }
        } catch (Exception e) {
            System.err.println("Gagal Ambil Data: " + e.getMessage());
        }
        return listProduct;
    }
}