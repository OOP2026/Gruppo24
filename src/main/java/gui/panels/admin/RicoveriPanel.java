package gui.panels.admin;

import controller.Controller;
import gui.components.DateTimePickerField;
import gui.utils.GuiUtils;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

import static gui.utils.GuiUtils.*;
import static utils.DateFormats.*;
import static utils.Messages.*;

public class RicoveriPanel extends JPanel implements RefreshablePanel {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);
    private static final int COL_PAZIENTE = 0;
    private static final int COL_NUM_PRATICA = 1;
    private static final int COL_LETTO = 2;
    private static final int COL_INIZIO = 3;
    private static final int COL_DIMISSIONE = 4;
    private static final int COL_IN_CORSO = 5;
    private static final int PAZIENTE_MIN_WIDTH = 260;

    private final transient Controller controller;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField praticaField  = new JTextField(15);
    private final JComboBox<Paziente> pazienteCombo = new JComboBox<>();
    private final JComboBox<Reparto>  repartoCombo  = new JComboBox<>();
    private final JComboBox<Letto>    lettoCombo    = new JComboBox<>();
    private final DateTimePickerField inizioField = new DateTimePickerField();
    private final DateTimePickerField fineField   = new DateTimePickerField();
    private final JTextField motivoField = new JTextField(20);

    public RicoveriPanel(Controller controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;

        String[] cols = {"Paziente" , "N. Pratica", "Letto", "Inizio", "Dimissione", "In corso"};
        this.tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        this.table = new JTable(tableModel);
        configureColumnWidths();

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
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustPazienteColumnWidth();
            }
        });
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        SwingUtilities.invokeLater(this::adjustPazienteColumnWidth);
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
            LocalDateTime inizio = inizioField.getLocalDateTime();
            if (inizio == null) {
                showError(this, "Selezionare la data e ora di inizio.");
                return;
            }
            LocalDateTime fine = fineField.isEmpty() ? null : fineField.getLocalDateTime();

            controller.registraRicovero(praticaField.getText().trim(), paz.getIdPaziente(),
                    letto.getCodiceUnivoco(), inizio, fine, motivoField.getText().trim());
            refreshRicoveri(tableModel);
            clearFields(praticaField, motivoField);
            clearDateTimePickers(inizioField, fineField);
            showInfo(this, "Ricovero registrato.");
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
        if ("No".equals(tableModel.getValueAt(row, COL_IN_CORSO))) {
            showError(this, "Ricovero già concluso.");
            return;
        }
        String numeroPratica = (String) tableModel.getValueAt(row, COL_NUM_PRATICA);
        DateTimePickerField dimissionePicker = new DateTimePickerField(LocalDateTime.now());
        int res = JOptionPane.showConfirmDialog(this, dimissionePicker,
                "Data e ora dimissione", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        LocalDateTime dataDimissione = dimissionePicker.getLocalDateTime();
        if (dataDimissione == null) {
            showError(this, "Selezionare la data e ora di dimissione.");
            return;
        }
        eseguiDimissione(numeroPratica, dataDimissione);
    }

    private void eseguiDimissione(String numeroPratica, LocalDateTime dataDimissione) {
        try {
            controller.registraDimissione(numeroPratica, dataDimissione);
            refreshRicoveri(tableModel);
            showInfo(this, "Dimissione registrata.");
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    public void refresh() {
        Paziente selezionato = (Paziente) pazienteCombo.getSelectedItem();
        pazienteCombo.removeAllItems();
        controller.getPazienti().forEach(pazienteCombo::addItem);
        if (selezionato != null) pazienteCombo.setSelectedItem(selezionato);
        refreshRicoveri(tableModel);
    }

    private void configureColumnWidths() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setFixedColumnWidth(COL_NUM_PRATICA, 120);
        setFixedColumnWidth(COL_LETTO, 70);
        setFixedColumnWidth(COL_INIZIO, 155);
        setFixedColumnWidth(COL_DIMISSIONE, 155);

        TableColumn pazienteCol = table.getColumnModel().getColumn(COL_PAZIENTE);
        pazienteCol.setPreferredWidth(300);
        pazienteCol.setMinWidth(PAZIENTE_MIN_WIDTH);

        TableColumn inCorsoCol = table.getColumnModel().getColumn(COL_IN_CORSO);
        inCorsoCol.setPreferredWidth(55);
        inCorsoCol.setMinWidth(50);
        inCorsoCol.setMaxWidth(65);
    }

    private void setFixedColumnWidth(int columnIndex, int width) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        column.setPreferredWidth(width);
        column.setMinWidth(width);
        column.setMaxWidth(width);
    }

    private void adjustPazienteColumnWidth() {
        if (table.getParent() == null) {
            return;
        }
        int viewportWidth = table.getParent().getWidth();
        if (viewportWidth <= 0) {
            return;
        }

        int fixedWidth = 0;
        for (int col = COL_NUM_PRATICA; col <= COL_IN_CORSO; col++) {
            fixedWidth += table.getColumnModel().getColumn(col).getWidth();
        }

        int pazienteWidth = Math.max(PAZIENTE_MIN_WIDTH, viewportWidth - fixedWidth);
        TableColumn pazienteCol = table.getColumnModel().getColumn(COL_PAZIENTE);
        pazienteCol.setPreferredWidth(pazienteWidth);
        pazienteCol.setWidth(pazienteWidth);
    }

    private void refreshRicoveri(DefaultTableModel model) {
        model.setRowCount(0);
        Map<Integer, Paziente> pazientiById = controller.getPazienti().stream()
                .collect(Collectors.toMap(Paziente::getIdPaziente, p -> p));
        for (Ricovero r : controller.getRicoveri()) {
            Paziente paziente = pazientiById.get(r.getIdPaziente());
            String pazienteLabel = paziente != null ? paziente.toString() : "–";
            model.addRow(new Object[]{
                    pazienteLabel,
                    r.getNumeroPratica(),
                    r.getCodiceUnivocoLetto(),
                    r.getDataInizioRicovero().format(DATETIME_FMT),
                    r.getDataDimissioneRicovero() != null ? r.getDataDimissioneRicovero().format(DATETIME_FMT) : "–",
                    r.isInCorso() ? "Sì" : "No"
            });
        }
    }
}
