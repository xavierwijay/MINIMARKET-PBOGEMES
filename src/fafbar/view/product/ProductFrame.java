/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package fafbar.view.product;

import fafbar.controller.ProductController;
import fafbar.model.Product;
import fafbar.view.category.CategoryFrame;
import fafbar.view.report.ReportFrame;
import fafbar.view.sales.SalesFrame;
import fafbar.view.user.UserFrame;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author Lenovo
 */
public class ProductFrame extends javax.swing.JFrame {
    private ProductController controller = new ProductController();
    private DefaultTableModel model;

    public ProductFrame() {
        initComponents();
        initCustomTable();
        
        loadData(null, null);
    }
    private void loadData(String categoryId, String keyword) {
        if (model != null) {
            model.setRowCount(0);
        }
        
        java.util.List<fafbar.model.Product> list = controller.searchProducts(categoryId, keyword);
        
        for (fafbar.model.Product p : list) {
            Object[] row = {
                false, 
                p.getId(),
                p.getCategoryId(),
                p.getName(),
                p.getUnit(),
                p.getCode(),
                p.getPrice(),
                p.getStock(),
                p.getCreatedAt()
            };
            model.addRow(row);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cmbCategoryID = new javax.swing.JComboBox<>();
        btnSearchCategory = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        TableProduct = new javax.swing.JTable();
        ButtonUpdate = new javax.swing.JButton();
        ButtonDelete = new javax.swing.JButton();
        TextSearch = new javax.swing.JTextField();
        sidebar2 = new javax.swing.JPanel();
        btnUser2 = new javax.swing.JButton();
        btnSales2 = new javax.swing.JButton();
        btnReport2 = new javax.swing.JButton();
        btnCategory2 = new javax.swing.JButton();
        btnProduct2 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        btnLogout7 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 204, 204));

        jPanel2.setBackground(new java.awt.Color(255, 204, 204));

        jLabel1.setText("PRODUCT");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Category ID :");

