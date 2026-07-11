package gui;

import controller.Controller;
import gui.panels.AdminPanel;
import gui.panels.MedicoPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final Controller controller;

    public MainFrame(Controller controller) {
        this.controller = controller;
        buildUI();
        configFrame();
    }

    private void buildUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(buildTopBar(), BorderLayout.NORTH);

        JPanel mainPanel = controller.isAmministratore()
                ? new AdminPanel(controller)
                : new MedicoPanel(controller);
        rootPanel.add(mainPanel, BorderLayout.CENTER);

        add(rootPanel);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(41, 128, 185));
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        String nomeUtente;
        if (controller.isMedico()) {
            nomeUtente = controller.getMedicoCorrente().getNome()
                    + " " + controller.getMedicoCorrente().getCognome()
                    + " – " + controller.getNomeRepartoMedicoCorrente();
        } else {
            nomeUtente = "Amministratore: " + controller.getAmministratoreCorrente().getLogin();
        }

        JLabel userLabel = new JLabel(nomeUtente);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        topBar.add(userLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Gestione Ospedale", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topBar.add(titleLabel, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> handleLogout());
        topBar.add(logoutBtn, BorderLayout.EAST);

        return topBar;
    }

    private void handleLogout() {
        controller.logout();
        dispose();
        new LoginFrame(controller).setVisible(true);
    }

    private void configFrame() {
        setTitle("Gestione Ospedale");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
    }
}
