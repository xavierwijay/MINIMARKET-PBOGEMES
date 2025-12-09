package fafbar;

import fafbar.view.auth.LoginFrame;

public class Fafbar {

    public static void main(String[] args) {
        // Kita panggil LoginFrame sebagai pintu gerbang utama
        LoginFrame login = new LoginFrame();
        
        // Taruh di tengah layar
        login.setLocationRelativeTo(null);
        
        // Tampilkan
        login.setVisible(true);
    }
    
}