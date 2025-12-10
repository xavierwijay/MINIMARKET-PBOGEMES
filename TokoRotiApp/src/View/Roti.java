/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

import Controller.KeranjangController;
import Controller.ProductController;
import Model.Product;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
/**
 *
 * @author LENOVO
 */
public class Roti extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Roti.class.getName());
    private final ProductController productController = new ProductController();

    /**
     * Creates new form Roti
     */
    public Roti() {
        initComponents();
        setupDynamicLayout();
        
        
    }
    
    private void setupDynamicLayout() {
    // 1. Ambil produk kategori ROTI
    java.util.List<Product> items = productController.ambilByKategori("ROTI");

    // 2. Bersihkan panel coklat (jPanel5) dan pakai BorderLayout
    jPanel5.removeAll();
    Color coklat = new Color(209, 186, 155);
    jPanel5.setBackground(coklat);
    jPanel5.setLayout(new BorderLayout());

    // 3. Panel judul "Roti" DI-TENGAH
    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
    titlePanel.setOpaque(false);

    // pastikan label bisa rata tengah
    jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
    titlePanel.add(jLabel4);

    jPanel5.add(titlePanel, BorderLayout.NORTH);

    // 4. GRID 4 KOLOM (card produk)
    JPanel grid = new JPanel(new GridLayout(0, 4, 25, 25));
    grid.setOpaque(false);

    for (Product p : items) {
        grid.add(buatCardProduk(p));
    }

    // 5. WRAPPER dengan padding kiri/kanan supaya tidak nempel tepi
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setOpaque(false);

    // padding: atas, kiri, bawah, kanan (bisa disesuaikan)
    wrapper.setBorder(BorderFactory.createEmptyBorder(0, 80, 20, 10));

    wrapper.add(grid, BorderLayout.CENTER);

    // 6. ScrollPane
    JScrollPane scroll = new JScrollPane(wrapper);
    scroll.setBorder(null);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    scroll.getViewport().setBackground(coklat);
    scroll.setBackground(coklat);

    // 7. Masukkan scroll ke jPanel5
    jPanel5.add(scroll, BorderLayout.CENTER);

    // 8. Refresh tampilan
    jPanel5.revalidate();
    jPanel5.repaint();
}
   private JPanel buatCardProduk(Product p) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(Color.WHITE);
    card.setPreferredSize(new Dimension(210, 300));
    card.setMaximumSize(new Dimension(210, 300));

    // ========= ATAS: GAMBAR =========
    JLabel img = new JLabel();
    img.setHorizontalAlignment(SwingConstants.CENTER);
    img.setVerticalAlignment(SwingConstants.CENTER);

    int targetW = 170;
    int targetH = 140;

    File f = resolveImageFile(p.getImagePath());
    if (f != null) {
        try {
            BufferedImage bi = ImageIO.read(f);
            if (bi != null) {
                Image scaled = bi.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
                img.setIcon(new ImageIcon(scaled));
            } else {
                img.setText("No Image");
            }
        } catch (Exception e) {
            img.setText("No Image");
        }
    } else {
        img.setText("No Image");
    }

    JPanel imgWrap = new JPanel(new BorderLayout());
    imgWrap.setOpaque(false);
    imgWrap.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    imgWrap.add(img, BorderLayout.CENTER);
    card.add(imgWrap, BorderLayout.NORTH);

    // ========= TENGAH: NAMA, HARGA, JUMLAH =========
    JPanel middle = new JPanel();
    middle.setOpaque(false);
    middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
    middle.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

    JLabel nama = new JLabel(p.getName());
    nama.setFont(new Font("Segoe UI", Font.BOLD, 12));
    nama.setAlignmentX(0.0f);

    JLabel harga = new JLabel("Rp. " + formatRupiah(p.getPrice()));
    harga.setForeground(new Color(158, 115, 52));
    harga.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    harga.setAlignmentX(0.0f);
    
    JLabel stokLabel = new JLabel("Stok: " + p.getStock());
    stokLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    JPanel qtyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    qtyRow.setOpaque(false);
    qtyRow.setAlignmentX(0.0f);

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

    // ========= BAWAH: TOMBOL =========
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
        jButton36 = new javax.swing.JButton();
        blistpesanan = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jRotiBawang = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jButton35 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jRotiIsiAyam = new javax.swing.JSpinner();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jButton37 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jRotiSobek = new javax.swing.JSpinner();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jButton38 = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jRotiTawar = new javax.swing.JSpinner();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jButton39 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

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
                .addGap(69, 69, 69)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(blistpesanan)
                .addGap(18, 18, 18)
                .addComponent(jButton36)
                .addGap(43, 43, 43))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton36, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blistpesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI Black", 1, 34)); // NOI18N
        jLabel2.setText("Jelajahi Aneka Roti Lezat");

        jPanel5.setBackground(new java.awt.Color(209, 186, 155));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Roti Bawang");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(158, 115, 52));
        jLabel7.setText("Rp. 25.0000");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Jumlah");

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/garlic.png"))); // NOI18N
        jLabel25.setText("jLabel25");

        jButton35.setBackground(new java.awt.Color(130, 87, 87));
        jButton35.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton35.setForeground(new java.awt.Color(255, 255, 255));
        jButton35.setText("Tambahkan Pesanan");
        jButton35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton35ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRotiBawang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7)
                    .addComponent(jLabel5)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(38, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton35)
                .addGap(27, 27, 27))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRotiBawang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addComponent(jButton35, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Roti");

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel26.setText("Roti Isi Ayam");

        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(158, 115, 52));
        jLabel27.setText("Rp. 15.0000");

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel28.setText("Jumlah");

        jLabel29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/roti isi.png"))); // NOI18N
        jLabel29.setText("jLabel29");

        jButton37.setBackground(new java.awt.Color(130, 87, 87));
        jButton37.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton37.setForeground(new java.awt.Color(255, 255, 255));
        jButton37.setText("Tambahkan Pesanan");
        jButton37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton37ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton37)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                            .addGap(20, 20, 20)
                            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel9Layout.createSequentialGroup()
                                    .addComponent(jLabel28)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jRotiIsiAyam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel27)
                                .addComponent(jLabel26)))
                        .addGroup(jPanel9Layout.createSequentialGroup()
                            .addGap(30, 30, 30)
                            .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(jRotiIsiAyam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton37, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));

        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel30.setText("Roti Sobek");

        jLabel31.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(158, 115, 52));
        jLabel31.setText("Rp. 13.0000");

        jLabel32.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel32.setText("Jumlah");

        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/roti sobek.png"))); // NOI18N

        jButton38.setBackground(new java.awt.Color(130, 87, 87));
        jButton38.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton38.setForeground(new java.awt.Color(255, 255, 255));
        jButton38.setText("Tambahkan Pesanan");
        jButton38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton38ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel32)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRotiSobek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel31)
                            .addComponent(jLabel30))))
                .addContainerGap(73, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addComponent(jButton38)
                        .addGap(30, 30, 30))))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel32)
                    .addComponent(jRotiSobek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton38, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));

        jLabel34.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel34.setText("Roti Tawar");

        jLabel35.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(158, 115, 52));
        jLabel35.setText("Rp. 22.0000");

        jLabel36.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel36.setText("Jumlah");

        jLabel37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/roti tawar.png"))); // NOI18N

        jButton39.setBackground(new java.awt.Color(130, 87, 87));
        jButton39.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton39.setForeground(new java.awt.Color(255, 255, 255));
        jButton39.setText("Tambahkan Pesanan");
        jButton39.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton39ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel36)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRotiTawar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel35)
                            .addComponent(jLabel34)))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jLabel37)))
                .addContainerGap(36, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton39)
                .addGap(26, 26, 26))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel37)
                .addGap(18, 18, 18)
                .addComponent(jLabel34)
                .addGap(3, 3, 3)
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRotiTawar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36))
                .addGap(18, 24, Short.MAX_VALUE)
                .addComponent(jButton39, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(47, 47, 47)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(495, 495, 495)
                        .addComponent(jLabel4)))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel4)
                .addGap(30, 30, 30)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
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
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(293, 293, 293)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(294, 294, 294))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(834, 834, 834))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 629, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton36ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton36ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        KueUlangTahun kut = new KueUlangTahun();
        kut.setVisible(true);
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

    private void jButton35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton35ActionPerformed
        // TODO add your handling code here:                                                   
    int jumlah = (int) jRotiBawang.getValue();   
    if (jumlah <= 0) {
        javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
        return;
    }
    Product p = new Product(201, "Roti Bawang", 25000.0, 0, "roti", "/View/garlic.png"); // ID & harga sesuaikan
    KeranjangController.getInstance().tambahItem(p, jumlah);

    javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
    jRotiBawang.setValue(0); // reset

    }//GEN-LAST:event_jButton35ActionPerformed

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

    private void jButton37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton37ActionPerformed
        // TODO add your handling code here:
         int jumlah = (int) jRotiIsiAyam.getValue();   
    if (jumlah <= 0) {
        javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
        return;
    }
    Product p = new Product(202, "Roti Isi Ayam", 15000.0, 0, "roti", "/View/roti isi.png"); // ID & harga sesuaikan
    KeranjangController.getInstance().tambahItem(p, jumlah);

    javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
    jRotiIsiAyam.setValue(0); // reset
    }//GEN-LAST:event_jButton37ActionPerformed

    private void jButton38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton38ActionPerformed
        // TODO add your handling code here:
         int jumlah = (int) jRotiSobek.getValue();   
    if (jumlah <= 0) {
        javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
        return;
    }
    Product p = new Product(203, "Roti Sobek", 13000.0, 0, "roti", "/View/roti sobek.png"); // ID & harga sesuaikan
    KeranjangController.getInstance().tambahItem(p, jumlah);

    javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
    jRotiSobek.setValue(0); // reset
    }//GEN-LAST:event_jButton38ActionPerformed

    private void jButton39ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton39ActionPerformed
        // TODO add your handling code here:
        int jumlah = (int) jRotiTawar.getValue();   
    if (jumlah <= 0) {
        javax.swing.JOptionPane.showMessageDialog(this, "Jumlah harus > 0");
        return;
    }
    Product p = new Product(204, "Roti Tawar", 22000.0, 0, "roti", "/View/roti tawar.png"); // ID & harga sesuaikan
    KeranjangController.getInstance().tambahItem(p, jumlah);

    javax.swing.JOptionPane.showMessageDialog(this, "Ditambahkan ke keranjang");
    jRotiTawar.setValue(0); // reset
    }//GEN-LAST:event_jButton39ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new Roti().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton blistpesanan;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSpinner jRotiBawang;
    private javax.swing.JSpinner jRotiIsiAyam;
    private javax.swing.JSpinner jRotiSobek;
    private javax.swing.JSpinner jRotiTawar;
    // End of variables declaration//GEN-END:variables
}
