package fafbar.view.sales;

import fafbar.controller.SaleController;
import fafbar.model.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import fafbar.model.Product;
import fafbar.model.User;
import javax.swing.table.DefaultTableModel;
import java.io.FileNotFoundException;
import java.util.ArrayList; // Pastikan ini ada
import java.util.List; // Pastikan ini ada
import javax.swing.table.DefaultTableModel; // Pastikan ini ada
import javax.swing.JOptionPane; // Pastikan ini ada

public class SalesFrame extends javax.swing.JFrame {

    private SaleController saleController;
    private User kasir;
    private DefaultTableModel model;
    
    public SalesFrame() {
    this(new User()); // atau new User(0,"guest","Guest","cashier");
}

// CONSTRUCTOR
public SalesFrame(User kasir) {
    initComponents();
    setLocationRelativeTo(null);

    this.kasir = kasir;
    this.saleController = new SaleController();
    this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

    tampilkanNamaKasir();
    tampilkanInvoiceBaru();
    startClock();

    model = (DefaultTableModel) table.getModel();
    
    model.setRowCount(0);

    // Mengunci semua field total agar tidak bisa diedit
    txtTotal.setEditable(false);
    txtTotal1.setEditable(false);
    grandTotal.setEditable(false); // Pastikan ini juga dikunci
    change.setEditable(false);     // Pastikan ini juga dikunci

    // -----------------------------------------------------------------
    // PERBAIKAN KRITIS: Listener Kolom Qty Tabel (menggunakan DOUBLE)
    // -----------------------------------------------------------------
    // HAPUS KODE LAMA, GANTI DENGAN INI:
    model.addTableModelListener(e -> {
        int row = e.getFirstRow();
        int col = e.getColumn();

        // PENTING: Kita cek apakah yang berubah Kolom Qty (2) ATAU Kolom Disc Item (4)
        if ((col == 2 || col == 4) && row >= 0) {
            try {
                // 1. Ambil Qty (Default 0 jika kosong/error)
                Object qtyObj = model.getValueAt(row, 2);
                int qty = (qtyObj == null || qtyObj.toString().isEmpty()) ? 0 : Integer.parseInt(qtyObj.toString());

                // 2. Ambil Harga (Default 0.0)
                Object priceObj = model.getValueAt(row, 3);
                double harga = (priceObj == null || priceObj.toString().isEmpty()) ? 0.0 : Double.parseDouble(priceObj.toString());

                // 3. Ambil Disc Item (Kolom 4) - Ini yang baru!
                Object discObj = model.getValueAt(row, 4);
                double discItem = 0.0;
                if (discObj != null && !discObj.toString().isEmpty()) {
                    discItem = Double.parseDouble(discObj.toString());
                }

                // 4. Rumus: (Qty * Harga) - Diskon Item
                double subtotal = (qty * harga) - discItem;

                // Validasi: Jangan biarkan Total minus
                if (subtotal < 0) {
                    subtotal = 0;
                    JOptionPane.showMessageDialog(this, "Diskon tidak boleh melebihi harga total barang!");
                }

                // 5. Update Kolom Total (Index 5)
                // Kita pakai invokeLater supaya update UI tidak bentrok dengan listener yang sedang berjalan
                final double finalSubtotal = subtotal;
                javax.swing.SwingUtilities.invokeLater(() -> {
                    model.setValueAt(finalSubtotal, row, 5);
                    hitungTotal(); // Update Total Besar di bawah
                });

            } catch (NumberFormatException ex) {
                // Error diam saja saat ngetik, nanti divalidasi pas Enter
            } catch (Exception ex) {
                System.err.println("Error hitung tabel: " + ex.getMessage());
            }
        }
    });
    }

    // Ini adalah method tambahItemDariInput() di SalesFrame.java (ASUMSI)

