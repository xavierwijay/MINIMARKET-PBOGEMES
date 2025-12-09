package fafbar.repository;

import fafbar.config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SaleRepository {
    private Connection conn;

    public SaleRepository() {
        try {
            conn = DBConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            conn = null;
        }
    }

    public String getLastInvoiceNumber() {
        String sql = "SELECT invoice_number FROM sales ORDER BY id DESC LIMIT 1";
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString("invoice_number");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  // kalau belum ada data
    }
}
