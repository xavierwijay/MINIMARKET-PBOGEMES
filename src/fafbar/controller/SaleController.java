package fafbar.controller;

import fafbar.model.Product;
import fafbar.config.DBConnection;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// === IMPORT KHUSUS UNTUK MEMBUKA PDF SECARA OTOMATIS ===
import java.awt.Desktop; 
import java.io.IOException;
// ========================================================

// =================================================================
// iText 5 IMPORTS
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Font;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import java.io.FileOutputStream;
import java.io.File; 
// =================================================================

public class SaleController {
    
    // -------------------------------------------------------------------------
    // METHOD 1: cariProdukByKodeAtauNama() - SUDAH DIPERBAIKI (CASE INSENSITIVE)
    // -------------------------------------------------------------------------
    
    public Product cariProdukByKodeAtauNama(String input) {
        // PERBAIKAN 1: Tambahkan LOWER() agar tidak peduli huruf besar/kecil
        // PERBAIKAN 2: Pastikan nama kolom sesuai (Code vs code). Saya samakan dengan Repository (Code)
        String sql = "SELECT id, Code, name, price, stock FROM tbproduct WHERE LOWER(Code) = LOWER(?) OR LOWER(name) LIKE LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, input);
            ps.setString(2, "%" + input + "%"); 

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setCode(rs.getString("Code")); // Sesuaikan nama kolom DB
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getDouble("price")); 
                    p.setStock(rs.getInt("stock"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error saat mencari produk: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error umum saat mencari produk: " + e.getMessage());
            e.printStackTrace();
        }
        return null; 
    }

    // -------------------------------------------------------------------------
    // METHOD 2: generateInvoice()
    // -------------------------------------------------------------------------

