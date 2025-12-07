
package Controller;

import Config.Koneksi;
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
            
            return this.dtm;
        }
        
        //method 2
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
                    Object[] obj = new Object[4];
                    obj[0] = res.getString("id");
                    obj[1] = res.getString("nama");
                    obj[2] = res.getString("jenis");
                    obj[3] = res.getString("harga");
                    this.dtm.addRow(obj);
                }
            } catch (Exception e) {
                System.out.println("Gagal Query"+e);
            }
        }
}
