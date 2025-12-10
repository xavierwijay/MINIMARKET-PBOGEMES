package fafbar;

import fafbar.view.auth.LoginFrame;
import fafbar.view.category.CategoryFrame; // Pastikan ini tetap ada jika dibutuhkan

public class Fafbar {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // --- INI ADALAH START UTAMA APLIKASI ---
        
        // Memulai dari LoginFrame
        var loginFrame = new LoginFrame(); 
        
        loginFrame.setLocationRelativeTo(null); // Tampilkan di tengah
        loginFrame.setVisible(true);
        
        // HAPUS ATAU KOMENTARI KODE LAMA ANDA:
        // var categoryFrame = new CategoryFrame();
        // categoryFrame.setExtendedState(CategoryFrame.MAXIMIZED_BOTH);
        // categoryFrame.setVisible(true);
    }
    
}