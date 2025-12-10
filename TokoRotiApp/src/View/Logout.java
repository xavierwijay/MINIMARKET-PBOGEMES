
package View;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Logout {
        public static void kembaliKeLogin(JFrame currentFrame) {
        int confirm = JOptionPane.showConfirmDialog(
                currentFrame,
                "Yakin ingin keluar ke halaman login?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // kalau punya session, bisa clear di sini
            // Controller.SessionUser.clear();

            new Login().setVisible(true);
            currentFrame.dispose();
        }
    }
}
