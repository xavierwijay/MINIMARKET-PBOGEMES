package fafbar.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    // Sesuaikan dengan setting database kamu
    private static final String URL = "jdbc:mysql://localhost:3306/fafbar"; 
    private static final String USER = "root"; 
    private static final String PASS = ""; 

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Register Driver (Opsional di Java baru, tapi bagus untuk memastikan)
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // SELALU BUAT KONEKSI BARU (Agar tidak mati saat di-close oleh Controller)
            conn = DriverManager.getConnection(URL, USER, PASS);
            
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Koneksi Gagal: " + e.getMessage());
        }
        return conn;
    }
}