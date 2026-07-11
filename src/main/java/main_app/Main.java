package main_app;

import controller.Controller;
import gui.LoginFrame;

import javax.swing.*;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {

        Logger logger = Logger.getLogger(Main.class.getName());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.info("Impossibile impostare il Look and Feel di sistema: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            Controller controller = new Controller();
            new LoginFrame(controller).setVisible(true);
        });
    }
}
