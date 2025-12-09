/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fafbar.model;

import java.util.Date;

public class Product {
    private int id;          
    private String categoryId;
    private String name;
    private String unit;
    private String code; // Field sudah benar
    private double price;
    private int stock;
    private Date createdAt; 

    // --- GETTER SETTER ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    // TIDAK ADA PERUBAHAN DI SINI KARENA SUDAH BENAR:
    public String getCode() { return code; } 
    public void setCode(String code) { this.code = code; } 

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}