package Config;

import java.sql.Connection;

public class TesKoneksi {
    public static void main(String[] args) {
        try {
            Connection c = Koneksi.configDB();
            if (c != null) {
                System.out.println("---------------------------------------------");
                System.out.println("✅ MANTAP! KONEKSI KE DATABASE BERHASIL!");
                System.out.println("---------------------------------------------");
            }
        } catch (Exception e) {
            System.out.println("---------------------------------------------");
            System.out.println("❌ WADUH GAGAL! Cek errornya di bawah ini:");
            System.err.println(e.getMessage());
            System.out.println("---------------------------------------------");
        }
    }
}