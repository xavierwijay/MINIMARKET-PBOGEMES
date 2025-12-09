package fafbar.repository;

import fafbar.config.DBConnection;
import fafbar.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    // 1. AMBIL DATA (SELECT) - Menggantikan logic loadData yang lama
    public List<Product> findAll(String catId, String keyword) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM tbproduct WHERE 1=1");

        // Logic Filter Dinamis
        if (catId != null) sql.append(" AND CategoryId = ?");
        if (keyword != null && !keyword.isEmpty()) sql.append(" AND name LIKE ?");
        
        sql.append(" ORDER BY CreatedAt DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            
            int index = 1;
            if (catId != null) pst.setString(index++, catId);
            if (keyword != null && !keyword.isEmpty()) pst.setString(index++, "%" + keyword + "%");

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setCategoryId(rs.getString("CategoryId"));
                p.setName(rs.getString("name"));
                p.setUnit(rs.getString("Unit"));
                p.setCode(rs.getString("Code"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getInt("stock"));
                p.setCreatedAt(rs.getTimestamp("CreatedAt"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. HAPUS DATA (DELETE)
    public boolean deleteBatch(List<Integer> ids) {
        String sql = "DELETE FROM tbproduct WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            for (int id : ids) {
                pst.setInt(1, id);
                pst.addBatch();
            }
            pst.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. UPDATE DATA
    public boolean update(Product p) {
        String sql = "UPDATE tbproduct SET name=?, Unit=?, price=?, stock=?, UpdatedAt=NOW() WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, p.getName());
            pst.setString(2, p.getUnit());
            pst.setDouble(3, p.getPrice());
            pst.setInt(4, p.getStock());
            pst.setInt(5, p.getId());
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean save(fafbar.model.Product p) {
        String sql = "INSERT INTO tbproduct (name, CategoryId, Code, Unit, price, stock, CreatedAt, UpdatedAt) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (java.sql.Connection conn = fafbar.config.DBConnection.getConnection();
             java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, p.getName());
            pst.setString(2, p.getCategoryId());
            pst.setString(3, p.getCode());
            pst.setString(4, p.getUnit());
            pst.setDouble(5, p.getPrice());
            pst.setInt(6, p.getStock());
            
            pst.executeUpdate();
            return true;
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}