    public String generateInvoice() {
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        String lastInvoice = null;
        String sql = "SELECT receipt_number FROM sales ORDER BY id DESC LIMIT 1"; 

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lastInvoice = rs.getString("receipt_number");
            }
        } catch (SQLException e) {
            System.err.println("Gagal ambil invoice terakhir: " + e.getMessage());
        }
        
        int newId = 1;
        
        if (lastInvoice != null && lastInvoice.length() > 4) {
            try {
                String numPart = lastInvoice.substring(lastInvoice.lastIndexOf("-") + 1);
                newId = Integer.parseInt(numPart) + 1;
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            }
        }
        
        String formattedId = String.format("%04d", newId); 
        return "INV-" + datePart + "-" + formattedId;
    }


    // -------------------------------------------------------------------------
    // METHOD 3: cetakStrukPDF() - PERBAIKAN AUTO OPEN
    // -------------------------------------------------------------------------

    public String cetakStrukPDF(String invoiceNumber, String kasirName, 
                                 List<Object[]> itemsData, 
                                 double subTotal, double discount, 
                                 double grandTotal, double cash, double change) {
        
        // 1. Definisikan Nama dan Lokasi File
        String namaFile = "Struk_" + invoiceNumber.replace("-", "_") + ".pdf";
        String folderPath = "Struk_Penjualan/"; 

        try {
            // Cek dan buat folder jika belum ada
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String fullPath = folderPath + namaFile;

            // 2. Inisialisasi Document iText
            Document document = new Document(new Rectangle(280f, 842f)); 
            document.setMargins(10, 10, 10, 10); 

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fullPath));
            document.open();

            // ... (Bagian Font dan Konten Struk sama seperti sebelumnya) ...
            Font fontHeader = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
            Font fontTitle = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
            Font fontNormal = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK);
            Font fontSmall = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);
            
            Paragraph p = new Paragraph("FAFBAR JAYA SHOP", fontTitle);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);
            
            p = new Paragraph("Jl. Diponegoro No. 123, Salatiga", fontNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);
            
            p = new Paragraph("Telp. 08123xxxx", fontNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingAfter(5f);
            document.add(p);
            
            document.add(new Paragraph("----------------------------------------------------------------", fontSmall));
            
            document.add(new Paragraph("No. Inv: " + invoiceNumber, fontNormal));
            document.add(new Paragraph("Kasir: " + kasirName, fontNormal));
            document.add(new Paragraph("Tgl  : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), fontNormal));
            
            document.add(new Paragraph("----------------------------------------------------------------", fontSmall));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 3f, 1.5f}); 
            
            for (Object[] item : itemsData) {
                String namaItem = item[1].toString();
                int qty = Integer.parseInt(item[2].toString());
                double harga = Double.parseDouble(item[3].toString()); 
                double subtotalItem = Double.parseDouble(item[5].toString()); 
                
                PdfPCell cellName = new PdfPCell(new Phrase(namaItem, fontNormal));
                cellName.setColspan(3);
                cellName.setBorder(Rectangle.NO_BORDER);
                table.addCell(cellName);

                PdfPCell cellQty = new PdfPCell(new Phrase(qty + " x", fontNormal));
                cellQty.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellQty.setBorder(Rectangle.NO_BORDER);
                table.addCell(cellQty);
                
                PdfPCell cellDetail = new PdfPCell(new Phrase(String.format("Rp %,.0f", harga), fontNormal));
                cellDetail.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellDetail.setBorder(Rectangle.NO_BORDER);
                table.addCell(cellDetail);
                
                PdfPCell cellSubtotal = new PdfPCell(new Phrase(String.format("Rp %,.0f", subtotalItem), fontNormal));
                cellSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellSubtotal.setBorder(Rectangle.NO_BORDER);
                table.addCell(cellSubtotal);
            }
            
            document.add(table);
            document.add(new Paragraph("----------------------------------------------------------------", fontSmall));
            
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{3f, 2f});
            
            totalTable.addCell(createCell("SUBTOTAL", fontNormal, Element.ALIGN_LEFT));
            totalTable.addCell(createCell(String.format("Rp %,.0f", subTotal), fontNormal, Element.ALIGN_RIGHT));
            totalTable.addCell(createCell("DISKON", fontNormal, Element.ALIGN_LEFT));
            totalTable.addCell(createCell(String.format("Rp %,.0f", discount), fontNormal, Element.ALIGN_RIGHT));
            totalTable.addCell(createCell("----------------------", fontSmall, Element.ALIGN_LEFT, 2));
            totalTable.addCell(createCell("GRAND TOTAL", fontHeader, Element.ALIGN_LEFT));
            totalTable.addCell(createCell(String.format("Rp %,.0f", grandTotal), fontHeader, Element.ALIGN_RIGHT));
            totalTable.addCell(createCell("BAYAR (CASH)", fontNormal, Element.ALIGN_LEFT));
            totalTable.addCell(createCell(String.format("Rp %,.0f", cash), fontNormal, Element.ALIGN_RIGHT));
            totalTable.addCell(createCell("KEMBALIAN", fontNormal, Element.ALIGN_LEFT));
            totalTable.addCell(createCell(String.format("Rp %,.0f", change), fontNormal, Element.ALIGN_RIGHT));
            
            document.add(totalTable);
            document.add(new Paragraph("----------------------------------------------------------------", fontSmall));
            
            p = new Paragraph("*** Terima Kasih Telah Berbelanja ***", fontNormal);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            document.close();
            writer.close();
            
            // ========================================================
            // PERBAIKAN UTAMA: MEMBUKA FILE PDF
            // ========================================================
            try {
                // Menggunakan getAbsoluteFile() agar path-nya jelas terbaca oleh System
                File pdfFile = new File(fullPath).getAbsoluteFile(); 
                
                if (Desktop.isDesktopSupported()) {
                    if (pdfFile.exists()) {
                        Desktop.getDesktop().open(pdfFile);
                        System.out.println("Struk PDF berhasil dibuka: " + pdfFile.getAbsolutePath());
                    } else {
                        System.out.println("File PDF tidak ditemukan saat akan dibuka: " + pdfFile.getAbsolutePath());
                    }
                } else {
                    System.out.println("Fitur Desktop tidak didukung di sistem ini.");
                }
            } catch (IOException e) {
                System.err.println("Error saat mencoba membuka file otomatis: " + e.getMessage());
            }
            // ========================================================

            return namaFile; 

        } catch (Exception e) {
            System.err.println("Error saat mencetak/membuat file struk: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private PdfPCell createCell(String content, Font font, int alignment) {
        return createCell(content, font, alignment, 1);
    }
    
    private PdfPCell createCell(String content, Font font, int alignment, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setColspan(colspan);
        return cell;
    }
}