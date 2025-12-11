package fafbar.view.sales;

import fafbar.controller.SaleController;
import fafbar.model.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import fafbar.model.Product;
import fafbar.model.User;
import fafbar.view.auth.LoginFrame;
import fafbar.view.category.CategoryFrame;
import fafbar.view.product.ProductFrame;
import fafbar.view.report.ReportFrame;
import fafbar.view.user.UserFrame;
import javax.swing.table.DefaultTableModel;
import java.io.FileNotFoundException;
import java.util.ArrayList; // Pastikan ini ada
import java.util.List; // Pastikan ini ada
import javax.swing.table.DefaultTableModel; // Pastikan ini ada
import javax.swing.JOptionPane; // Pastikan ini ada

public class SalesFrameAdmin extends javax.swing.JFrame {

    private SaleController saleController;
    private User kasir;
    private DefaultTableModel model;
    
    public SalesFrameAdmin() {
    this(new User()); // atau new User(0,"guest","Guest","cashier");
}

// CONSTRUCTOR
public SalesFrameAdmin(User kasir) {
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

            new SalesFrameAdmin(dummy).setVisible(true);
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
        sidebar1 = new javax.swing.JPanel();
        btnUser1 = new javax.swing.JButton();
        btnSales1 = new javax.swing.JButton();
        btnReport1 = new javax.swing.JButton();
        btnCategory1 = new javax.swing.JButton();
        btnProduct1 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        btnLogout6 = new javax.swing.JButton();

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
                .addGap(20, 20, 20)
                .addComponent(Jam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tanggal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 417, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 584, Short.MAX_VALUE)
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
                .addContainerGap(172, Short.MAX_VALUE))
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

        sidebar1.setBackground(new java.awt.Color(255, 153, 153));

