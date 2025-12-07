package utility;

import Controller.KeranjangController;
import Model.KeranjangPemesanan;
import Model.Product;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class PdfStruk {

    private static final NumberFormat RUPIAH =
        NumberFormat.getCurrencyInstance(new Locale("id","ID"));
    private static final SimpleDateFormat SDF_TGL =
        new SimpleDateFormat("yyyy-MM-dd", new Locale("id","ID"));
    private static final SimpleDateFormat SDF_TS =
        new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final SimpleDateFormat SDF_WAKTU =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private PdfStruk() {}

    public static File buatStruk(String namaPembeli, Date tanggalAmbil) throws Exception {
        String ts = SDF_TS.format(new Date());
        File out = new File(System.getProperty("user.home"),
                "Struk_TokoRoti_NOTABLE_" + ts + ".pdf");

        Document doc = new Document(PageSize.A5, 36, 36, 28, 24);
        PdfWriter.getInstance(doc, new FileOutputStream(out));
        doc.open();

        // Fonts
        Font brand   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font title   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normal  = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font bold    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font bigBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font small   = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);

        // Header
        Paragraph pBrand = new Paragraph("TOKO ROTI SANDX", brand);
        pBrand.setAlignment(Element.ALIGN_CENTER);
        doc.add(pBrand);

        Paragraph pTitle = new Paragraph("STRUK PEMBAYARAN", title);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        doc.add(pTitle);
        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);

        // Detail (paragraf saja, bukan tabel)
        doc.add(detailRow("Nama Pelanggan", (isEmpty(namaPembeli) ? "-" : namaPembeli), bold, normal));
        doc.add(detailRow("Tanggal & Waktu", SDF_WAKTU.format(new Date()), bold, normal));
        doc.add(detailRow("Metode Pembayaran", "Transfer BCA", bold, normal));
        if (tanggalAmbil != null) {
            doc.add(detailRow("Tanggal Ambil", SDF_TGL.format(tanggalAmbil), bold, normal));
        }

        doc.add(Chunk.NEWLINE);
        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);

        // Item: "Nama × qty @Rp... = Rp..."
        double total = 0.0;
        for (KeranjangPemesanan it : KeranjangController.getInstance().getItems()) {
            Product p = it.getProduct();
            int qty = it.getJumlahPesanan();
            double sub = it.getSubtotal();
            total += sub;

            String line = String.format("%s × %d @%s = %s",
                    p.getName(), qty,
                    RUPIAH.format(p.getPrice()),
                    RUPIAH.format(sub));
            Paragraph row = new Paragraph(line, normal);
            row.setAlignment(Element.ALIGN_LEFT);
            doc.add(row);
        }

        doc.add(Chunk.NEWLINE);
        doc.add(new LineSeparator());

        Paragraph pTotal = new Paragraph("TOTAL BAYAR: " + RUPIAH.format(total), bigBold);
        pTotal.setAlignment(Element.ALIGN_LEFT);
        doc.add(pTotal);

        doc.add(Chunk.NEWLINE);
        Paragraph thanks = new Paragraph("Terima kasih atas kunjungannya!", normal);
        thanks.setAlignment(Element.ALIGN_CENTER);
        doc.add(thanks);

        doc.add(Chunk.NEWLINE);
        Paragraph acct = new Paragraph("Pembayaran ke BCA 1234567890 a/n Toko Roti SANDX", small);
        acct.setAlignment(Element.ALIGN_CENTER);
        doc.add(acct);

        Paragraph foot = new Paragraph("Dicetak oleh Aplikasi Toko Roti SANDX", small);
        foot.setAlignment(Element.ALIGN_CENTER);
        doc.add(foot);

        doc.close();
        return out;
    }

    private static Paragraph detailRow(String label, String value, Font fLbl, Font fVal) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " : ", fLbl));
        p.add(new Chunk(value, fVal));
        p.setAlignment(Element.ALIGN_LEFT);
        return p;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
