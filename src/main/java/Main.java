import controller.Controller;
import gui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        } catch (Exception e) {
            System.err.println("Impossibile impostare il Look and Feel di sistema: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            Controller controller = new Controller();
            new LoginFrame(controller).setVisible(true);
        });
    }
}