        btnUser1.setBackground(new java.awt.Color(255, 153, 153));
        btnUser1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnUser1.setText("USER");
        btnUser1.setBorder(null);
        btnUser1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUser1ActionPerformed(evt);
            }
        });

        btnSales1.setBackground(new java.awt.Color(255, 204, 204));
        btnSales1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnSales1.setText("SALES");
        btnSales1.setBorder(null);
        btnSales1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSales1ActionPerformed(evt);
            }
        });

        btnReport1.setBackground(new java.awt.Color(255, 153, 153));
        btnReport1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnReport1.setText("REPORT");
        btnReport1.setBorder(null);
        btnReport1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReport1ActionPerformed(evt);
            }
        });

        btnCategory1.setBackground(new java.awt.Color(255, 153, 153));
        btnCategory1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnCategory1.setText("CATEGORY");
        btnCategory1.setBorder(null);
        btnCategory1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCategory1ActionPerformed(evt);
            }
        });

        btnProduct1.setBackground(new java.awt.Color(255, 153, 153));
        btnProduct1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnProduct1.setText("PRODUCT");
        btnProduct1.setBorder(null);
        btnProduct1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProduct1ActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 2, 24)); // NOI18N
        jLabel12.setText("FAFBAR");

        btnLogout6.setBackground(new java.awt.Color(255, 153, 153));
        btnLogout6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogout6.setText("LOGOUT");
        btnLogout6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogout6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sidebar1Layout = new javax.swing.GroupLayout(sidebar1);
        sidebar1.setLayout(sidebar1Layout);
        sidebar1Layout.setHorizontalGroup(
            sidebar1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebar1Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sidebar1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(sidebar1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLogout6, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(sidebar1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnProduct1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnReport1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnUser1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSales1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCategory1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(40, 40, 40))
        );
        sidebar1Layout.setVerticalGroup(
            sidebar1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sidebar1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel12)
                .addGap(45, 45, 45)
                .addComponent(btnUser1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnSales1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnReport1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnCategory1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnProduct1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout6)
                .addGap(43, 43, 43))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(sidebar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel4Layout.createSequentialGroup()
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
                            .addGap(32, 32, 32)
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
                            .addComponent(proses, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jScrollPane1)
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGap(44, 44, 44)
                                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(lblKasir)
                                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGap(47, 47, 47)
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
                            .addGap(124, 124, 124)))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
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
                        .addComponent(btnCart))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(8, 8, 8)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblKasir, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(29, 29, 29)))
                .addGap(36, 36, 36)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
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
                        .addGap(23, 23, 23)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(grandTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(32, 32, 32))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(sidebar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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
   // 1. Validasi
        if (model.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Keranjang kosong!", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Ambil nilai angka dari text field
            double dTotal = Double.parseDouble(txtTotal.getText());
            double dDisc = discount.getText().isEmpty() ? 0 : Double.parseDouble(discount.getText());
            double dGrand = Double.parseDouble(grandTotal.getText());
            double dCash = Double.parseDouble(cash.getText());
            double dChange = Double.parseDouble(change.getText());

            // Cek pembayaran
            if (dCash < dGrand) {
                javax.swing.JOptionPane.showMessageDialog(this, "Uang pembayaran kurang!", "Gagal", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Kumpulkan Data Barang dari Tabel
            java.util.List<Object[]> items = new java.util.ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                Object[] row = new Object[6];
                row[0] = model.getValueAt(i, 0); // Kode
                row[1] = model.getValueAt(i, 1); // Nama
                row[2] = model.getValueAt(i, 2); // Qty (Integer)
                row[3] = model.getValueAt(i, 3); // Harga (Double)
                row[4] = model.getValueAt(i, 4); // Disc (Double)
                row[5] = model.getValueAt(i, 5); // Total (Double)
                items.add(row);
            }

            // 3. Ambil User ID
            int userId = (kasir != null) ? kasir.getId() : 1; 
            String kasirName = (kasir != null) ? kasir.getFullName() : "Admin";

            // 4. Proses Simpan ke Database
            boolean success = saleController.processSale(
                invoice.getText(),
                txtTotal.getText(),
                discount.getText(),
                grandTotal.getText(),
                cash.getText(),
                change.getText(),
                userId,
                items
            );

            if (success) {
                // 5. CETAK STRUK (Panggil method baru yang sudah diperbaiki)
                saleController.cetakStruk(
                    invoice.getText(), 
                    kasirName, 
                    items, 
                    dTotal, dDisc, dGrand, dCash, dChange
                );

                // Reset Tampilan
                resetFrame();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi!", "Error DB", javax.swing.JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Format angka salah!", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
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

    private void btnUser1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUser1ActionPerformed
        UserFrame userFrame = new UserFrame();
        userFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnUser1ActionPerformed

    private void btnSales1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSales1ActionPerformed
        SalesFrame salesFrame = new SalesFrame();
        salesFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnSales1ActionPerformed

    private void btnReport1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReport1ActionPerformed
        ReportFrame reportFrame = new ReportFrame();
        reportFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnReport1ActionPerformed

    private void btnCategory1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCategory1ActionPerformed
        CategoryFrame categoryFrame = new CategoryFrame();
        categoryFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnCategory1ActionPerformed

    private void btnProduct1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProduct1ActionPerformed
        ProductFrame productFrame = new ProductFrame();
        productFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnProduct1ActionPerformed

    private void btnLogout6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogout6ActionPerformed
        // 1. Konfirmasi Logout
        int konfirmasi = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda yakin ingin Logout?",
            "Konfirmasi Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        // 2. Jika user memilih YES
        if (konfirmasi == JOptionPane.YES_OPTION) {
            //

            // Asumsi: Anda memiliki frame Login bernama LoginFrame
            // Ganti 'LoginFrame' sesuai nama frame Login Anda

            // 3. Tampilkan frame Login
            // Import dulu fafbar.view.Login.LoginFrame
            new LoginFrame().setVisible(true);

            // 4. Tutup frame SalesFrame saat ini
            this.dispose();
        }
    }//GEN-LAST:event_btnLogout6ActionPerformed

    
    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SalesFrameAdmin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Jam;
    private javax.swing.JTextField barcode;
    private javax.swing.JButton btnCart;
    private javax.swing.JButton btnCategory;
    private javax.swing.JButton btnCategory1;
    private javax.swing.JButton btnLogout5;
    private javax.swing.JButton btnLogout6;
    private javax.swing.JButton btnProduct;
    private javax.swing.JButton btnProduct1;
    private javax.swing.JButton btnReport;
    private javax.swing.JButton btnReport1;
    private javax.swing.JButton btnSales;
    private javax.swing.JButton btnSales1;
    private javax.swing.JButton btnUser;
    private javax.swing.JButton btnUser1;
    private javax.swing.JTextField cash;
    private javax.swing.JTextField change;
    private javax.swing.JTextField discount;
    private javax.swing.JTextField grandTotal;
    private javax.swing.JLabel invoice;
    private javax.swing.JLabel jKasir;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
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
    private javax.swing.JPanel sidebar;
    private javax.swing.JPanel sidebar1;
    private javax.swing.JTable table;
    private javax.swing.JLabel tanggak;
    private javax.swing.JLabel tanggal;
    private javax.swing.JTextField txtTotal;
    private javax.swing.JTextField txtTotal1;
    // End of variables declaration//GEN-END:variables

}
