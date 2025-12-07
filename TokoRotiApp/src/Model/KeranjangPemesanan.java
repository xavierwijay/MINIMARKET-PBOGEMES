
package Model;

/**
 *
 * @author LENOVO
 */
public class KeranjangPemesanan {
     private Product product;
    private int jumlahPesanan;   

    public KeranjangPemesanan(Product product, int jumlahPesanan) {
        this.product = product;
        this.jumlahPesanan = jumlahPesanan;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getJumlahPesanan() { return jumlahPesanan; }
    public void setJumlahPesanan(int jumlahPesanan) { this.jumlahPesanan = jumlahPesanan; }

    public double getSubtotal() {
        return product.getPrice() * jumlahPesanan;
    }
}

