package fafbar.repository;

import fafbar.config.DBConnection;
import fafbar.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    // ============================
    // Helper: mapping ResultSet → User object
    // ============================
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        // FIX: Menggunakan kolom "name" dari database, bukan "full_name"
        user.setFullName(rs.getString("name"));
        user.setRole(rs.getString("role"));
        user.setEmail(rs.getString("email"));
        user.setIsActive(rs.getBoolean("is_active"));
        user.setCreatedAt(rs.getTimestamp("created_at"));

        return user;
    }


    // ============================
    // GET ALL USERS
    // ============================
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();

        String sql = """
                SELECT id, username, password, name, role, email, is_active, created_at 
                FROM USERS
                """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                userList.add(mapRowToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getAllUsers(): " + e.getMessage());
        }

        return userList;
    }


    // ============================
    // GET USERS BY ROLE
    // ============================
    public List<User> getUsersByRole(String role) {
        List<User> userList = new ArrayList<>();

        String sql = """
                SELECT id, username, password, name, role, email, is_active, created_at
                FROM USERS
                WHERE role = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userList.add(mapRowToUser(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getUsersByRole(): " + e.getMessage());
        }

        return userList;
    }
}