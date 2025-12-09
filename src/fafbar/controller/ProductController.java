/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fafbar.controller;

import fafbar.model.Product;
import fafbar.repository.ProductRepository;
import java.util.List;
import fafbar.model.Category;

public class ProductController {
    
    private ProductRepository repo = new ProductRepository();

    // Fitur: Cari Data (Load Tabel)
    public List<Product> searchProducts(String categoryId, String keyword) {
        // Bersihkan ID Kategori dari dropdown (misal "7-Minuman" jadi "7")
        if (categoryId != null) {
            if (categoryId.equalsIgnoreCase("All")) {
                categoryId = null; // Null artinya ambil semua
            } else if (categoryId.contains("-")) {
                categoryId = categoryId.split("-")[0];
            }
        }
        return repo.findAll(categoryId, keyword);
    }

    // Fitur: Hapus Data
    public String deleteProducts(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return "Tidak ada data yang dipilih!";
        }
        boolean success = repo.deleteBatch(ids);
        return success ? "Data berhasil dihapus!" : "Gagal menghapus data.";
    }

    // Fitur: Update Data
    public String updateProduct(int id, String name, String unit, double price, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setUnit(unit);
        p.setPrice(price);
        p.setStock(stock);
        
        boolean success = repo.update(p);
        return success ? "Data berhasil diupdate!" : "Gagal update data.";
    }
}

