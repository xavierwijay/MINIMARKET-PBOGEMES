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
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.element.Paragraph;
//import com.itextpdf.layout.element.Table;
//import com.itextpdf.layout.properties.TextAlignment;
import java.io.FileNotFoundException;

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
    this.setSize(1366, 768);

    tampilkanNamaKasir();
    tampilkanInvoiceBaru();
    startClock();

    model = (DefaultTableModel) table.getModel();

    // Mengunci semua field total agar tidak bisa diedit
    txtTotal.setEditable(false);
    txtTotal1.setEditable(false);
    grandTotal.setEditable(false); // Pastikan ini juga dikunci
    change.setEditable(false);     // Pastikan ini juga dikunci

    // -----------------------------------------------------------------
    // PERBAIKAN KRITIS: Listener Kolom Qty Tabel (menggunakan DOUBLE)
    // -----------------------------------------------------------------
    model.addTableModelListener(e -> {
        int row = e.getFirstRow();
        int col = e.getColumn();

        // Kolom Qty adalah indeks 2
        if (col == 2 && row >= 0) {
            try {
                // Ambil Qty sebagai Integer
                int qty = Integer.parseInt(model.getValueAt(row, 2).toString());
                
                // Ambil Harga sebagai Double (indeks 3)
                double harga = Double.parseDouble(model.getValueAt(row, 3).toString());
                
                // Ambil Disc Item sebagai Double (indeks 4)
                double discItem = Double.parseDouble(model.getValueAt(row, 4).toString());
                
                // Hitung Subtotal sebagai Double: (Qty * Harga) - Disc Item
                double subtotal = (qty * harga) - discItem;

                // Set nilai subtotal baru di kolom Total (indeks 5)
                model.setValueAt(subtotal, row, 5); 
                
                hitungTotal(); 
                
            } catch (NumberFormatException ex) {
                // Peringatan jika input Qty bukan angka
                JOptionPane.showMessageDialog(this, "Input Qty harus berupa angka yang valid.", "Error Input Qty", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                // Peringatan jika ada error lain (misal: data harga di tabel corrupted)
                System.err.println("Error saat menghitung ulang total item di tabel: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat kalkulasi tabel: " + ex.getMessage(), "Error Sistem", JOptionPane.ERROR_MESSAGE);
            }
        }
    });
    
    // Listener tombol CART (jButton2)
    btnCart.addActionListener(e -> tambahItemDariInput());
    
    // Listener untuk field Barcode (saat user tekan Enter)
    barcode.addActionListener(e -> tambahItemDariInput());
    
    // Listener untuk Diskon dan Cash (saat user tekan Enter)
    discount.addActionListener(e -> hitungGrandTotal());
    cash.addActionListener(e -> hitungGrandTotal());
    
    // Listener tombol Refresh (pastikan sudah terhubung ke method)
    refresh.addActionListener(e -> resetFrame());
}

// Ini adalah method tambahItemDariInput() di SalesFrame.java (ASUMSI)

private void tambahItemDariInput() {
    try {
        String input = barcode.getText().trim(); 

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan kode/barcode atau nama produk!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // FIX: Pastikan ini dikonversi dengan aman (try-catch sudah ada)
        int qtyVal = Integer.parseInt(qty.getValue().toString()); 

        Product produk = saleController.cariProdukByKodeAtauNama(input); 

        if (produk == null) {
            JOptionPane.showMessageDialog(this, "Produk dengan kode/nama '" + input + "' tidak ditemukan!", "Produk Tidak Ditemukan", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // FIX: Menggunakan getCode() dan getName() (bukan getKode dan getFullName)
        // Pastikan Model Product punya setUnit(String)
        tambahItemKeTabel(produk.getCode(), produk.getName(), qtyVal, produk.getPrice()); 

        // reset input
        barcode.setText("");
        qty.setValue(1);
        
        // FIX: Kembalikan fokus kursor ke field barcode
        barcode.requestFocus(); 

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Kuantitas (Qty) harus berupa angka yang valid.", "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
        System.err.println("Error saat memproses input item: " + ex.getMessage());
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan sistem: " + ex.getMessage(), "Error Sistem", JOptionPane.ERROR_MESSAGE);
    }
}

private void tambahItemKeTabel(String kode, String nama, int qty, double harga) {
    double subtotal = qty * harga; 

    model.addRow(new Object[]{
        kode, 
        nama, 
        qty, 
        harga, // <-- Sekarang Double
        // Kolom 'Disc Item' (indeks 4) diabaikan/diasumsikan 0 jika tidak ada diskon per item
        0.0, 
        subtotal // <-- Sekarang Double (Ini adalah kolom "Total" di JTable)
    });

    hitungTotal();
}


private void hitungTotal() {
    double total = 0.0; 

    for (int i = 0; i < model.getRowCount(); i++) {
        
        double subtotal = Double.parseDouble(model.getValueAt(i, 5).toString());
        total += subtotal;
    }

   
    txtTotal.setText(String.valueOf(total));
    txtTotal1.setText(String.valueOf(total));
    
    
    hitungGrandTotal(); 
}

     private void tampilkanNamaKasir() {
        lblKasir.setText(kasir.getFullName());
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
        // 1. Ambil Nilai Total
        double total = Double.parseDouble(txtTotal.getText());
        
        // 2. Ambil Nilai Diskon
        String discText = discount.getText().trim().isEmpty() ? "0" : discount.getText();
        double discVal = Double.parseDouble(discText);
        
        // 3. Hitung Grand Total
        double grandTotalVal = total - discVal;
        if (grandTotalVal < 0) {
            grandTotalVal = 0;
            // Opsi: Beri peringatan jika diskon terlalu besar
        }
        
        grandTotal.setText(String.valueOf(grandTotalVal));

        // 4. Ambil Nilai Cash (Uang Tunai)
        String cashText = cash.getText().trim().isEmpty() ? "0" : cash.getText();
        double cashVal = Double.parseDouble(cashText);
        
        // 5. Hitung Kembalian (Change)
        double changeVal = cashVal - grandTotalVal;
        
        change.setText(String.valueOf(changeVal));

        } catch (NumberFormatException e) {
            // Ini terjadi jika user menginput huruf di diskon/cash
            JOptionPane.showMessageDialog(this, "Input Total, Diskon, atau Cash harus berupa angka.", "Error Input", JOptionPane.ERROR_MESSAGE);
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
                .addGap(473, 473, 473)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jKasir)
                .addGap(208, 208, 208))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(28, 28, 28)
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
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID Barang", "Nama Barang", "Qty", "Harga", "Disc Item", "Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, false, false, false
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
                .addContainerGap(233, Short.MAX_VALUE)
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
                .addGap(45, 45, 45)
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
                        .addComponent(proses, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addGroup(jPanel4Layout.createSequentialGroup()
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(44, 44, 44)
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lblKasir, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                                            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(55, 55, 55)
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel9)
                                            .addComponent(jLabel10))
                                        .addGap(44, 44, 44)
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(qty)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(barcode, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(search))
                                            .addGroup(jPanel4Layout.createSequentialGroup()
                                                .addComponent(btnCart)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 269, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGap(47, 47, 47)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(222, 222, 222))))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(20, 20, 20)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
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
                                    .addComponent(jLabel10))))
                        .addGap(23, 23, 23)
                        .addComponent(btnCart)))
                .addGap(36, 36, 36)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 1381, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

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
