package gui.panels.admin;

import controller.Controller;
import gui.utils.GuiUtils;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static gui.utils.GuiUtils.*;
import static utils.DateFormats.*;
import static utils.Messages.*;

public class RicoveriPanel extends JPanel {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);

    private final transient Controller controller;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField praticaField  = new JTextField(15);
    private final JComboBox<Paziente> pazienteCombo = new JComboBox<>();
    private final JComboBox<Reparto>  repartoCombo  = new JComboBox<>();
    private final JComboBox<Letto>    lettoCombo    = new JComboBox<>();
    private final JTextField inizioField = new JTextField(DATE_TIME_FORMAT_PATTERN, 22);
    private final JTextField fineField   = new JTextField(DATE_TIME_FORMAT_PATTERN + " (facoltativo)", 22);
    private final JTextField motivoField = new JTextField(20);

    public RicoveriPanel(Controller controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;

        String[] cols = {"N. Pratica", "Letto", "Inizio", "Dimissione", "In corso"};
        this.tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        this.table = new JTable(tableModel);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        refreshRicoveri(tableModel);

        controller.getPazienti().forEach(pazienteCombo::addItem);
        controller.getReparti().forEach(repartoCombo::addItem);

        repartoCombo.addActionListener(e -> aggiornaLetti());
        if (repartoCombo.getItemCount() > 0) repartoCombo.setSelectedIndex(0);

        JButton addBtn = new JButton("Registra Ricovero");
        JButton dimettBtn = new JButton("Registra Dimissione");
        addBtn.addActionListener(e -> handleAggiungiRicovero());
        dimettBtn.addActionListener(e -> handleDimissione());

        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(dimettBtn);

        add(buildFormPanel(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel buildFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registra Ricovero"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);

        GuiUtils.addFormRow(formPanel, gbc, 0, "N. Pratica:", praticaField);
        GuiUtils.addFormRow(formPanel, gbc, 1, "Paziente:",   pazienteCombo);
        GuiUtils.addFormRow(formPanel, gbc, 2, "Reparto:",    repartoCombo);
        GuiUtils.addFormRow(formPanel, gbc, 3, "Letto:",      lettoCombo);
        GuiUtils.addFormRow(formPanel, gbc, 4, "Inizio:",     inizioField);
        GuiUtils.addFormRow(formPanel, gbc, 5, "Fine:",       fineField);
        GuiUtils.addFormRow(formPanel, gbc, 6, "Motivo:",     motivoField);
        return formPanel;
    }

    private void aggiornaLetti() {
        lettoCombo.removeAllItems();
        Reparto r = (Reparto) repartoCombo.getSelectedItem();
        if (r != null) controller.getLettiPerReparto(r.getIdReparto()).forEach(lettoCombo::addItem);
    }

    private void handleAggiungiRicovero() {
        try {
            Paziente paz  = (Paziente) pazienteCombo.getSelectedItem();
            Letto    letto = (Letto) lettoCombo.getSelectedItem();
            if (paz == null || letto == null) {
                showError(this, "Selezionare paziente e letto.");
                return;
            }
            LocalDateTime inizio = LocalDateTime.parse(inizioField.getText().trim(), DATETIME_FMT);
            LocalDateTime fine   = parseFineOptional(fineField.getText().trim());

            controller.registraRicovero(praticaField.getText().trim(), paz.getIdPaziente(),
                    letto.getCodiceUnivoco(), inizio, fine, motivoField.getText().trim());
            refreshRicoveri(tableModel);
            clearFields(praticaField, inizioField, fineField, motivoField);
            showInfo(this, "Ricovero registrato.");
        } catch (DateTimeParseException ex) {
            showError(this, DATE_FORMAT_MSG_USE + DATE_TIME_FORMAT_PATTERN);
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void handleDimissione() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError(this, "Selezionare un ricovero dalla tabella.");
            return;
        }
        if ("No".equals(tableModel.getValueAt(row, 4))) {
            showError(this, "Ricovero già concluso.");
            return;
        }
        String numeroPratica = (String) tableModel.getValueAt(row, 0);
        String dataStr = JOptionPane.showInputDialog(this,
                "Data e ora dimissione " + DATE_TIME_FORMAT_PATTERN,
                "Registra Dimissione", JOptionPane.QUESTION_MESSAGE);
        if (dataStr == null) return;
        eseguiDimissione(numeroPratica, dataStr.trim());
    }

    private void eseguiDimissione(String numeroPratica, String dataStr) {
        try {
            LocalDateTime dataDimissione = LocalDateTime.parse(dataStr, DATETIME_FMT);
            controller.registraDimissione(numeroPratica, dataDimissione);
            refreshRicoveri(tableModel);
            showInfo(this, "Dimissione registrata.");
        } catch (DateTimeParseException ex) {
            showError(this, DATE_FORMAT_MSG);
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private LocalDateTime parseFineOptional(String fineStr) {
        if (fineStr.isEmpty() || fineStr.startsWith("dd/MM")) return null;
        return LocalDateTime.parse(fineStr, DATETIME_FMT);
    }

    private void refreshRicoveri(DefaultTableModel model) {
        model.setRowCount(0);
        for (Ricovero r : controller.getRicoveri()) {
            model.addRow(new Object[]{
                    r.getNumeroPratica(),
                    r.getCodiceUnivocoLetto(),
                    r.getDataInizioRicovero().format(DATETIME_FMT),
                    r.getDataDimissioneRicovero() != null ? r.getDataDimissioneRicovero().format(DATETIME_FMT) : "–",
                    r.isInCorso() ? "Sì" : "No"
            });
        }
    }
}
