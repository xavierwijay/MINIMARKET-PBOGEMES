/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fafbar.controller;
import fafbar.model.Category;
import fafbar.model.Product;
import fafbar.repository.CategoryRepository;
import fafbar.repository.ProductRepository;
/**
 *
 * @author Lenovo
 */
public class CategoryController {
    private CategoryRepository catRepo = new CategoryRepository();
    private ProductRepository prodRepo = new ProductRepository();

    // Logic untuk Tombol Search
    public Category searchCategory(String id) {
        // Membersihkan ID dari strip "-" jika ada
        if(id.contains("-")) {
            id = id.split("-")[0];
        }
        return catRepo.findById(id);
    }

    // Logic untuk Tombol Add (Menyimpan Kategori & Produk)
    public String saveTransaction(Category cat, Product prod) {
        try {
            // 1. Coba Simpan Kategori dulu (Repository akan handle kalau duplikat)
            catRepo.save(cat);
            
            // 2. Jika Produk Kosong, selesai sampai sini
            if (prod.getName().isEmpty()) {
                return "Kategori berhasil diperbarui/disimpan (Tanpa Produk).";
            }
            
            // 3. Generate Kode Produk Unik
            long angkaAcak = System.currentTimeMillis() % 1000;
            String autoCode = "P" + cat.getId() + "-" + angkaAcak;
            prod.setCode(autoCode);
            prod.setCategoryId(cat.getId()); // Pastikan ID nyambung

            // 4. Simpan Produk
            boolean isProdSaved = prodRepo.save(prod);
            
            if (isProdSaved) {
                return "Sukses! Produk: " + prod.getName() + " | Kode: " + autoCode;
            } else {
                return "Gagal menyimpan produk ke database.";
            }

        } catch (Exception e) {
            return "Error Controller: " + e.getMessage();
        }
    }
    
    public String getAutoId() {
        int lastId = catRepo.getLastId();
        int newId = lastId + 1;
        return String.valueOf(newId); // Ubah jadi String biar bisa masuk ComboBox
    }
}
