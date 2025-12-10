package fafbar.model;

import java.math.BigDecimal;

/**
 * Model untuk merepresentasikan SATU ITEM penjualan (satu baris di sales_details)
 * Dibuat berdasarkan struktur tabel sales_details.
 */
public class SaleDetail {

    private int id;
    private int saleId; // Tidak digunakan di DB saat ini, tapi bagus untuk model
    private String productCode;
    private String productName;
    private int quantity;
    private BigDecimal priceAtSale; // Menggunakan price
    private BigDecimal discountPerItem; // Ada di model tapi tidak di DB, set ke ZERO
    private BigDecimal subtotal;

    // Constructors
    public SaleDetail() {}

    // Getters
    public int getId() { return id; }
    public int getSaleId() { return saleId; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPriceAtSale() { return priceAtSale; }
    public BigDecimal getDiscountPerItem() { return discountPerItem; }
    public BigDecimal getSubtotal() { return subtotal; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPriceAtSale(BigDecimal priceAtSale) { this.priceAtSale = priceAtSale; }
    public void setDiscountPerItem(BigDecimal discountPerItem) { this.discountPerItem = discountPerItem; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}