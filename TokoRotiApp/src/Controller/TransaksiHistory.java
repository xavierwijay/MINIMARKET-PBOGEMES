package Controller;

import javax.swing.table.DefaultTableModel;
import java.util.Date;

public class TransaksiHistory {

    // model untuk tabel di KelolaPesananAdmin
    private static final DefaultTableModel HISTORI_MODEL = new DefaultTableModel(
            new Object[]{"ID", "Tanggal", "Nama Kasir", "Total", "Metode"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // admin hanya lihat
        }
    };

    private static int nextId = 1;

    public static DefaultTableModel getModel() {
        return HISTORI_MODEL;
    }

    public static void tambahTransaksi(Date tanggal, String kasir, double total, String metode) {
        if (tanggal == null) {
            tanggal = new Date();
        }
        HISTORI_MODEL.addRow(new Object[]{
                nextId++,
                new java.sql.Date(tanggal.getTime()),
                kasir,
                total,
                metode
        });
    }

    public static void hapusByRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < HISTORI_MODEL.getRowCount()) {
            HISTORI_MODEL.removeRow(rowIndex);
        }
    }
}
