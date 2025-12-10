
package Controller;

import Config.Koneksi;
import Model.Makanan;
import View.PilihMenu;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import javax.swing.table.DefaultTableModel;

public class MakananController {
    public Statement stm;
    public ResultSet res;
    public String sql;
    
    DefaultTableModel dtm = new DefaultTableModel();
    
    public MakananController() throws SQLException {
        this.stm = Koneksi.configDB().createStatement();
    }

        //method 1: membuat model / design tabel virtual
        public DefaultTableModel createTable(){
            this.dtm.addColumn("ID Makanan");
            this.dtm.addColumn("Nama Makanan");
            this.dtm.addColumn("Jenis");
            this.dtm.addColumn("Harga");
            this.dtm.addColumn("Stock");
            
            return this.dtm;
        }
        
public boolean updateStock(int id, int jumlah) throws SQLException {

    // Ambil stok dari DB
    String sqlGet = "SELECT stock FROM tbmakanan WHERE product_id = " + id;
    ResultSet rs = this.stm.executeQuery(sqlGet);

    int stokSekarang = 0;
    if (rs.next()) {
        stokSekarang = rs.getInt("stock");
    }

    // Validasi stok
    if (jumlah > stokSekarang) {
        javax.swing.JOptionPane.showMessageDialog(null,
             "Stock tidak cukup! Stok tersedia: " + stokSekarang);
        return false;  // ⛔ GAGAL
    }

    int stokBaru = stokSekarang - jumlah;

    // Update stok
    String sqlUpdate = "UPDATE tbmakanan SET stock = " + stokBaru +
                       " WHERE product_id = " + id;

    stm.executeUpdate(sqlUpdate);

    return true; // ✔️ BERHASIL
}

     
        public void tampilkanMakanan(){
            try {
                this.dtm.getDataVector().removeAllElements();
                this.dtm.fireTableDataChanged();
                
                // query
                this.sql = "SELECT * FROM tbmakanan";
                
                //jalankan
                this.res = this.stm.executeQuery(sql);
                
                //masukkan
                while(res.next()){
                    Object[] obj = new Object[5];
                    obj[0] = res.getString("product_id");
                    obj[1] = res.getString("name");
                    obj[2] = res.getString("category");
                    obj[3] = res.getString("price");
                    obj[4] = res.getString("stock");
                    this.dtm.addRow(obj);
                }
            } catch (Exception e) {
                System.out.println("Gagal Query"+e);
            }
        }
}
