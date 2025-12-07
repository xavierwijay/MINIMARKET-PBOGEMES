package Controller;

import Model.KeranjangPemesanan; 
import Model.Product;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class KeranjangController {
    private static KeranjangController instance;

    private final List<KeranjangPemesanan> items = new ArrayList<>();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Nama", "Harga", "Jumlah", "Subtotal"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            if (c == 1 || c == 3) return Double.class; // Harga, Subtotal
            if (c == 2) return Integer.class;         // Jumlah
            return String.class;                      // Nama
        }
    };

    private KeranjangController() {}

    public static KeranjangController getInstance() {
        if (instance == null) instance = new KeranjangController();
        return instance;
    }

    public DefaultTableModel getTableModel() { return tableModel; }
    public List<KeranjangPemesanan> getItems() { return items; }

    public void tambahItem(Product p, int jumlahPesanan) {
        if (p == null || jumlahPesanan <= 0) return;
        for (KeranjangPemesanan ki : items) {
            if (ki.getProduct().getId() == p.getId()) {
                ki.setJumlahPesanan(ki.getJumlahPesanan() + jumlahPesanan);
                tampilkanTable();
                return;
            }
        }
        items.add(new KeranjangPemesanan(p, jumlahPesanan));
        tampilkanTable();
    }

    public void hapusItem(int row) {
        if (row >= 0 && row < items.size()) {
            items.remove(row);
            tampilkanTable();
        }
    }

    public void clear() {
        items.clear();
        tampilkanTable();
    }

    public double getTotal() {
        double total = 0.0;
        for (KeranjangPemesanan ki : items) total += ki.getSubtotal();
        return total;
    }

    private void tampilkanTable() {
        tableModel.setRowCount(0);
        for (KeranjangPemesanan ki : items) {
            tableModel.addRow(new Object[]{
                ki.getProduct().getName(),
                ki.getProduct().getPrice(),
                ki.getJumlahPesanan(),
                ki.getSubtotal()
            });
        }
    }
}
