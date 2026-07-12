package gui.panels.admin;

import controller.Controller;
import gui.utils.GuiUtils;
import model.Paziente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static gui.utils.GuiUtils.*;
import static utils.DateFormats.DATE_FORMAT_PATTERN;

public class PazientiPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);

    private final transient Controller controller;

    public PazientiPanel(Controller controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        String[] cols = {"CF", "Nome", "Cognome", "Data Nascita", "ID"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.removeColumn(table.getColumnModel().getColumn(4));
        refreshPazienti(tableModel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Aggiungi / Modifica Paziente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField cfField      = new JTextField(15);
        JTextField nomeField    = new JTextField(12);
        JTextField cognomeField = new JTextField(12);
        JTextField nascitaField = new JTextField(10);
        nascitaField.setToolTipText("gg/mm/aaaa");

        GuiUtils.addFormRow(formPanel, gbc, 0, "CF:",           cfField);
        GuiUtils.addFormRow(formPanel, gbc, 1, "Nome:",         nomeField);
        GuiUtils.addFormRow(formPanel, gbc, 2, "Cognome:",      cognomeField);
        GuiUtils.addFormRow(formPanel, gbc, 3, "Data nascita:", nascitaField);

        JButton addBtn  = new JButton("Aggiungi");
        JButton editBtn = new JButton("Modifica selezionato");
        JPanel  btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            cfField.setText((String) tableModel.getValueAt(row, 0));
            nomeField.setText((String) tableModel.getValueAt(row, 1));
            cognomeField.setText((String) tableModel.getValueAt(row, 2));
            nascitaField.setText((String) tableModel.getValueAt(row, 3));
        });

        addBtn.addActionListener(e -> {
            try {
                LocalDate nascita = LocalDate.parse(nascitaField.getText().trim(), DATE_FMT);
                controller.aggiungiPaziente(cfField.getText().trim(),
                        nomeField.getText().trim(), cognomeField.getText().trim(), nascita);
                refreshPazienti(tableModel);
                clearFields(cfField, nomeField, cognomeField, nascitaField);
                showInfo(this, "Paziente aggiunto con successo.");
            } catch (Exception ex) {
                showError(this, ex.getMessage());
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showError(this, "Selezionare un paziente dalla tabella."); return; }
            int idPaziente = (int) tableModel.getValueAt(row, 4);
            try {
                LocalDate nascita = LocalDate.parse(nascitaField.getText().trim(), DATE_FMT);
                controller.modificaPaziente(idPaziente, nomeField.getText().trim(),
                        cognomeField.getText().trim(), nascita);
                refreshPazienti(tableModel);
                showInfo(this, "Paziente modificato.");
            } catch (Exception ex) {
                showError(this, ex.getMessage());
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);
    }

    private void refreshPazienti(DefaultTableModel model) {
        model.setRowCount(0);
        for (Paziente p : controller.getPazienti()) {
            model.addRow(new Object[]{
                p.getCodiceFiscale(), p.getNome(), p.getCognome(),
                p.getDataNascita().format(DATE_FMT),
                p.getIdPaziente()
            });
        }
    }
}
