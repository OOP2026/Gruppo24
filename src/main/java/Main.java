import controller.Controller;
import gui.LoginFrame;

import javax.swing.*;

/**
 * Entry point dell'applicazione Gestione Ospedale.
 * Avvia la LoginFrame sul thread EDT di Swing.
 */
public class Main {
    public static void main(String[] args) {
        // Usa il look-and-feel nativo del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            Controller controller = new Controller();
            LoginFrame loginFrame = new LoginFrame(controller);
            loginFrame.setVisible(true);
        });
    }
}
