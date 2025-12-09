package fafbar.controller;

import fafbar.config.DBConnection;
import fafbar.config.Session;
import fafbar.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthController {

    public static boolean login(String email, String password) {

        String sql = """
            SELECT id, username, full_name, role, email 
            FROM USERS
            WHERE email = ? AND password = ?
            AND is_active = TRUE
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRole(rs.getString("role"));
                    user.setEmail(rs.getString("email"));

                    Session.setCurrentUser(user);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Database Error during login:");
            e.printStackTrace();
        }
        return false;
    }

    public static void logout() {
        Session.clear();
    }
}