package View;

import Controller.KeranjangController;
import Controller.ProductController;
import Model.Product;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.text.DecimalFormat;

public class Bolu extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Bolu.class.getName());
    private final ProductController productController = new ProductController();

    public Bolu() {
        initComponents();
        setupDynamicLayout();  
       // === buat header (jPanel28) melar otomatis ===
        jPanel27.addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(java.awt.event.ComponentEvent e) {
            jPanel28.setSize(jPanel27.getWidth(), jPanel28.getHeight());
        }
    });
    }

  private void setupDynamicLayout() {
    java.util.List<Product> items = productController.ambilByKategori("BOLU");

    pnlDynamic1.removeAll();

    Color coklat = new Color(209, 186, 155);
    pnlDynamic1.setBackground(coklat);
    pnlDynamic1.setLayout(new BorderLayout());

    // ====== TITLE ======
    JPanel titlePanel = new JPanel();
    titlePanel.setOpaque(false);
    titlePanel.setBorder(BorderFactory.createEmptyBorder(13, 0, 10, 0));
    jLabel88.setHorizontalAlignment(SwingConstants.CENTER);
    titlePanel.add(jLabel88);
    pnlDynamic1.add(titlePanel, BorderLayout.NORTH);

    // ====== GRID 4 KOLOM ======
    JPanel grid = new JPanel(new GridLayout(0, 4, 5, 25)); // <-- samakan 25
    grid.setOpaque(false);

    for (Product p : items) {
        JPanel cardWrap = new JPanel();
        cardWrap.setOpaque(false);
        cardWrap.add(buatCardProduk(p));
        grid.add(cardWrap);
    }

    // ====== WRAPPER ======
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setOpaque(false);
    wrapper.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 80)); // sama

    wrapper.add(grid, BorderLayout.CENTER);

    JScrollPane scroll = new JScrollPane(wrapper);
    scroll.setBorder(null);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    scroll.getViewport().setBackground(coklat);
    scroll.setBackground(coklat);

    pnlDynamic1.add(scroll, BorderLayout.CENTER);
    pnlDynamic1.setPreferredSize(new Dimension(1000, 480)); // samakan juga

    pnlDynamic1.revalidate();
    pnlDynamic1.repaint();
}



    private JPanel buatCardProduk(Product p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(210, 300));
        

        // === GAMBAR PRODUK ===
        JLabel img = new JLabel();
        img.setHorizontalAlignment(SwingConstants.CENTER);
        img.setVerticalAlignment(SwingConstants.CENTER);
        int targetW = 170, targetH = 140;
        File f = resolveImageFile(p.getImagePath());
        if (f != null && f.exists()) {
            try {
                BufferedImage bi = ImageIO.read(f);
                if (bi != null) {
                    Image scaled = bi.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
                    img.setIcon(new ImageIcon(scaled));
                } else img.setText("No Image");
            } catch (Exception e) { img.setText("No Image"); }
        } else img.setText("No Image");

        JPanel imgWrap = new JPanel(new BorderLayout());
        imgWrap.setOpaque(false);
        imgWrap.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        imgWrap.add(img, BorderLayout.CENTER);
        card.add(imgWrap, BorderLayout.NORTH);

        // === BAGIAN TENGAH ===
        JPanel middle = new JPanel();
        middle.setOpaque(false);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

        JLabel nama = new JLabel(p.getName());
        nama.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nama.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel harga = new JLabel("Rp. " + formatRupiah(p.getPrice()));
        harga.setForeground(new Color(158, 115, 52));
        harga.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel stokLabel = new JLabel("Stok: " + p.getStock());
        stokLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel qtyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        qtyRow.setOpaque(false);
        qtyRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lQty = new JLabel("Jumlah");
        JSpinner sp = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        qtyRow.add(lQty);
        qtyRow.add(sp);

        middle.add(nama);
        middle.add(Box.createVerticalStrut(2));
        middle.add(harga);
        middle.add(javax.swing.Box.createVerticalStrut(2));
        middle.add(stokLabel);     
        middle.add(Box.createVerticalStrut(6));
        middle.add(qtyRow);
        card.add(middle, BorderLayout.CENTER);

        // === BAGIAN BAWAH ===
        JButton btn = new JButton("Tambahkan Pesanan");
        btn.setBackground(new Color(130, 87, 87));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btn.addActionListener(e -> {
            int jumlah = (Integer) sp.getValue();

        // 1. Jumlah harus > 0
        if (jumlah <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
            return;
        }

        ProductController pc = new ProductController();

        // 2. Ambil stok terbaru dari DB
        Model.Product current = pc.ambilById(p.getId());
        if (current == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Produk sudah tidak ada di database");
            return;
        }

        // 3. Cek stok cukup
        if (jumlah > current.getStock()) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Stok tidak cukup! Tersedia: " + current.getStock()
            );
            return;
        }

        // 4. Kurangi stok di DB (tidak boleh minus)
        boolean ok = pc.kurangiStok(current.getId(), jumlah);
        if (!ok) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Stok baru saja berubah, silakan coba lagi"
            );
            return;
        }

        // 5. Tambah ke keranjang
        KeranjangController.getInstance().tambahItem(current, jumlah);

        // 6. Ambil stok terbaru lagi & update label di UI
        Model.Product after = pc.ambilById(current.getId());
        if (after != null) {
            stokLabel.setText("Stok: " + after.getStock());

            // kalau sudah habis, sembunyikan card dari katalog
            if (after.getStock() <= 0) {
                card.setVisible(false);
            }
        }

        javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
        sp.setValue(0);
    });

    javax.swing.JPanel bottom = new javax.swing.JPanel();
    bottom.setOpaque(false);
    bottom.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 10, 0));
    bottom.add(btn);

    card.add(bottom, java.awt.BorderLayout.SOUTH);

    return card;
}

    private File resolveImageFile(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return null;
        File f = new File(imagePath);
        if (f.exists()) return f;
        String p = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        f = new File("src/" + p);
        if (f.exists()) return f;
        f = new File("src/View/" + p);
        if (f.exists()) return f;
        f = new File("build/classes/" + p);
        if (f.exists()) return f;
        return null;
    }

    private static String formatRupiah(double v) {
        DecimalFormat df = new DecimalFormat("#,###");
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

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton37 = new javax.swing.JButton();
        blistpesanan1 = new javax.swing.JButton();
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
        jLabel64 = new javax.swing.JLabel();
        jKutb = new javax.swing.JSpinner();
        jLabel65 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jLabel66 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel27 = new javax.swing.JPanel();
        jPanel28 = new javax.swing.JPanel();
        jLabel67 = new javax.swing.JLabel();
        jButton38 = new javax.swing.JButton();
        blistpesanan2 = new javax.swing.JButton();
        jLabel81 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        pnlDynamic1 = new javax.swing.JPanel();
        jPanel34 = new javax.swing.JPanel();
        jLabel83 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        jKucs1 = new javax.swing.JSpinner();
        jLabel87 = new javax.swing.JLabel();
        bTambahPesanan1 = new javax.swing.JButton();
        jLabel108 = new javax.swing.JLabel();
        jLabel88 = new javax.swing.JLabel();
        jPanel38 = new javax.swing.JPanel();
        jLabel89 = new javax.swing.JLabel();
        jLabel90 = new javax.swing.JLabel();
        jLabel91 = new javax.swing.JLabel();
        jKucs2 = new javax.swing.JSpinner();
        jLabel92 = new javax.swing.JLabel();
        bTambahPesanan2 = new javax.swing.JButton();
        jLabel109 = new javax.swing.JLabel();
        jPanel40 = new javax.swing.JPanel();
        jLabel110 = new javax.swing.JLabel();
        jLabel111 = new javax.swing.JLabel();
        jLabel112 = new javax.swing.JLabel();
        jKucs3 = new javax.swing.JSpinner();
        jLabel113 = new javax.swing.JLabel();
        bTambahPesanan3 = new javax.swing.JButton();
        jLabel114 = new javax.swing.JLabel();
        jPanel41 = new javax.swing.JPanel();
        jLabel115 = new javax.swing.JLabel();
        jLabel116 = new javax.swing.JLabel();
        jLabel117 = new javax.swing.JLabel();
        jKucs4 = new javax.swing.JSpinner();
        jLabel118 = new javax.swing.JLabel();
        bTambahPesanan4 = new javax.swing.JButton();
        jLabel119 = new javax.swing.JLabel();
        jPanel39 = new javax.swing.JPanel();
        jButton39 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        jButton42 = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(239, 231, 221));

        jPanel2.setBackground(new java.awt.Color(209, 186, 155));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/logo.png"))); // NOI18N

        jButton37.setBackground(new java.awt.Color(79, 111, 128));
        jButton37.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton37.setForeground(new java.awt.Color(255, 255, 255));
        jButton37.setText("Keluar");
        jButton37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton37ActionPerformed(evt);
            }
        });

        blistpesanan1.setBackground(new java.awt.Color(132, 99, 161));
        blistpesanan1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        blistpesanan1.setForeground(new java.awt.Color(255, 255, 255));
        blistpesanan1.setText("Keranjang Pemesanan");
        blistpesanan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blistpesanan1ActionPerformed(evt);
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
                .addComponent(blistpesanan1)
                .addGap(18, 18, 18)
                .addComponent(jButton37)
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
                    .addComponent(jButton37, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blistpesanan1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jLabel64.setForeground(new java.awt.Color(158, 115, 52));
        jLabel64.setText("Rp. 120.0000");

        jLabel65.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel65.setText("Jumlah");

        jButton8.setBackground(new java.awt.Color(130, 87, 87));
        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton8.setForeground(new java.awt.Color(255, 255, 255));
        jButton8.setText("Tambahkan Pesanan");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jLabel66.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/buah cake.png"))); // NOI18N
        jLabel66.setText("jLabel23");

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
                            .addComponent(jLabel64)))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel65)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jKutb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel66, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel66, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel64)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel65)
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel27.setBackground(new java.awt.Color(239, 231, 221));

        jPanel28.setBackground(new java.awt.Color(209, 186, 155));

        jLabel67.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/logo.png"))); // NOI18N

        jButton38.setBackground(new java.awt.Color(79, 111, 128));
        jButton38.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton38.setForeground(new java.awt.Color(255, 255, 255));
        jButton38.setText("Keluar");
        jButton38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton38ActionPerformed(evt);
            }
        });

        blistpesanan2.setBackground(new java.awt.Color(132, 99, 161));
        blistpesanan2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        blistpesanan2.setForeground(new java.awt.Color(255, 255, 255));
        blistpesanan2.setText("Keranjang Pemesanan");
        blistpesanan2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blistpesanan2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(jLabel67)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(blistpesanan2)
                .addGap(18, 18, 18)
                .addComponent(jButton38)
                .addGap(44, 44, 44))
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel28Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel67, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton38, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blistpesanan2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel81.setFont(new java.awt.Font("Segoe UI Black", 1, 34)); // NOI18N
        jLabel81.setText("Jelajahi Aneka Roti Lezat");

        jLabel82.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel82.setText("Temukan Roti Pilihan Anda");

        pnlDynamic1.setBackground(new java.awt.Color(209, 186, 155));

        jPanel34.setBackground(new java.awt.Color(255, 255, 255));

        jLabel83.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel83.setText("Bolu Keju");

        jLabel85.setForeground(new java.awt.Color(158, 115, 52));
        jLabel85.setText("Rp. 20.000");

        jLabel87.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel87.setText("Jumlah");

        bTambahPesanan1.setBackground(new java.awt.Color(130, 87, 87));
        bTambahPesanan1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bTambahPesanan1.setForeground(new java.awt.Color(255, 255, 255));
        bTambahPesanan1.setText("Tambahkan Pesanan");
        bTambahPesanan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bTambahPesanan1ActionPerformed(evt);
            }
        });

        jLabel108.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/bolu keju.png"))); // NOI18N

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel34Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel83)
                            .addGroup(jPanel34Layout.createSequentialGroup()
                                .addComponent(jLabel108, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel86))
                            .addComponent(jLabel85)
                            .addGroup(jPanel34Layout.createSequentialGroup()
                                .addComponent(jLabel87)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jKucs1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel34Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(bTambahPesanan1)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel34Layout.createSequentialGroup()
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel34Layout.createSequentialGroup()
                        .addGap(181, 181, 181)
                        .addComponent(jLabel86)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel34Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel108, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)))
                .addComponent(jLabel83)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel85)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel87)
                    .addComponent(jKucs1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(bTambahPesanan1)
                .addGap(27, 27, 27))
        );

        jLabel88.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        jLabel88.setForeground(new java.awt.Color(255, 255, 255));
        jLabel88.setText("Bolu");

        jPanel38.setBackground(new java.awt.Color(255, 255, 255));

        jLabel89.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel89.setText("Bolu Meses");

        jLabel90.setForeground(new java.awt.Color(158, 115, 52));
        jLabel90.setText("Rp. 15.000");

        jLabel92.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel92.setText("Jumlah");

        bTambahPesanan2.setBackground(new java.awt.Color(130, 87, 87));
        bTambahPesanan2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bTambahPesanan2.setForeground(new java.awt.Color(255, 255, 255));
        bTambahPesanan2.setText("Tambahkan Pesanan");
        bTambahPesanan2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bTambahPesanan2ActionPerformed(evt);
            }
        });

        jLabel109.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/bolumeses.png"))); // NOI18N

        javax.swing.GroupLayout jPanel38Layout = new javax.swing.GroupLayout(jPanel38);
        jPanel38.setLayout(jPanel38Layout);
        jPanel38Layout.setHorizontalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel38Layout.createSequentialGroup()
                .addGroup(jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel38Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel89)
                            .addGroup(jPanel38Layout.createSequentialGroup()
                                .addComponent(jLabel109, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel91))
                            .addComponent(jLabel90)
                            .addGroup(jPanel38Layout.createSequentialGroup()
                                .addComponent(jLabel92)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jKucs2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel38Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(bTambahPesanan2)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel38Layout.setVerticalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel38Layout.createSequentialGroup()
                .addGroup(jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel38Layout.createSequentialGroup()
                        .addGap(181, 181, 181)
                        .addComponent(jLabel91)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel38Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel109, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)))
                .addComponent(jLabel89)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel90)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel92)
                    .addComponent(jKucs2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bTambahPesanan2)
                .addGap(27, 27, 27))
        );

        jPanel40.setBackground(new java.awt.Color(255, 255, 255));

        jLabel110.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel110.setText("Bolu Meses Strawberry");

        jLabel111.setForeground(new java.awt.Color(158, 115, 52));
        jLabel111.setText("Rp. 20.000");

        jLabel113.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel113.setText("Jumlah");

        bTambahPesanan3.setBackground(new java.awt.Color(130, 87, 87));
        bTambahPesanan3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bTambahPesanan3.setForeground(new java.awt.Color(255, 255, 255));
        bTambahPesanan3.setText("Tambahkan Pesanan");
        bTambahPesanan3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bTambahPesanan3ActionPerformed(evt);
            }
        });

        jLabel114.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/bolu meses strawberry.png"))); // NOI18N

        javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
        jPanel40.setLayout(jPanel40Layout);
        jPanel40Layout.setHorizontalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel40Layout.createSequentialGroup()
                .addGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel40Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel110)
                            .addGroup(jPanel40Layout.createSequentialGroup()
                                .addComponent(jLabel114, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel112))
                            .addComponent(jLabel111)
                            .addGroup(jPanel40Layout.createSequentialGroup()
                                .addComponent(jLabel113)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jKucs3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel40Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(bTambahPesanan3)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel40Layout.setVerticalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel40Layout.createSequentialGroup()
                .addGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel40Layout.createSequentialGroup()
                        .addGap(181, 181, 181)
                        .addComponent(jLabel112)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel40Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel114, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)))
                .addComponent(jLabel110)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel111)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel113)
                    .addComponent(jKucs3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bTambahPesanan3)
                .addGap(27, 27, 27))
        );

        jPanel41.setBackground(new java.awt.Color(255, 255, 255));

        jLabel115.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel115.setText("Bolu Matcha Strawberry");

        jLabel116.setForeground(new java.awt.Color(158, 115, 52));
        jLabel116.setText("Rp. 27.000");

        jLabel118.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel118.setText("Jumlah");

        bTambahPesanan4.setBackground(new java.awt.Color(130, 87, 87));
        bTambahPesanan4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bTambahPesanan4.setForeground(new java.awt.Color(255, 255, 255));
        bTambahPesanan4.setText("Tambahkan Pesanan");
        bTambahPesanan4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bTambahPesanan4ActionPerformed(evt);
            }
        });

        jLabel119.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/bolu matcha strawberry.png"))); // NOI18N

        javax.swing.GroupLayout jPanel41Layout = new javax.swing.GroupLayout(jPanel41);
        jPanel41.setLayout(jPanel41Layout);
        jPanel41Layout.setHorizontalGroup(
            jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel41Layout.createSequentialGroup()
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel41Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel115)
                            .addGroup(jPanel41Layout.createSequentialGroup()
                                .addComponent(jLabel119, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel117))
                            .addComponent(jLabel116)
                            .addGroup(jPanel41Layout.createSequentialGroup()
                                .addComponent(jLabel118)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jKucs4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel41Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(bTambahPesanan4)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel41Layout.setVerticalGroup(
            jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel41Layout.createSequentialGroup()
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel41Layout.createSequentialGroup()
                        .addGap(181, 181, 181)
                        .addComponent(jLabel117)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel41Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel119, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)))
                .addComponent(jLabel115)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel116)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel118)
                    .addComponent(jKucs4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bTambahPesanan4)
                .addGap(27, 27, 27))
        );

        javax.swing.GroupLayout pnlDynamic1Layout = new javax.swing.GroupLayout(pnlDynamic1);
        pnlDynamic1.setLayout(pnlDynamic1Layout);
        pnlDynamic1Layout.setHorizontalGroup(
            pnlDynamic1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDynamic1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jPanel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(jPanel38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(jPanel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(jPanel41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDynamic1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel88)
                .addGap(476, 476, 476))
        );
        pnlDynamic1Layout.setVerticalGroup(
            pnlDynamic1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDynamic1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel88)
                .addGap(41, 41, 41)
                .addGroup(pnlDynamic1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDynamic1Layout.createSequentialGroup()
                        .addComponent(jPanel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel38, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel40, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel39.setBackground(new java.awt.Color(210, 206, 206));

        jButton39.setBackground(new java.awt.Color(175, 139, 87));
        jButton39.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton39.setForeground(new java.awt.Color(255, 255, 255));
        jButton39.setText("Kue Ulang Tahun");
        jButton39.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton39ActionPerformed(evt);
            }
        });

        jButton40.setBackground(new java.awt.Color(175, 139, 87));
        jButton40.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton40.setForeground(new java.awt.Color(255, 255, 255));
        jButton40.setText("Roti");
        jButton40.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton40ActionPerformed(evt);
            }
        });

        jButton41.setBackground(new java.awt.Color(175, 139, 87));
        jButton41.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton41.setForeground(new java.awt.Color(255, 255, 255));
        jButton41.setText("Bolu");
        jButton41.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton41ActionPerformed(evt);
            }
        });

        jButton42.setBackground(new java.awt.Color(175, 139, 87));
        jButton42.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton42.setForeground(new java.awt.Color(255, 255, 255));
        jButton42.setText("Kue Kering");
        jButton42.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton42ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel39Layout = new javax.swing.GroupLayout(jPanel39);
        jPanel39.setLayout(jPanel39Layout);
        jPanel39Layout.setHorizontalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel39Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton39)
                .addGap(18, 18, 18)
                .addComponent(jButton40)
                .addGap(18, 18, 18)
                .addComponent(jButton41)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton42)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel39Layout.setVerticalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel39Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton39)
                    .addComponent(jButton40)
                    .addComponent(jButton41)
                    .addComponent(jButton42))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlDynamic1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addGap(463, 463, 463)
                .addComponent(jLabel82)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel81)
                .addGap(266, 266, 266))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(264, 264, 264))
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addComponent(jPanel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel81)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel82)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDynamic1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1292, 1292, 1292))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, 655, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton37ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton37ActionPerformed

    private void blistpesanan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blistpesanan1ActionPerformed
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
    }//GEN-LAST:event_blistpesanan1ActionPerformed

    private void bTambahPesananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bTambahPesananActionPerformed
        // TODO add your handling code here:
        int jumlah = (int) jKucs.getValue();
        if (jumlah <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
            return;
        }
        Product p = new Product(101, "Kue Ulang Tahun Coklat Strawberry", 70000.0, 0, "kue", "/View/strawrberry coklat.png"); // ID & harga sesuaikan
        KeranjangController.getInstance().tambahItem(p, jumlah);

        javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
        jKucs.setValue(0); // reset
    }//GEN-LAST:event_bTambahPesananActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        int jumlah = (int) jKutm.getValue();
        if (jumlah <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
            return;
        }
        Product p = new Product(102, "Kue Ulang Tahun Matcha", 100000.0, 0, "kue", "/View/matcaha cake.png"); // ID & harga sesuaikan
        KeranjangController.getInstance().tambahItem(p, jumlah);

        javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
        jKutm.setValue(0); // reset
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        int jumlah = (int) jKutv.getValue();
        if (jumlah <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
            return;
        }
        Product p = new Product(103, "Kue Ulang Tahun Vanila", 90000.0, 0, "kue", "/View/vanilla cake.png"); // ID & harga sesuaikan
        KeranjangController.getInstance().tambahItem(p, jumlah);

        javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
        jKutv.setValue(0); // reset
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        int jumlah = (int) jKutb.getValue();
        if (jumlah <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
            return;
        }
        Product p = new Product(104, "Kue Ulang Tahun Buah", 120000.0, 0, "kue", "/View/buah cake.png"); // ID & harga sesuaikan
        KeranjangController.getInstance().tambahItem(p, jumlah);

        javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
        jKutb.setValue(0); // reset
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        KueUlangTahun ku = new KueUlangTahun();
        ku.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        Roti r = new Roti();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        Bolu b = new Bolu();
        b.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        KueKering kk = new KueKering();
        kk.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton38ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton38ActionPerformed

    private void blistpesanan2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blistpesanan2ActionPerformed
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
    }//GEN-LAST:event_blistpesanan2ActionPerformed

    private void bTambahPesanan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bTambahPesanan1ActionPerformed
        // TODO add your handling code here:
        int jumlah = (int) jKucs.getValue();
        if (jumlah <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
            return;
        }
        Product p = new Product(1, "Bolu Keju", 20000, 0, "bolu", "/View/bolu keju.png"); // ID & harga sesuaikan
        KeranjangController.getInstance().tambahItem(p, jumlah);

        javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
        jKucs.setValue(0); // reset
    }//GEN-LAST:event_bTambahPesanan1ActionPerformed

    private void jButton39ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton39ActionPerformed
        // TODO add your handling code here:
        KueUlangTahun ku = new KueUlangTahun();
        ku.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton39ActionPerformed

    private void jButton40ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton40ActionPerformed
        // TODO add your handling code here:
        Roti r = new Roti();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton40ActionPerformed

    private void jButton41ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton41ActionPerformed
        // TODO add your handling code here:
        Bolu b = new Bolu();
        b.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton41ActionPerformed

    private void jButton42ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton42ActionPerformed
        // TODO add your handling code here:
        KueKering kk = new KueKering();
        kk.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton42ActionPerformed

    private void bTambahPesanan2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bTambahPesanan2ActionPerformed
        // TODO add your handling code here:
        int jumlah = (int) jKucs.getValue();
        if (jumlah <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
            return;
        }
        Product p = new Product(2, "Bolu Meses", 15000, 0, "bolu", "/View/bolumeses.png"); // ID & harga sesuaikan
        KeranjangController.getInstance().tambahItem(p, jumlah);

        javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
        jKucs.setValue(0); // reset
    }//GEN-LAST:event_bTambahPesanan2ActionPerformed

    private void bTambahPesanan3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bTambahPesanan3ActionPerformed
        // TODO add your handling code here:int jumlah = (int) jKucs.getValue();
        int jumlah = (int) jKucs.getValue();
        if (jumlah <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
            return;
        }
        Product p = new Product(3, "Bolu Meses Strawberry", 20000, 0, "bolu", "/View/bolu meses strawberry.png"); // ID & harga sesuaikan
        KeranjangController.getInstance().tambahItem(p, jumlah);

        javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
        jKucs.setValue(0); // reset
    }//GEN-LAST:event_bTambahPesanan3ActionPerformed

    private void bTambahPesanan4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bTambahPesanan4ActionPerformed
        // TODO add your handling code here:
        int jumlah = (int) jKucs.getValue();
        if (jumlah <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
            return;
        }
        Product p = new Product(4, "Bolu Matcha Strawberry", 27000, 0, "bolu", "/View/bolu matcha strawberry.png"); // ID & harga sesuaikan
        KeranjangController.getInstance().tambahItem(p, jumlah);

        javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
        jKucs.setValue(0); // reset
    }//GEN-LAST:event_bTambahPesanan4ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new Bolu().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bTambahPesanan;
    private javax.swing.JButton bTambahPesanan1;
    private javax.swing.JButton bTambahPesanan2;
    private javax.swing.JButton bTambahPesanan3;
    private javax.swing.JButton bTambahPesanan4;
    private javax.swing.JButton blistpesanan1;
    private javax.swing.JButton blistpesanan2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JSpinner jKucs;
    private javax.swing.JSpinner jKucs1;
    private javax.swing.JSpinner jKucs2;
    private javax.swing.JSpinner jKucs3;
    private javax.swing.JSpinner jKucs4;
    private javax.swing.JSpinner jKutb;
    private javax.swing.JSpinner jKutm;
    private javax.swing.JSpinner jKutv;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel108;
    private javax.swing.JLabel jLabel109;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel110;
    private javax.swing.JLabel jLabel111;
    private javax.swing.JLabel jLabel112;
    private javax.swing.JLabel jLabel113;
    private javax.swing.JLabel jLabel114;
    private javax.swing.JLabel jLabel115;
    private javax.swing.JLabel jLabel116;
    private javax.swing.JLabel jLabel117;
    private javax.swing.JLabel jLabel118;
    private javax.swing.JLabel jLabel119;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel pnlDynamic;
    private javax.swing.JPanel pnlDynamic1;
    // End of variables declaration//GEN-END:variables
}
