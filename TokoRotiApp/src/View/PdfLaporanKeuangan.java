package View;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfLaporanKeuangan {

    private static final NumberFormat rupiah =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    /**
     * Membuat PDF laporan keuangan dari isi TableModel dan total pemasukan.
     * @param model          model tabel yang berisi data transaksi
     * @param totalPemasukan total pemasukan (dalam double)
     */
    public static File buatLaporan(TableModel model, double totalPemasukan)
            throws IOException, DocumentException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String fileName = "LaporanKeuangan-" + timeStamp + ".pdf";

        // Simpan di folder Home user
        File file = new File(System.getProperty("user.home"), fileName);

        Document doc = new Document(PageSize.A4.rotate()); // landscape
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // Judul
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("LAPORAN KEUANGAN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        doc.add(title);

        // Info waktu cetak
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 10);
        Paragraph info = new Paragraph(
                "Dicetak: " + new SimpleDateFormat("dd MMMM yyyy HH:mm").format(new Date()),
                infoFont);
        info.setAlignment(Element.ALIGN_RIGHT);
        info.setSpacingAfter(10f);
        doc.add(info);

        // Tabel
        PdfPTable table = new PdfPTable(model.getColumnCount());
        table.setWidthPercentage(100f);
        table.setHeaderRows(1);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font cellFont   = new Font(Font.FontFamily.HELVETICA, 9);

        // Header kolom
        for (int col = 0; col < model.getColumnCount(); col++) {
            PdfPCell cell = new PdfPCell(new Phrase(
                    String.valueOf(model.getColumnName(col)), headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5f);
            table.addCell(cell);
        }

        // Data
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                Object value = model.getValueAt(row, col);
                String text;

                if (value == null) {
                    text = "";
                } else if (col == model.getColumnCount() - 1 && value instanceof Number) {
                    // kolom terakhir = total (format rupiah)
                    text = rupiah.format(((Number) value).doubleValue());
                } else {
                    text = value.toString();
                }

                PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
                cell.setPadding(4f);
                table.addCell(cell);
            }
        }

        doc.add(table);

        // Total pemasukan
        Paragraph totalPara = new Paragraph(
                "Total Pemasukan: " + rupiah.format(totalPemasukan),
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
        totalPara.setAlignment(Element.ALIGN_RIGHT);
        totalPara.setSpacingBefore(15f);
        doc.add(totalPara);

        doc.close();
        return file;
    }
}