        cmbCategoryID.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "7-Minuman", "8-Makanan", "9-Sembako" }));
        cmbCategoryID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryIDActionPerformed(evt);
            }
        });

        btnSearchCategory.setText("Search");
        btnSearchCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchCategoryActionPerformed(evt);
            }
        });

        TableProduct.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Select", "ID", "Category ID", "Name", "Unit", "Code", "Price", "Stock", "CreatedAt", "UpdatedAt"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        TableProduct.setColumnSelectionAllowed(true);
        jScrollPane2.setViewportView(TableProduct);

        ButtonUpdate.setBackground(new java.awt.Color(102, 204, 255));
        ButtonUpdate.setText("Update");
        ButtonUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonUpdateActionPerformed(evt);
            }
        });

        ButtonDelete.setBackground(new java.awt.Color(255, 0, 0));
        ButtonDelete.setText("Delete");
        ButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonDeleteActionPerformed(evt);
            }
        });

        TextSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 429, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(245, 245, 245))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(ButtonDelete)
                                .addGap(18, 18, 18)
                                .addComponent(ButtonUpdate)
                                .addContainerGap())))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(cmbCategoryID, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TextSearch)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSearchCategory)))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCategoryID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearchCategory)
                    .addComponent(TextSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ButtonUpdate)
                    .addComponent(ButtonDelete))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sidebar2.setBackground(new java.awt.Color(255, 204, 204));

        btnUser2.setBackground(new java.awt.Color(255, 204, 204));
        btnUser2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnUser2.setText("USER");
        btnUser2.setBorder(null);
        btnUser2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUser2ActionPerformed(evt);
            }
        });

        btnSales2.setBackground(new java.awt.Color(255, 204, 204));
        btnSales2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnSales2.setText("SALES");
        btnSales2.setBorder(null);
        btnSales2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSales2ActionPerformed(evt);
            }
        });

        btnReport2.setBackground(new java.awt.Color(255, 204, 204));
        btnReport2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnReport2.setText("REPORT");
        btnReport2.setBorder(null);
        btnReport2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReport2ActionPerformed(evt);
            }
        });

        btnCategory2.setBackground(new java.awt.Color(255, 153, 153));
        btnCategory2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnCategory2.setText("CATEGORY");
        btnCategory2.setBorder(null);
        btnCategory2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCategory2ActionPerformed(evt);
            }
        });

        btnProduct2.setBackground(new java.awt.Color(255, 204, 204));
        btnProduct2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnProduct2.setText("PRODUCT");
        btnProduct2.setBorder(null);
        btnProduct2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProduct2ActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 2, 24)); // NOI18N
        jLabel12.setText("FAFBAR");

        btnLogout7.setBackground(new java.awt.Color(255, 204, 204));
        btnLogout7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogout7.setText("LOGOUT");
        btnLogout7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogout7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sidebar2Layout = new javax.swing.GroupLayout(sidebar2);
        sidebar2.setLayout(sidebar2Layout);
        sidebar2Layout.setHorizontalGroup(
            sidebar2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sidebar2Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sidebar2Layout.createSequentialGroup()
                .addContainerGap(40, Short.MAX_VALUE)
                .addGroup(sidebar2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLogout7, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(sidebar2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnProduct2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnReport2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnUser2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSales2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCategory2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(40, 40, 40))
        );
        sidebar2Layout.setVerticalGroup(
            sidebar2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sidebar2Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel12)
                .addGap(45, 45, 45)
                .addComponent(btnUser2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnSales2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnReport2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnCategory2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(btnProduct2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 185, Short.MAX_VALUE)
                .addComponent(btnLogout7)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(sidebar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(sidebar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbCategoryIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCategoryIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCategoryIDActionPerformed

    private void btnSearchCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchCategoryActionPerformed
        String selected = cmbCategoryID.getSelectedItem().toString();
                if (selected.equalsIgnoreCase("All")) {
                    loadData(null, null);
                } else {
                    String catId = selected.split("-")[0];
                    loadData(catId, null);
                }
    }//GEN-LAST:event_btnSearchCategoryActionPerformed

    private void ButtonUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonUpdateActionPerformed
        int rowIndex = -1;
        int jumlahDicetang = 0;
        
        for (int i = 0; i < TableProduct.getRowCount(); i++) {
            boolean isChecked = (boolean) TableProduct.getValueAt(i, 0);
            if (isChecked) {
                rowIndex = i;
                jumlahDicetang++;
            }
        }
        
        if (jumlahDicetang == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih produk yang mau diedit dulu!");
            return;
        }
        if (jumlahDicetang > 1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Hanya bisa edit 1 produk dalam satu waktu!");
            return;
        }

        int idProduk    = (int) TableProduct.getValueAt(rowIndex, 1);
        String namaLama = (String) TableProduct.getValueAt(rowIndex, 3);
        String unitLama = (String) TableProduct.getValueAt(rowIndex, 4);
        double hargaLama= (double) TableProduct.getValueAt(rowIndex, 6);
        int stokLama    = (int) TableProduct.getValueAt(rowIndex, 7);

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.GridLayout(4, 2, 10, 10)); 
        
        javax.swing.JTextField txtNamaEdit  = new javax.swing.JTextField(namaLama);
        javax.swing.JTextField txtUnitEdit  = new javax.swing.JTextField(unitLama);
        javax.swing.JTextField txtHargaEdit = new javax.swing.JTextField(String.valueOf(hargaLama));
        javax.swing.JTextField txtStokEdit  = new javax.swing.JTextField(String.valueOf(stokLama));

        panel.add(new javax.swing.JLabel("Nama Produk:")); panel.add(txtNamaEdit);
        panel.add(new javax.swing.JLabel("Satuan (Unit):")); panel.add(txtUnitEdit);
        panel.add(new javax.swing.JLabel("Harga:")); panel.add(txtHargaEdit);
        panel.add(new javax.swing.JLabel("Stok:")); panel.add(txtStokEdit);

        int result = javax.swing.JOptionPane.showConfirmDialog(
                this, panel, "Edit Produk", 
                javax.swing.JOptionPane.OK_CANCEL_OPTION, 
                javax.swing.JOptionPane.PLAIN_MESSAGE
        );

        if (result == javax.swing.JOptionPane.OK_OPTION) {
            try {
                String namaBaru = txtNamaEdit.getText();
                String unitBaru = txtUnitEdit.getText();
                double hargaBaru = Double.parseDouble(txtHargaEdit.getText());
                int stokBaru = Integer.parseInt(txtStokEdit.getText());

                if(namaBaru.isEmpty() || unitBaru.isEmpty()) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Nama dan Unit tidak boleh kosong!");
                    return;
                }

                String hasil = controller.updateProduct(idProduk, namaBaru, unitBaru, hargaBaru, stokBaru);
                
                javax.swing.JOptionPane.showMessageDialog(this, hasil);
                
                btnSearchCategory.doClick(); 

            } catch (NumberFormatException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Harga dan Stok harus angka!");
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_ButtonUpdateActionPerformed

    private void ButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonDeleteActionPerformed
        List<Integer> idsToDelete = new ArrayList<>();
        
        for (int i = 0; i < TableProduct.getRowCount(); i++) {
            boolean isChecked = (boolean) TableProduct.getValueAt(i, 0); 
            if (isChecked) {
                int id = (int) TableProduct.getValueAt(i, 1); 
                idsToDelete.add(id);
            }
        }
        
        if (idsToDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Centang minimal satu produk untuk dihapus!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Yakin hapus " + idsToDelete.size() + " data?", 
                "Konfirmasi Hapus", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            
            String message = controller.deleteProducts(idsToDelete);
            
            JOptionPane.showMessageDialog(this, message);
            
            btnSearchCategory.doClick(); 
        }
    }//GEN-LAST:event_ButtonDeleteActionPerformed

    private void TextSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TextSearchActionPerformed

    private void btnUser2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUser2ActionPerformed
        UserFrame userFrame = new UserFrame();
        userFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnUser2ActionPerformed

    private void btnSales2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSales2ActionPerformed
        SalesFrame salesFrame = new SalesFrame();
        salesFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnSales2ActionPerformed

    private void btnReport2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReport2ActionPerformed
        ReportFrame reportFrame = new ReportFrame();
        reportFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnReport2ActionPerformed

    private void btnCategory2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCategory2ActionPerformed
        CategoryFrame categoryFrame = new CategoryFrame();
        categoryFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnCategory2ActionPerformed

    private void btnProduct2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProduct2ActionPerformed
        ProductFrame productFrame = new ProductFrame();
        productFrame.setVisible(true);

        this.dispose();
    }//GEN-LAST:event_btnProduct2ActionPerformed

    private void btnLogout7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogout7ActionPerformed
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
    }//GEN-LAST:event_btnLogout7ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
            java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ProductFrame().setVisible(true);
            }
        });
        java.awt.EventQueue.invokeLater(() -> new ProductFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ButtonDelete;
    private javax.swing.JButton ButtonUpdate;
    private javax.swing.JTable TableProduct;
    private javax.swing.JTextField TextSearch;
    private javax.swing.JButton btnCategory;
    private javax.swing.JButton btnCategory1;
    private javax.swing.JButton btnCategory2;
    private javax.swing.JButton btnLogout5;
    private javax.swing.JButton btnLogout6;
    private javax.swing.JButton btnLogout7;
    private javax.swing.JButton btnProduct;
    private javax.swing.JButton btnProduct1;
    private javax.swing.JButton btnProduct2;
    private javax.swing.JButton btnReport;
    private javax.swing.JButton btnReport1;
    private javax.swing.JButton btnReport2;
    private javax.swing.JButton btnSales;
    private javax.swing.JButton btnSales1;
    private javax.swing.JButton btnSales2;
    private javax.swing.JButton btnSearchCategory;
    private javax.swing.JButton btnUser;
    private javax.swing.JButton btnUser1;
    private javax.swing.JButton btnUser2;
    private javax.swing.JComboBox<String> cmbCategoryID;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel sidebar;
    private javax.swing.JPanel sidebar1;
    private javax.swing.JPanel sidebar2;
    // End of variables declaration//GEN-END:variables

    private void initCustomTable() {
            String[] columnNames = {
            "Select", "ID", "Category ID", "Name", "Unit", 
            "Code", "Price", "Stock", "CreatedAt"
        };

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; 
                if (columnIndex == 6 || columnIndex == 7) return Integer.class;
                return super.getColumnClass(columnIndex);
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; 
            }
        };

        TableProduct.setModel(model);
        TableProduct.getColumnModel().getColumn(0).setPreferredWidth(50);
        TableProduct.getColumnModel().getColumn(0).setMaxWidth(50);
    }
}

