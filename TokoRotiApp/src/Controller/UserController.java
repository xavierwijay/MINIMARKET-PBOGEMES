package Controller;

import Config.Koneksi;
import Model.User;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement; 

public class UserController {
    private Connection conn;

    public UserController() {
        try {
            conn = Koneksi.configDB();
        } catch (SQLException e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
        }
    }
    
    public boolean cekLogin(String username, String pw) {
        String u = esc(username);
        String p = esc(pw);
        String sql = "SELECT 1 FROM users " + "WHERE username = '" + u + "' " + "AND password = '" + p + "' " + "AND role = 'customer' " + "LIMIT 1";
        try (Statement stm = conn.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Login gagal (user): " + e.getMessage());
            return false;
        }
    }

    public boolean cekUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username.trim());
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next(); // true kalau sudah ada
            }
        } catch (SQLException e) {
            System.out.println("Cek username gagal: " + e.getMessage());
            return true; // anggap sudah ada supaya aman
        }
    }

    public boolean cekFullname(String fullname) {
        String sql = "SELECT 1 FROM users WHERE fullname = ? AND role = 'customer' LIMIT 1";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, fullname.trim());
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Cek fullname gagal: " + e.getMessage());
            return true;
        }
    }

    public boolean buatAkunUser(String username, String fullname, String password) {
        String sql = "INSERT INTO users (username, password, fullname, role) " + "VALUES (?, ?, ?, 'customer')";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username.trim());
            pst.setString(2, password);
            pst.setString(3, fullname.trim());
            return pst.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println("Buat akun user gagal: " + e.getMessage());
            return false;
        }
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("'", "''").trim();
    }
}