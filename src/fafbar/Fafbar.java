/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package fafbar;

import fafbar.view.auth.LoginFrame;
import fafbar.view.category.CategoryFrame;

public class Fafbar {

    /**
     * @param args the command line argumerepornts
     */
    public static void main(String[] args) {
        var categoryFrame = new CategoryFrame();
        
        categoryFrame.setExtendedState(CategoryFrame.MAXIMIZED_BOTH);
        categoryFrame.setVisible(true);
    }
    
}
