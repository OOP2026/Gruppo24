package gui;

import controller.Controller;
import dao.DAOException;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final transient Controller controller;

    private JTextField loginField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame(Controller controller) {
        this.controller = controller;
        buildUI();
        configFrame();
    }

    private void buildUI() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 10));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        JLabel titleLabel = new JLabel("Accesso Utente Ospedale", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        rootPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel loginLabel = new JLabel("Login:");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(loginLabel, gbc);

        loginField = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(loginField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(18);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        formPanel.add(passwordField, gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 4));

        JButton loginButton = new JButton("Accedi");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(loginButton);
        bottomPanel.add(btnPanel, BorderLayout.NORTH);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());
        getRootPane().setDefaultButton(loginButton);

        add(rootPanel);
    }

    private void configFrame() {
        setTitle("Login – Gestione Ospedale");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private void handleLogin() {
        String login    = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Inserire login e password.");
            return;
        }

        try {
            boolean ok = controller.login(login, password);
            if (!ok) {
                statusLabel.setText("Credenziali non valide. Riprovare.");
                passwordField.setText("");
                loginField.requestFocus();
            } else {
                dispose();
                new MainFrame(controller).setVisible(true);
            }
        } catch (DAOException e) {
            statusLabel.setText("Errore di connessione al database.");
            JOptionPane.showMessageDialog(this,
                "Impossibile connettersi al database.\n" + e.getMessage(),
                "Errore DB", JOptionPane.ERROR_MESSAGE);
        }
    }
}
