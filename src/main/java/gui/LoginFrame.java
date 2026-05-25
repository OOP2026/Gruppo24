package gui;

import controller.Controller;
import model.Utente;

import javax.swing.*;
import java.awt.*;



public class LoginFrame extends JFrame {

    private final Controller controller;


    private JPanel  rootPanel;
    private JLabel  titleLabel;
    private JLabel  loginLabel;
    private JLabel  passwordLabel;
    private JTextField    loginField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel  statusLabel;

    public LoginFrame(Controller controller) {
        this.controller = controller;
        buildUI();
        configFrame();
    }

    private void buildUI() {
        rootPanel = new JPanel(new BorderLayout(0, 10));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        titleLabel = new JLabel("Acesso Utente Ospedale", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        rootPanel.add(titleLabel, BorderLayout.NORTH);


        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        loginLabel = new JLabel("Login:");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(loginLabel, gbc);

        loginField = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(loginField, gbc);

        passwordLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(18);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        formPanel.add(passwordField, gbc);

        rootPanel.add(formPanel, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new BorderLayout(0, 4));

        loginButton = new JButton("Accedi");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(loginButton);
        bottomPanel.add(btnPanel, BorderLayout.NORTH);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        rootPanel.add(bottomPanel, BorderLayout.SOUTH);


        JLabel hint = new JLabel(
            "<html><small><i>Demo: admin/admin123 · dr.rossi/pass123 · dr.bianchi/pass789</i></small></html>",
            SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        bottomPanel.add(hint, BorderLayout.CENTER);


        loginButton.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());
        getRootPane().setDefaultButton(loginButton);

        add(rootPanel);
    }

    private void configFrame() {
        setTitle("Login – Gestione Ospedale");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        Utente utente = controller.login(login, password);
        if (utente == null) {
            statusLabel.setText("Credenziali non valide. Riprovare.");
            passwordField.setText("");
            loginField.requestFocus();
        } else {
            dispose();
            new MainFrame(controller).setVisible(true);
        }
    }
}
