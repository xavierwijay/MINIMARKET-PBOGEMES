package fafbar.controller;

import fafbar.config.DBConnection;
import fafbar.model.Product;
import fafbar.repository.SaleRepository;

// --- IMPORT ITEXT 5 ---
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;

public class SaleController {
    
    private final SaleRepository saleRepo = new SaleRepository();
    private final NumberFormat currencyFmt;

    // Font Khusus Struk (Courier Monospaced)
    private static final Font FONT_HEADER = new Font(Font.FontFamily.COURIER, 14, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.FontFamily.COURIER, 10, Font.NORMAL);
    private static final Font FONT_BOLD = new Font(Font.FontFamily.COURIER, 10, Font.BOLD);

    public SaleController() {
        // Format Rupiah
        currencyFmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        currencyFmt.setMaximumFractionDigits(0);
    }

    // --- 1. Cari Produk (TETAP SAMA) ---
    public Product cariProdukByKodeAtauNama(String input) {
        String sql = "SELECT id, Code, name, price, stock, Unit FROM tbproduct WHERE Code = ? OR name LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, input);
            ps.setString(2, "%" + input + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setCode(rs.getString("Code"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setStock(rs.getInt("stock"));
                    p.setUnit(rs.getString("Unit"));
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- 2. Generate Invoice (TETAP SAMA) ---
    public String generateInvoice() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String lastInv = saleRepo.getLastInvoiceNumber();
        int sequence = 1;
        if (lastInv != null && lastInv.contains(datePart)) {
            try {
                String seqStr = lastInv.substring(lastInv.lastIndexOf("-") + 1);
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (Exception e) { sequence = 1; }
        }
        return String.format("INV-%s-%04d", datePart, sequence);
    }

    // --- 3. Proses Transaksi (TETAP SAMA) ---
    public boolean processSale(String invoice, String txtSubtotal, String txtDiscount, 
                               String txtGrandTotal, String txtCash, String txtChange, 
                               int userId, List<Object[]> items) {
        try {
            if (items.isEmpty()) return false;
            double subtotal = Double.parseDouble(txtSubtotal);
            double discount = txtDiscount.isEmpty() ? 0 : Double.parseDouble(txtDiscount);
            double grandTotal = Double.parseDouble(txtGrandTotal);
            double cash = txtCash.isEmpty() ? 0 : Double.parseDouble(txtCash);
            double change = Double.parseDouble(txtChange);

            if (cash < grandTotal) return false;

            return saleRepo.saveTransaction(invoice, subtotal, discount, grandTotal, cash, change, userId, items);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 4. CETAK STRUK (Updated: PDF iText 5) ---
    // Nama method tetap 'cetakStruk' agar SalesFrame tidak error
    public void cetakStruk(String invoice, String kasir, List<Object[]> items, 
                           double subtotal, double discount, double grandTotal, 
                           double cash, double change) {
        Document document = new Document();
        try {
            // Lokasi File PDF
            String dest = "Struk_" + invoice + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(dest));
            
            document.open();

            // --- HEADER ---
            Paragraph pHeader = new Paragraph("FAFBAR JAYA SHOP", FONT_HEADER);
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            Paragraph pAddr = new Paragraph("Jl. Diponegoro No. 123, Salatiga\nTelp. 08123xxxx", FONT_NORMAL);
            pAddr.setAlignment(Element.ALIGN_CENTER);
            document.add(pAddr);
            
            // Garis Pemisah (Dashes)
            document.add(new Paragraph("-----------------------------------------------------------------", FONT_NORMAL));

            // --- INFO TRANSAKSI ---
            Paragraph pInfo = new Paragraph(
                "No. Inv : " + invoice + "\n" +
                "Kasir   : " + kasir + "\n" +
                "Tgl     : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), FONT_NORMAL);
            document.add(pInfo);
            
            document.add(new Paragraph("-----------------------------------------------------------------", FONT_NORMAL));

            // --- TABEL ITEM BARANG ---
            // Kita gunakan tabel tanpa border untuk layout rapi
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{70, 30}); // Kolom Kiri 70%, Kanan 30%

            for (Object[] item : items) {
                String nama = (String) item[1];
                int qty = (int) item[2];
                double price = (double) item[3];
                double totalItem = (double) item[5];

                // Cell Kiri: Nama Produk & Detail Qty x Harga
                PdfPCell cellDesc = new PdfPCell();
                cellDesc.setBorder(Rectangle.NO_BORDER);
                cellDesc.addElement(new Paragraph(nama, FONT_BOLD)); // Nama Barang Tebal
                cellDesc.addElement(new Paragraph(qty + " x " + currencyFmt.format(price), FONT_NORMAL));
                
                // Cell Kanan: Total Harga per Item
                PdfPCell cellTotal = new PdfPCell(new Phrase(currencyFmt.format(totalItem), FONT_NORMAL));
                cellTotal.setBorder(Rectangle.NO_BORDER);
                cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);

                table.addCell(cellDesc);
                table.addCell(cellTotal);
            }
            document.add(table);

            document.add(new Paragraph("-----------------------------------------------------------------", FONT_NORMAL));

            // --- TABEL TOTAL PEMBAYARAN ---
            PdfPTable tableTotal = new PdfPTable(2);
            tableTotal.setWidthPercentage(100);
            tableTotal.setWidths(new float[]{50, 50});

            addTotalRow(tableTotal, "SUBTOTAL", subtotal, false);
            if (discount > 0) {
                addTotalRow(tableTotal, "DISKON", discount, false);
            }
            addTotalRow(tableTotal, "GRAND TOTAL", grandTotal, true); // Bold Grand Total
            addTotalRow(tableTotal, "BAYAR (CASH)", cash, false);
            addTotalRow(tableTotal, "KEMBALIAN", change, false);

            document.add(tableTotal);
            
            document.add(new Paragraph("-----------------------------------------------------------------", FONT_NORMAL));

            // --- FOOTER ---
            Paragraph pFooter = new Paragraph("*** Terima Kasih Telah Berbelanja ***", FONT_NORMAL);
            pFooter.setAlignment(Element.ALIGN_CENTER);
            pFooter.setSpacingBefore(10);
            document.add(pFooter);

            document.close();

            // Otomatis Buka File PDF
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(dest));
            }

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal mencetak PDF: " + e.getMessage());
        }
    }

    // Helper untuk membuat baris total rapi
    private void addTotalRow(PdfPTable table, String label, double value, boolean isBold) {
        Font f = isBold ? FONT_BOLD : FONT_NORMAL;
        
        // Label (Rata Kanan di kolom kiri)
        PdfPCell cLabel = new PdfPCell(new Phrase(label, f));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setHorizontalAlignment(Element.ALIGN_RIGHT); // Supaya mepet ke angka
        
        // Value (Rata Kanan di kolom kanan)
        PdfPCell cValue = new PdfPCell(new Phrase(currencyFmt.format(value), f));
        cValue.setBorder(Rectangle.NO_BORDER);
        cValue.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(cLabel);
        table.addCell(cValue);
    }
}