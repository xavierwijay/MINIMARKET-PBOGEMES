package Controller;

import Config.Koneksi;
import Model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductController {
    
    // 1. FITUR TAMBAH ROTI (CREATE) - Masuk ke tbmakanan
    public void tambahData(Product p) {
        // PERUBAHAN DISINI: Menggunakan tbmakanan dan kolom image_path
        String sql = "INSERT INTO tbmakanan (name, price, stock, category, image_path) VALUES (?, ?, ?, ?, ?)";
        
        try {
            Connection c = Koneksi.configDB();
            PreparedStatement pst = c.prepareStatement(sql);
            
            pst.setString(1, p.getName());
            pst.setDouble(2, p.getPrice());
            pst.setInt(3, p.getStock());
            pst.setString(4, p.getCategory());
            
            // Pastikan di Model Product sudah ada method getImagePath()
            // Jika belum ada, ganti dengan string kosong "" dulu: pst.setString(5, "");
            pst.setString(5, p.getImagePath()); 
            
            pst.execute();
            System.out.println("Berhasil Tambah Data ke tbmakanan: " + p.getName());
        } catch (Exception e) {
            System.err.println("Gagal Tambah Data: " + e.getMessage());
        }
    }

    // 2. FITUR EDIT ROTI (UPDATE)
    public void editData(Product p) {
        // Query Update: Ubah nama, harga, stok, kategori, gambar BERDASARKAN product_id
        String sql = "UPDATE tbmakanan SET name=?, price=?, stock=?, category=?, image_path=? WHERE product_id=?";
        
        try {
            Connection c = Koneksi.configDB();
            PreparedStatement pst = c.prepareStatement(sql);
            
            pst.setString(1, p.getName());
            pst.setDouble(2, p.getPrice());
            pst.setInt(3, p.getStock());
            pst.setString(4, p.getCategory());
            pst.setString(5, p.getImagePath());
            
            // ID ditaruh di tanda tanya terakhir (WHERE product_id=?)
            pst.setInt(6, p.getId());
            
            pst.executeUpdate();
            System.out.println("Berhasil Update ID: " + p.getId());
            
        } catch (Exception e) {
            System.err.println("Gagal Update: " + e.getMessage());
        }
    }

    // 3. FITUR HAPUS ROTI (DELETE)
    public void hapusData(int id) {
        // Hapus berdasarkan product_id
        String sql = "DELETE FROM tbmakanan WHERE product_id=?";
        
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

    // 4. FITUR AMBIL SEMUA DATA (READ)
    public List<Product> ambilSemuaData() {
        List<Product> listProduct = new ArrayList<>();
        // Ambil semua dari tbmakanan
        String sql = "SELECT * FROM tbmakanan"; 
        
        try {
            Connection c = Koneksi.configDB();
            Statement stm = c.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            
            while(rs.next()) {
                // Perhatikan nama kolom sesuai database tbmakanan yang baru di-alter
                int id = rs.getInt("product_id"); 
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                String category = rs.getString("category");
                String imagePath = rs.getString("image_path");
                
                // Masukkan ke Constructor Product (Pastikan urutannya sesuai dengan Model kamu)
                Product p = new Product(id, name, price, stock, category, imagePath);
                
                listProduct.add(p);
            }
        } catch (Exception e) {
            System.err.println("Gagal Ambil Data: " + e.getMessage());
        }
        return listProduct;
    }
    
    // Controller/ProductController.java
public List<Product> ambilByKategori(String kategori) {
     List<Product> list = new ArrayList<>();
    String sql = "SELECT * FROM tbmakanan WHERE UPPER(category) = UPPER(?)";

    try (Connection c = Koneksi.configDB();
         PreparedStatement pst = c.prepareStatement(sql)) {

        pst.setString(1, kategori);
        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("product_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");
                String category = rs.getString("category");
                String imagePath = rs.getString("image_path");

                list.add(new Product(id, name, price, stock, category, imagePath));
            }
        }

    } catch (Exception e) {
        System.err.println("Gagal ambilByKategori: " + e.getMessage());
    }

    return list;
}

// Opsional: hanya yang stok > 0
public List<Product> ambilByKategoriTersedia(String kategori) {
    List<Product> list = new ArrayList<>();
    String sql = "SELECT product_id, name, price, stock, category, image_path " +
                 "FROM tbmakanan WHERE UPPER(category)=UPPER(?) AND stock > 0 " +
                 "ORDER BY product_id DESC";
    try (Connection c = Koneksi.configDB();
         PreparedStatement pst = c.prepareStatement(sql)) {
        pst.setString(1, kategori);
        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(new Product(
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    rs.getString("image_path")
                ));
            }
        }
    } catch (SQLException e) {
        System.err.println("Gagal ambil by kategori (stok): " + e.getMessage());
    }
    return list;
}
    public Product ambilById(int id) {
    Product p = null;
    String sql = "SELECT * FROM tbmakanan WHERE product_id = ?";

    try (Connection c = Koneksi.configDB();
         PreparedStatement pst = c.prepareStatement(sql)) {

        pst.setInt(1, id);
        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                p = new Product(
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("stock"),
                    rs.getString("category"),
                    rs.getString("image_path")
                );
            }
        }
    } catch (Exception e) {
        System.err.println("Gagal ambilById: " + e.getMessage());
    }

    return p;
}
    public boolean kurangiStok(int productId, int qty) {
    String sql = "UPDATE tbmakanan " +
                 "SET stock = stock - ? " +
                 "WHERE product_id = ? AND stock >= ?";

    try (Connection c = Koneksi.configDB();
         PreparedStatement pst = c.prepareStatement(sql)) {

        pst.setInt(1, qty);
        pst.setInt(2, productId);
        pst.setInt(3, qty);

        int updated = pst.executeUpdate();
        return updated > 0;   // true = stok berhasil dikurangi

    } catch (SQLException e) {
        System.err.println("Gagal kurangiStok: " + e.getMessage());
        return false;
}
    } 
}