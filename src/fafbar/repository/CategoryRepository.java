package fafbar.repository;

import fafbar.config.DBConnection;
import fafbar.model.Category;
import java.sql.*;

public class CategoryRepository {

    // --- 1. METHOD INI YANG DICARI SAMA CONTROLLER (Biar gak merah) ---
    public Category findById(String id) {
        String sql = "SELECT * FROM tbcategory WHERE CategoryId = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                // Pastikan urutan ini sesuai dengan Constructor di Category.java
                // (ID, Name, Description)
                return new Category(
                    rs.getString("CategoryId"),
                    rs.getString("name"),
                    rs.getString("description")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Kalau data tidak ketemu, kembalikan null
    }

    // --- 2. Method Simpan (Save) ---
    public boolean save(Category cat) {
        String sql = "INSERT INTO tbcategory (CategoryId, name, description, CreatedAt, UpdatedAt) VALUES (?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, cat.getId());
            pst.setString(2, cat.getName());
            pst.setString(3, cat.getDescription());
            
            pst.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            // Kalau ID sudah ada, biarkan saja (jangan error)
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int getLastId() {
    String sql = "SELECT MAX(CategoryId) FROM tbcategory";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {
        
        if (rs.next()) {
            return rs.getInt(1); // Mengembalikan angka terbesar (misal: 7)
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0; // Kalau kosong, return 0
}
}