    // di dalam private void tambahItemDariInput() {...
    private void tambahItemDariInput() {
        try {
            String input = barcode.getText().trim(); 
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan kode/barcode atau nama produk!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int qtyVal = Integer.parseInt(qty.getValue().toString()); 
            Product produk = saleController.cariProdukByKodeAtauNama(input); 

            if (produk == null) {
                JOptionPane.showMessageDialog(this, "Produk dengan kode/nama '" + input + "' tidak ditemukan!", "Produk Tidak Ditemukan", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ===============================================
            // --- LOGIKA BARU: VALIDASI STOK CUKUP ---
            // ===============================================

            // 1. Hitung total Qty yang akan diminta (Qty dari input + Qty yang sudah ada di tabel)
            int qtySudahDiKeranjang = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0).toString().equals(produk.getCode())) {
                    qtySudahDiKeranjang = Integer.parseInt(model.getValueAt(i, 2).toString());
                    break;
                }
            }
            int totalQtyDiminta = qtyVal + qtySudahDiKeranjang;

            // 2. Bandingkan dengan stok produk di DB (produk.getStock())
            if (totalQtyDiminta > produk.getStock()) {
                // Tampilkan pesan jika stok tidak cukup
                JOptionPane.showMessageDialog(this, 
                    "Stok '" + produk.getName() + "' tidak cukup.\nStok tersedia: " + produk.getStock() + " (sudah termasuk yang di keranjang: " + qtySudahDiKeranjang + ")", 
                    "Stok Kurang", JOptionPane.WARNING_MESSAGE);
                return; // Hentikan proses penambahan item
            }

            // ===============================================
            // --- END LOGIKA BARU ---
            // ===============================================

            // Jika stok cukup, lanjutkan menambah item ke tabel
            tambahItemKeTabel(produk.getCode(), produk.getName(), qtyVal, produk.getPrice()); 

            // reset input
            barcode.setText("");
            qty.setValue(1);
            barcode.requestFocus(); 

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Kuantitas (Qty) harus berupa angka yang valid.", "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            System.err.println("Error saat memproses input item: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan sistem: " + ex.getMessage(), "Error Sistem", JOptionPane.ERROR_MESSAGE);
        }
    }
    // ...
    private void tambahItemKeTabel(String kode, String nama, int qtyInput, double harga) {
        boolean produkSudahAda = false;
        int barisDitemukan = -1;

        // 1. Cek apakah kode barang sudah ada di tabel?
        for (int i = 0; i < model.getRowCount(); i++) {
            String kodeDiTabel = model.getValueAt(i, 0).toString(); // Ambil kode dari kolom 0

            if (kodeDiTabel.equals(kode)) {
                produkSudahAda = true;
                barisDitemukan = i;
                break; // Berhenti looping kalau sudah ketemu
            }
        }

        if (produkSudahAda) {
            // 2. Kalau ada, update Qty dan Total-nya saja
            int qtyLama = Integer.parseInt(model.getValueAt(barisDitemukan, 2).toString());
            int qtyBaru = qtyLama + qtyInput;
            double subtotalBaru = qtyBaru * harga;

            model.setValueAt(qtyBaru, barisDitemukan, 2); // Update Kolom Qty
            model.setValueAt(subtotalBaru, barisDitemukan, 5); // Update Kolom Total
        } else {
            // 3. Kalau belum ada, tambahkan baris baru
            double subtotal = qtyInput * harga;
            model.addRow(new Object[]{
                kode, 
                nama, 
                qtyInput, 
                harga, 
                0.0, // Diskon item 0
                subtotal
            });
        }

        // Hitung ulang total belanjaan
        hitungTotal();
    }

    private void hitungTotal() {
        double subtotal = 0.0; 

        for (int i = 0; i < model.getRowCount(); i++) {
            // Kolom 5 adalah kolom "Total" item (Harga per item * Qty - Disc Item)
            // Ini adalah subtotal per item
            double subtotalItem = Double.parseDouble(model.getValueAt(i, 5).toString());
            subtotal += subtotalItem;
        }

        // txtTotal1: Total di bagian bawah (kita anggap ini Subtotal)
        txtTotal1.setText(String.valueOf(subtotal)); 

        // Panggil hitungGrandTotal() untuk mengupdate kotak merah atas
        hitungGrandTotal(); 
    }

     private void tampilkanNamaKasir() {
    // Cek dulu apakah objek kasir ada isinya (biar gak error NullPointer)
    if (this.kasir != null) {
        
        // 1. Tampilkan di Kotak Input (TextField)
        lblKasir.setText(this.kasir.getFullName());
        
        // 2. Kunci kotak input biar gak bisa diedit manual
        lblKasir.setEditable(false); 
        
        // 3. Tampilkan juga di Header Kanan Atas (Yang tulisan besar)
        // Pastikan variabelnya benar (di form kamu namanya jKasir)
        jKasir.setText("Kasir: " + this.kasir.getFullName());
    }
}
    
    private void tampilkanInvoiceBaru() {
        String invoiceNumber = saleController.generateInvoice();
        invoice.setText(invoiceNumber);
    }

    
    private void startClock() {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

        Timer timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            Jam.setText(now.format(timeFmt));
            tanggal.setText(now.format(dateFmt));
            tanggak.setText(now.format(dateFmt));
            
        });

        timer.start();
    }
    private void hitungGrandTotal() {
    try {
        // 1. Ambil Nilai Subtotal (dari txtTotal1)
        double subtotal = Double.parseDouble(txtTotal1.getText()); // Ambil Subtotal
        
        // 2. Ambil Nilai Diskon Global
        String discText = discount.getText().trim().isEmpty() ? "0" : discount.getText();
        double discVal = Double.parseDouble(discText);
        
        // 3. Hitung Grand Total
        double grandTotalVal = subtotal - discVal;
        if (grandTotalVal < 0) {
            grandTotalVal = 0;
            // Ini akan memastikan Grand Total tidak negatif
        }
        
        // Output 1: Update field Grand Total (Bawah)
        grandTotal.setText(String.valueOf(grandTotalVal));

        // Output 2: Update Kotak Merah Besar (Atas)
        txtTotal.setText(String.valueOf(grandTotalVal)); // INI ADALAH FIX UNTUK KOTAK MERAH

        // 4. Ambil Nilai Cash (Uang Tunai)
        String cashText = cash.getText().trim().isEmpty() ? "0" : cash.getText();
        double cashVal = Double.parseDouble(cashText);
        
        // 5. Hitung Kembalian (Change)
        double changeVal = cashVal - grandTotalVal;
        
        change.setText(String.valueOf(changeVal));

        } catch (NumberFormatException e) {
            // Ini terjadi jika user menginput huruf di diskon/cash
            // Opsional: Anda bisa set Grand Total dan Change ke 0 saat error input
        }
}
    

        private void resetFrame() {
        // 1. Kosongkan Tabel
        model.setRowCount(0);

        // 2. Reset Input
        barcode.setText("");
        qty.setValue(1);
        discount.setText("0");
        cash.setText("0");

        // 3. Reset Total/Grand Total
        txtTotal.setText("0");
        txtTotal1.setText("0");
        grandTotal.setText("0");
        change.setText("0");

        // 4. Generate Invoice Baru
        tampilkanInvoiceBaru(); 
    }
    // Hubungkan tombol refresh ke resetFrame


 public class MainApp {

    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(() -> {

            // Dummy user untuk testing tampilan
            User dummy = new User();
            dummy.setFullName("Kasir Demo");

            new SalesFrame(dummy).setVisible(true);
        });
    }
}




    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jKasir = new javax.swing.JLabel();
        Jam = new javax.swing.JLabel();
        tanggal = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        lblKasir = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        barcode = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        qty = new javax.swing.JSpinner();
        jPanel1 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        invoice = new javax.swing.JLabel();
        txtTotal = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtTotal1 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        grandTotal = new javax.swing.JTextField();
        discount = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        cash = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        change = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        tanggak = new javax.swing.JLabel();
        search = new javax.swing.JButton();
        btnCart = new javax.swing.JButton();
        refresh = new javax.swing.JButton();
        proses = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel4.setBackground(new java.awt.Color(255, 204, 204));

        jPanel2.setBackground(new java.awt.Color(255, 204, 204));

        jKasir.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jKasir.setText("Kasir 1, Putri");

        Jam.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        Jam.setText("jLabel7");

        tanggal.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        tanggal.setText("jLabel7");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("|");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel8.setText("FAFBAR");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addComponent(Jam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tanggal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 391, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 565, Short.MAX_VALUE)
                .addComponent(jKasir)
                .addGap(36, 36, 36))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Jam)
                    .addComponent(tanggal)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jKasir))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jScrollPane1.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                jScrollPane1AncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID Barang", "Nama Barang", "Qty", "Harga", "Disc Item", "Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                tableAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        jScrollPane1.setViewportView(table);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Kasir");

        lblKasir.setForeground(new java.awt.Color(102, 102, 102));
        lblKasir.setText("Cari produk disini ...");
        lblKasir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lblKasirActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Date");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel4.setText("Penjualan Barang");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Barcode");

        barcode.setForeground(new java.awt.Color(102, 102, 102));
        barcode.setText("Cari produk disini ...");
        barcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                barcodeActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("Qty");

        jPanel1.setBackground(new java.awt.Color(255, 153, 153));

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel25.setText("No. Invoice");

        invoice.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        invoice.setText("152638324");

        txtTotal.setEditable(false);
        txtTotal.setBackground(new java.awt.Color(255, 153, 153));
        txtTotal.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        txtTotal.setForeground(new java.awt.Color(255, 255, 255));
        txtTotal.setText("Total");
        txtTotal.setBorder(null);
        txtTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(193, Short.MAX_VALUE)
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(txtTotal))
                    .addComponent(invoice))
                .addGap(30, 30, 30))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(invoice))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtTotal)
                .addGap(13, 13, 13))
        );

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel14.setText("Total");

        txtTotal1.setEditable(false);
        txtTotal1.setBackground(new java.awt.Color(255, 255, 255));
        txtTotal1.setForeground(new java.awt.Color(102, 102, 102));
        txtTotal1.setText("0");
        txtTotal1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotal1ActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setText("Discount");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel16.setText("Grand Total");

        grandTotal.setForeground(new java.awt.Color(102, 102, 102));
        grandTotal.setText("0");
        grandTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grandTotalActionPerformed(evt);
            }
        });

        discount.setForeground(new java.awt.Color(102, 102, 102));
        discount.setText("0");
        discount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discountActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setText("Cash");

        cash.setForeground(new java.awt.Color(102, 102, 102));
        cash.setText("0");
        cash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cashActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel18.setText("Change");

        change.setForeground(new java.awt.Color(102, 102, 102));
        change.setText("0");
        change.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeActionPerformed(evt);
            }
        });

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        tanggak.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        tanggak.setText("jLabel20");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tanggak)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tanggak, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                .addContainerGap())
        );

        search.setText("search");
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });

        btnCart.setText("cart");
        btnCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCartActionPerformed(evt);
            }
        });

        refresh.setBackground(new java.awt.Color(255, 153, 153));
        refresh.setText("REFRESH");
        refresh.setBorder(null);
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });

        proses.setBackground(new java.awt.Color(255, 153, 153));
        proses.setText("PROSES");
        proses.setBorder(null);
        proses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prosesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(80, 80, 80)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel15)
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel14)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtTotal1)
                                    .addComponent(discount)
                                    .addComponent(grandTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(135, 135, 135)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel18)
                                    .addComponent(jLabel17))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cash)
                                    .addComponent(change, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(55, 55, 55)
                                .addComponent(refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(42, 42, 42)
                                .addComponent(proses, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(44, 44, 44)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblKasir, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10))
                                .addGap(44, 44, 44)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnCart)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(qty, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(barcode, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(search)))
                                .addGap(58, 58, 58)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(157, 157, 157))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(157, 157, 157))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(8, 8, 8)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3))
                            .addGap(20, 20, 20)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblKasir, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2)))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(barcode, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel9))
                            .addGap(20, 20, 20)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(qty, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel10))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnCart)))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTotal1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(discount, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(cash, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel17))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(change, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(proses, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(grandTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(21, 21, 21))
        );

        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblKasirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lblKasirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lblKasirActionPerformed

    private void barcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barcodeActionPerformed
        tambahItemDariInput();
    }//GEN-LAST:event_barcodeActionPerformed

    private void txtTotal1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotal1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotal1ActionPerformed

    private void grandTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grandTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_grandTotalActionPerformed

    private void discountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discountActionPerformed
        hitungGrandTotal();
    }//GEN-LAST:event_discountActionPerformed

    private void cashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cashActionPerformed
        hitungGrandTotal();
    }//GEN-LAST:event_cashActionPerformed

    private void changeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_changeActionPerformed

    private void tableAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tableAncestorAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_tableAncestorAdded

    private void jScrollPane1AncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_jScrollPane1AncestorAdded

 
    }//GEN-LAST:event_jScrollPane1AncestorAdded

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
       JOptionPane.showMessageDialog(this, 
                "Fungsi Pencarian Produk Belum Diimplementasikan. Silakan Gunakan Input Barcode.", 
                "Fitur Belum Tersedia", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_searchActionPerformed

    private void txtTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalActionPerformed

    private void prosesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prosesActionPerformed
     // 1. Validasi Sederhana
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang belanja masih kosong!", "Gagal Proses", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        double changeVal = Double.parseDouble(change.getText());
        if (changeVal < 0) {
             JOptionPane.showMessageDialog(this, "Uang tunai kurang! Kembalian tidak boleh negatif.", "Gagal Proses", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ==========================================================
        // 2. Kumpulkan Semua Data Transaksi (CRITICAL STEP)
        // ==========================================================
        List<Object[]> itemsData = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object[] rowData = new Object[6]; // ID Barang, Nama, Qty, Harga, Disc Item, Total
            for (int j = 0; j < 6; j++) {
                rowData[j] = model.getValueAt(i, j);
            }
            itemsData.add(rowData);
        }

        String invoiceNum = invoice.getText();
        String kasirName = lblKasir.getText(); 
        double subTotal = Double.parseDouble(txtTotal1.getText()); // Total (sebelum diskon)
        double discountVal = Double.parseDouble(discount.getText());
        double grandTotalVal = Double.parseDouble(grandTotal.getText());
        double cashVal = Double.parseDouble(cash.getText());
        double changeResult = changeVal;
        
        // 3. SIMULASI Simpan Transaksi ke DB (Ganti dengan implementasi saveTransaction)
     boolean isSaleSaved = true; // Ganti dengan pemanggilan ke Repository
     
     if (isSaleSaved) {
         // 4. CETAK STRUK PDF (Sekarang juga berfungsi membuka file secara otomatis)
         String pdfFileName = saleController.cetakStrukPDF(
             invoiceNum, 
             kasirName, 
             itemsData, 
             subTotal, 
             discountVal, 
             grandTotalVal, 
             cashVal, 
             changeResult
         );
         
         if (pdfFileName != null) {
             // 4.1. PDF berhasil dibuat dan DIBUKA SECARA OTOMATIS (karena perubahan di Controller)
             
             // UBAH: Ganti pop-up 'Berhasil dibuat' dengan notifikasi yang lebih cepat/minimal
             // Atau, HILANGKAN SAMA SEKALI, karena user sudah melihat PDF terbuka.
             
             // Pilihan A: Hilangkan pop-up sama sekali, langsung reset
    
             resetFrame(); 
             
         } else {
             // Gagal mencetak PDF
             JOptionPane.showMessageDialog(this, 
                     "Gagal membuat file struk PDF. Cek console untuk error.", 
                     "Error PDF", 
                     JOptionPane.ERROR_MESSAGE);
         }
     } else {
         JOptionPane.showMessageDialog(this, "Gagal menyimpan data transaksi ke database.", "Error Database", JOptionPane.ERROR_MESSAGE);
     }
    }//GEN-LAST:event_prosesActionPerformed

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
        resetFrame();
    }//GEN-LAST:event_refreshActionPerformed

    private void btnCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCartActionPerformed
        tambahItemDariInput();
    }//GEN-LAST:event_btnCartActionPerformed

    private void cartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

    }//GEN-LAST:event_jButton2ActionPerformed

    
    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SalesFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Jam;
    private javax.swing.JTextField barcode;
    private javax.swing.JButton btnCart;
    private javax.swing.JTextField cash;
    private javax.swing.JTextField change;
    private javax.swing.JTextField discount;
    private javax.swing.JTextField grandTotal;
    private javax.swing.JLabel invoice;
    private javax.swing.JLabel jKasir;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField lblKasir;
    private javax.swing.JButton proses;
    private javax.swing.JSpinner qty;
    private javax.swing.JButton refresh;
    private javax.swing.JButton search;
    private javax.swing.JTable table;
    private javax.swing.JLabel tanggak;
    private javax.swing.JLabel tanggal;
    private javax.swing.JTextField txtTotal;
    private javax.swing.JTextField txtTotal1;
    // End of variables declaration//GEN-END:variables

}
