package Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static Connection mysqlconfig;
    
    public static Connection configDB() throws SQLException {
        try {
            // Ini alamat database kamu
            String url = "jdbc:mysql://localhost:3306/db_tokoroti"; 
            String user = "root"; // User default XAMPP
            String pass = "";     // Password default XAMPP (biasanya kosong)
            
            // Panggil driver MySQL yang baru (pake cj)
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            mysqlconfig = DriverManager.getConnection(url, user, pass);
            
        } catch (SQLException e) {
            System.err.println("Koneksi Gagal: " + e.getMessage());
            throw e; 
        }
        return mysqlconfig;
    }
}