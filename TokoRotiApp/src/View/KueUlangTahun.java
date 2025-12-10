/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Controller.KeranjangController;
import Controller.ProductController;
import Model.Product;
import java.awt.Component;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author LENOVO
 */
public class KueUlangTahun extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(KueUlangTahun.class.getName());
    private final ProductController productController = new ProductController();

    
    public KueUlangTahun() {
        initComponents();  // panggil komponen bawaan NetBeans
        setupDynamicLayout(); // isi panel produk (pnlDynamic)
    }
    
    

    private void setupDynamicLayout() {
        // Ambil data produk dari kategori KUE ULANG TAHUN
        java.util.List<Model.Product> items =productController.ambilByKategori("KUE ULANG TAHUN");

        // Bersihkan isi lama panel
        pnlDynamic.removeAll();

        // Warna latar belakang
        java.awt.Color coklat = new java.awt.Color(209, 186, 155);
        pnlDynamic.setBackground(coklat);
        pnlDynamic.setLayout(new java.awt.BorderLayout());

        // === GRID PRODUK ===
        javax.swing.JPanel grid = new javax.swing.JPanel(new java.awt.GridLayout(0, 4, 25, 25));
        grid.setOpaque(false);
        for (Model.Product p : items) {
            grid.add(buatCardProduk(p));
        }

        // === PEMBUNGKUS SCROLL ===
        javax.swing.JPanel wrapper = new javax.swing.JPanel(new java.awt.BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(
                javax.swing.BorderFactory.createEmptyBorder(10, 25, 20, 10)
        );
        wrapper.add(grid, java.awt.BorderLayout.CENTER);

        // === SCROLLPANE ===
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setVerticalScrollBarPolicy(
                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        );
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(coklat);
        scroll.setBackground(coklat);

        // Tambahkan scroll ke panel dinamis
        pnlDynamic.add(scroll, java.awt.BorderLayout.CENTER);

        // Batasi tinggi supaya tidak nutup tulisan atas
        pnlDynamic.setPreferredSize(new java.awt.Dimension(1000, 480));

        // Refresh tampilan
        pnlDynamic.revalidate();
        pnlDynamic.repaint();
    }

    //  RESOLVER GAMBAR
    private java.io.File resolveImageFile(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return null;

        java.io.File f = new java.io.File(imagePath);
        if (f.exists()) return f;

        String p = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;

        f = new java.io.File("src/" + p);
        if (f.exists()) return f;

        f = new java.io.File("src/View/" + p);
        if (f.exists()) return f;

        f = new java.io.File("build/classes/" + p);
        if (f.exists()) return f;

        return null;
    }

    
    //  CARD PRODUK

    private javax.swing.JPanel buatCardProduk(Model.Product p) {
        javax.swing.JPanel card = new javax.swing.JPanel(new java.awt.BorderLayout());
        card.setBackground(java.awt.Color.WHITE);
        card.setPreferredSize(new java.awt.Dimension(210, 300));
        card.setMaximumSize(new java.awt.Dimension(210, 300));

        // --------- ATAS: GAMBAR ----------
        javax.swing.JLabel img = new javax.swing.JLabel();
        img.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        img.setVerticalAlignment(javax.swing.SwingConstants.CENTER);

        int targetW = 150;
        int targetH = 140;

        java.io.File f = resolveImageFile(p.getImagePath());
        if (f != null) {
            try {
                java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(f);
                if (bi != null) {
                    java.awt.Image scaled =
                            bi.getScaledInstance(targetW, targetH, java.awt.Image.SCALE_SMOOTH);
                    img.setIcon(new javax.swing.ImageIcon(scaled));
                } else {
                    img.setText("No Image");
                }
            } catch (Exception e) {
                img.setText("No Image");
            }
        } else {
            img.setText("No Image");
        }

        javax.swing.JPanel imgWrap = new javax.swing.JPanel(new java.awt.BorderLayout());
        imgWrap.setOpaque(false);
        imgWrap.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 0, 10));
        imgWrap.add(img, java.awt.BorderLayout.CENTER);
        card.add(imgWrap, java.awt.BorderLayout.NORTH);

        // --------- TENGAH: NAMA, HARGA, JUMLAH ----------
        javax.swing.JPanel middle = new javax.swing.JPanel();
        middle.setOpaque(false);
        middle.setLayout(new javax.swing.BoxLayout(middle, javax.swing.BoxLayout.Y_AXIS));
        middle.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 14, 4, 14));

        javax.swing.JLabel nama = new javax.swing.JLabel(p.getName());
        nama.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        nama.setAlignmentX(0.0f);

        javax.swing.JLabel harga = new javax.swing.JLabel("Rp. " + formatRupiah(p.getPrice()));
        harga.setForeground(new java.awt.Color(158, 115, 52));
        harga.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        harga.setAlignmentX(0.0f);
        
        JLabel stokLabel = new JLabel("Stok: " + p.getStock());
        stokLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        javax.swing.JPanel qtyRow = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0) );
        qtyRow.setOpaque(false);
        qtyRow.setAlignmentX(0.0f);

        javax.swing.JLabel lQty = new javax.swing.JLabel("Jumlah");
        javax.swing.JSpinner sp = new javax.swing.JSpinner(
                new javax.swing.SpinnerNumberModel(0, 0, 999, 1)
        );
        
        
        qtyRow.add(lQty);
        qtyRow.add(sp);

        middle.add(nama);
        middle.add(javax.swing.Box.createVerticalStrut(2));
        middle.add(harga);
        middle.add(javax.swing.Box.createVerticalStrut(2));
        middle.add(stokLabel);
        middle.add(javax.swing.Box.createVerticalStrut(6));
        middle.add(qtyRow);

        card.add(middle, java.awt.BorderLayout.CENTER);

        // --------- BAWAH: TOMBOL ----------
        javax.swing.JButton btn = new javax.swing.JButton("Tambahkan Pesanan");
        btn.setBackground(new java.awt.Color(130, 87, 87));
        btn.setForeground(java.awt.Color.WHITE);
        btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

        btn.addActionListener(e -> {
    int jumlah = (Integer) sp.getValue();
    if (jumlah <= 0) {
        JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
        return;
    }

    ProductController pc = new ProductController();

    // 1. Ambil stok TERBARU dari DB
    Model.Product current = pc.ambilById(p.getId());
    if (current == null) {
        JOptionPane.showMessageDialog(this, "Produk tidak ditemukan di database");
        return;
    }

    int stokSekarang = current.getStock();

    // 2. Kalau sudah habis, jangan bisa dibeli
    if (stokSekarang <= 0) {
        JOptionPane.showMessageDialog(this, "Stok sudah habis");
        stokLabel.setText("Stok: 0 (Habis)");
        sp.setEnabled(false);
        btn.setEnabled(false);
        return;
    }

    // 3. Cek input user <= stok
    if (jumlah > stokSekarang) {
        JOptionPane.showMessageDialog(
            this,
            "Stok tidak cukup!\nStok tersedia: " + stokSekarang
        );
        return;
    }

    // 4. Tambah ke keranjang
    KeranjangController.getInstance().tambahItem(current, jumlah);

    boolean ok = pc.kurangiStok(current.getId(), jumlah);
    if (!ok) {
        // stok berubah di menit terakhir, kasih info
        JOptionPane.showMessageDialog(this, "Stok berubah, silakan coba lagi");
        Model.Product after = pc.ambilById(current.getId());
        if (after != null) {
            stokLabel.setText("Stok: " + after.getStock());
        }
        return;
    }

    // 6. Ambil stok setelah dikurangi & update label
    Model.Product after = pc.ambilById(current.getId());
    if (after != null) {
        int s = after.getStock();
        stokLabel.setText("Stok: " + s);
        if (s <= 0) {
            // kalau habis: disable, dan refresh katalog supaya kartu hilang
            stokLabel.setText("Stok: 0 (Habis)");
            sp.setEnabled(false);
            btn.setEnabled(false);

            // refresh seluruh katalog supaya produk 0 stok hilang
            SwingUtilities.invokeLater(() -> setupDynamicLayout());
        }
    }

    JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
    sp.setValue(0);
});


        javax.swing.JPanel bottom = new javax.swing.JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 10, 0));
        bottom.add(btn);

        card.add(bottom, java.awt.BorderLayout.SOUTH);

        return card;
    }

    // =========================
    //  FORMAT RUPIAH
    // =========================
    private static String formatRupiah(double v) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(v).replace(",", ".");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton36 = new javax.swing.JButton();
        blistpesanan = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        pnlDynamic = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jKucs = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        bTambahPesanan = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jKutm = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jKutv = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jKutb = new javax.swing.JSpinner();
        jLabel24 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        jMenuItem1.setText("jMenuItem1");

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        jMenuItem2.setText("jMenuItem2");

        jMenuItem3.setText("jMenuItem3");

        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("jRadioButtonMenuItem1");

        jMenu1.setText("jMenu1");

        jMenu2.setText("jMenu2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(239, 231, 221));

        jPanel2.setBackground(new java.awt.Color(209, 186, 155));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/logo.png"))); // NOI18N

        jButton36.setBackground(new java.awt.Color(79, 111, 128));
        jButton36.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton36.setForeground(new java.awt.Color(255, 255, 255));
        jButton36.setText("Keluar");
        jButton36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton36ActionPerformed(evt);
            }
        });

        blistpesanan.setBackground(new java.awt.Color(132, 99, 161));
        blistpesanan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        blistpesanan.setForeground(new java.awt.Color(255, 255, 255));
        blistpesanan.setText("Keranjang Pemesanan");
        blistpesanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blistpesananActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(blistpesanan)
                .addGap(18, 18, 18)
                .addComponent(jButton36)
                .addGap(44, 44, 44))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton36, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blistpesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI Black", 1, 34)); // NOI18N
        jLabel2.setText("Jelajahi Aneka Roti Lezat");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Temukan Roti Pilihan Anda");

        pnlDynamic.setBackground(new java.awt.Color(209, 186, 155));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Kue Ulang Tahun Coklat Strawberry");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("(25cm)");

        jLabel7.setForeground(new java.awt.Color(158, 115, 52));
        jLabel7.setText("Rp. 70.0000");

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/strawrberry coklat.png"))); // NOI18N

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Jumlah");

        bTambahPesanan.setBackground(new java.awt.Color(130, 87, 87));
        bTambahPesanan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bTambahPesanan.setForeground(new java.awt.Color(255, 255, 255));
        bTambahPesanan.setText("Tambahkan Pesanan");
        bTambahPesanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bTambahPesananActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel8))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jKucs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(bTambahPesanan)
                .addGap(33, 33, 33))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jKucs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bTambahPesanan)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jLabel4.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Kue UlangTahun");

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("Kue Ulang Tahun Matcha");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("(25cm)");

        jLabel12.setForeground(new java.awt.Color(158, 115, 52));
        jLabel12.setText("Rp. 100.0000");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel14.setText("Jumlah");

        jButton6.setBackground(new java.awt.Color(130, 87, 87));
        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("Tambahkan Pesanan");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/matcaha cake.png"))); // NOI18N
        jLabel13.setText("jLabel13");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel10)
                            .addComponent(jLabel12)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jKutm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jButton6)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jKutm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton6)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setText("Kue Ulang Tahun Vanilla");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel16.setText("(25cm)");

        jLabel17.setForeground(new java.awt.Color(158, 115, 52));
        jLabel17.setText("Rp. 90.0000");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel19.setText("Jumlah");

        jButton7.setBackground(new java.awt.Color(130, 87, 87));
        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton7.setForeground(new java.awt.Color(255, 255, 255));
        jButton7.setText("Tambahkan Pesanan");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/vanilla cake.png"))); // NOI18N
        jLabel18.setText("jLabel18");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addGap(33, 33, 33))
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel15)
                            .addComponent(jLabel17)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jKutv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jKutv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel20.setText("Kue Ulang Tahun Buah");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel21.setText("(25cm)");

        jLabel22.setForeground(new java.awt.Color(158, 115, 52));
        jLabel22.setText("Rp. 120.0000");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel24.setText("Jumlah");

        jButton8.setBackground(new java.awt.Color(130, 87, 87));
        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton8.setForeground(new java.awt.Color(255, 255, 255));
        jButton8.setText("Tambahkan Pesanan");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/buah cake.png"))); // NOI18N
        jLabel23.setText("jLabel23");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton8)
                .addGap(33, 33, 33))
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addComponent(jLabel20)
                            .addComponent(jLabel22)))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jKutb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jKutb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlDynamicLayout = new javax.swing.GroupLayout(pnlDynamic);
        pnlDynamic.setLayout(pnlDynamicLayout);
        pnlDynamicLayout.setHorizontalGroup(
            pnlDynamicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDynamicLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDynamicLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(408, 408, 408))
        );
        pnlDynamicLayout.setVerticalGroup(
            pnlDynamicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDynamicLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabel4)
                .addGap(41, 41, 41)
                .addGroup(pnlDynamicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(62, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(210, 206, 206));

        jButton1.setBackground(new java.awt.Color(175, 139, 87));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Kue Ulang Tahun");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(175, 139, 87));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Roti");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(175, 139, 87));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Bolu");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(175, 139, 87));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Kue Kering");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDynamic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(463, 463, 463)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(275, 275, 275))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(264, 264, 264))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDynamic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1343, 1343, 1343))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 707, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        KueUlangTahun ku = new KueUlangTahun();    
        ku.setVisible(true);
    this.dispose(); 
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        KueKering kk = new KueKering();
        kk.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        Bolu b = new Bolu();
        b.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton36ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton36ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        Roti r = new Roti();    
        r.setVisible(true);
    this.dispose(); 
    }//GEN-LAST:event_jButton2ActionPerformed

    private void bTambahPesananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bTambahPesananActionPerformed
        // TODO add your handling code here:
        int jumlah = (int) jKucs.getValue();   

    if (jumlah <= 0) {
        JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
        return;
    }

    Product p = productController.ambilById(27); 

    if (p == null) {
        JOptionPane.showMessageDialog(this, "Produk tidak ditemukan di database");
        return;
    }

    int stokTersedia = p.getStock();

    // CEK STOK
    if (jumlah > stokTersedia) {
        JOptionPane.showMessageDialog(this,
                "Stok tidak cukup!\nStok tersedia: " + stokTersedia);
        return;
    }

    // MASUKKAN KE KERANJANG
    KeranjangController.getInstance().tambahItem(p, jumlah);
    
     productController.kurangiStok(p.getId(), jumlah);

    JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
    jKucs.setValue(0); // reset
    }//GEN-LAST:event_bTambahPesananActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        int jumlah = (int) jKucs.getValue();   

    if (jumlah <= 0) {
        JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
        return;
    }

    Product p = productController.ambilById(28); 

    if (p == null) {
        JOptionPane.showMessageDialog(this, "Produk tidak ditemukan di database");
        return;
    }

    int stokTersedia = p.getStock();

    // CEK STOK
    if (jumlah > stokTersedia) {
        JOptionPane.showMessageDialog(this,
                "Stok tidak cukup!\nStok tersedia: " + stokTersedia);
        return;
    }

    // MASUKKAN KE KERANJANG
    KeranjangController.getInstance().tambahItem(p, jumlah);
    
     productController.kurangiStok(p.getId(), jumlah);

    JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
    jKucs.setValue(0); // reset
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
         int jumlah = (int) jKucs.getValue();   

    if (jumlah <= 0) {
        JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
        return;
    }

    Product p = productController.ambilById(29); 

    if (p == null) {
        JOptionPane.showMessageDialog(this, "Produk tidak ditemukan di database");
        return;
    }

    int stokTersedia = p.getStock();

    // CEK STOK
    if (jumlah > stokTersedia) {
        JOptionPane.showMessageDialog(this,
                "Stok tidak cukup!\nStok tersedia: " + stokTersedia);
        return;
    }

    // MASUKKAN KE KERANJANG
    KeranjangController.getInstance().tambahItem(p, jumlah);
    
     productController.kurangiStok(p.getId(), jumlah);

    JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
    jKucs.setValue(0); // reset
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
         int jumlah = (int) jKucs.getValue();   

    if (jumlah <= 0) {
        JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
        return;
    }

    Product p = productController.ambilById(30); 

    if (p == null) {
        JOptionPane.showMessageDialog(this, "Produk tidak ditemukan di database");
        return;
    }

    int stokTersedia = p.getStock();

    // CEK STOK
    if (jumlah > stokTersedia) {
        JOptionPane.showMessageDialog(this,
                "Stok tidak cukup!\nStok tersedia: " + stokTersedia);
        return;
    }

    // MASUKKAN KE KERANJANG
    KeranjangController.getInstance().tambahItem(p, jumlah);
    
     productController.kurangiStok(p.getId(), jumlah);

    JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
    jKucs.setValue(0); // reset                            
    }//GEN-LAST:event_jButton8ActionPerformed

    private void blistpesananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blistpesananActionPerformed
        // TODO add your handling code here:
        if (KeranjangController.getInstance().getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong");
            return;
        }

        int jawab = JOptionPane.showConfirmDialog(
            this,
            "Buka Keranjang Pemesanan?",
            "Konfirmasi",
            JOptionPane.YES_NO_OPTION
        );

        if (jawab == JOptionPane.YES_OPTION) {
            View.KeranjangPemesanan k = new View.KeranjangPemesanan();
            k.setLocationRelativeTo(this);
            k.setVisible(true);
            // optional: this.dispose();
        }
    }//GEN-LAST:event_blistpesananActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new KueUlangTahun().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bTambahPesanan;
    private javax.swing.JButton blistpesanan;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JSpinner jKucs;
    private javax.swing.JSpinner jKutb;
    private javax.swing.JSpinner jKutm;
    private javax.swing.JSpinner jKutv;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JPanel pnlDynamic;
    // End of variables declaration//GEN-END:variables
}
