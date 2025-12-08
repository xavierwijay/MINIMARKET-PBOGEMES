package Controller;

import Config.Koneksi;
import Model.Admin;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;

public class AdminController {
    private Connection conn;

    public AdminController() {
        try {
            conn = Koneksi.configDB();
        } catch (SQLException e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
        }
    }

    
    private static String esc(String s) {
        return s == null ? "" : s.replace("'", "''").trim();
    
    }
    
    public boolean cekLogin(String namaInput, String pw) {
    String n = esc(namaInput);
    String p = esc(pw);

    // HANYA admin, HANYA lewat fullname
    String sql = "SELECT 1 FROM users " +
                 "WHERE fullname = '" + n + "' " +
                 "AND password = '" + p + "' " +
                 "AND role = 'admin' " +
                 "LIMIT 1";

    try (java.sql.Statement stm = conn.createStatement();
         java.sql.ResultSet rs = stm.executeQuery(sql)) {
        return rs.next();
    } catch (java.sql.SQLException e) {
        System.out.println("Login gagal (admin): " + e.getMessage());
        return false;
    }
}

}
