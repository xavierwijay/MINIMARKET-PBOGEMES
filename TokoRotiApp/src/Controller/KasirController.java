/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;
import Config.Koneksi;
import Model.Kasir;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KasirController {
    private Connection conn;

    public KasirController() {
        try {
            conn = Koneksi.configDB();
        } catch (SQLException e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
        }
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("'", "''").trim();
    }

    // Kasir login: username + password, role dikunci 'cashier'
    public boolean cekLogin(String username, String pw) {
        String u = esc(username);
        String p = esc(pw);

        String sql = "SELECT 1 FROM users " + "WHERE username = '" + u + "' " + "AND password = '" + p + "' " + "AND role = 'cashier' " + "LIMIT 1";

        try (Statement stm = conn.createStatement();
             ResultSet rs  = stm.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Login kasir gagal: " + e.getMessage());
            return false;
        }
    }
}
