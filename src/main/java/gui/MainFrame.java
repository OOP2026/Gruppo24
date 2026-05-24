package gui;

import controller.Controller;
import gui.panels.AdminPanel;
import gui.panels.MedicoPanel;
import model.Medico;

import javax.swing.*;
import java.awt.*;

/**
 * Finestra principale dell'applicazione.
 * Mostra AdminPanel o MedicoPanel in base al ruolo dell'utente loggato.
 */
public class MainFrame extends JFrame {

    private final Controller controller;

    public MainFrame(Controller controller) {
        this.controller = controller;
        buildUI();
        configFrame();
    }

    private void buildUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());

        // ── Barra superiore ──────────────────────────────────────────────────
        JPanel topBar = buildTopBar();
        rootPanel.add(topBar, BorderLayout.NORTH);

        // ── Pannello principale in base al ruolo ─────────────────────────────
        JPanel mainPanel;
        if (controller.isAmministratore()) {
            mainPanel = new AdminPanel(controller);
        } else {
            mainPanel = new MedicoPanel(controller);
        }
        rootPanel.add(mainPanel, BorderLayout.CENTER);

        add(rootPanel);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(41, 128, 185));
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        // Nome utente a sinistra
        String nomeUtente;
        if (controller.isMedico()) {
            Medico m = controller.getMedicoCorrente();
            nomeUtente = "Dr. " + m.getNome() + " " + m.getCognome()
                    + " – " + m.getReparto().getNome();
        } else {
            nomeUtente = "Amministratore: " + controller.getUtenteCorrente().getLogin();
        }
        JLabel userLabel = new JLabel(nomeUtente);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        topBar.add(userLabel, BorderLayout.WEST);

        // Titolo al centro
        JLabel titleLabel = new JLabel("Gestione Ospedale", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        topBar.add(titleLabel, BorderLayout.CENTER);

        // Bottone logout a destra
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
        setSize(1000, 680);
        setLocationRelativeTo(null);
    }
}
