package gui.panels.admin;

import controller.Controller;
import gui.components.DatePickerField;
import model.PazienteInDimissione;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static gui.utils.GuiUtils.showError;
import static gui.utils.GuiUtils.showInfo;
import static utils.DateFormats.*;
import static utils.TimeZones.EUROPE_ROME;

public class DimissioniPanel extends JPanel implements RefreshablePanel {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);

    private final transient Controller controller;
    private final DefaultTableModel tableModel;
    private final DatePickerField dataField;

    public DimissioniPanel(Controller controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        this.tableModel = new DefaultTableModel(
                new String[]{"Paziente", "CF", "Letto", "Dimissione Prevista"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        this.dataField = new DatePickerField(LocalDate.now(ZoneId.of(EUROPE_ROME)));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        JTable table = new JTable(tableModel);

        JButton oggiBtn  = new JButton("Oggi");
        JButton cercaBtn = new JButton("Cerca");
        oggiBtn.addActionListener(e -> {
            dataField.setLocalDate(LocalDate.now(ZoneId.of(EUROPE_ROME)));
            cerca(true);
        });
        cercaBtn.addActionListener(e -> cerca(true));
        cerca(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Data dimissione"));
        top.add(dataField);
        top.add(oggiBtn);
        top.add(cercaBtn);

        add(top,                    BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void cerca(boolean avvisaSeVuoto) {
        try {
            LocalDate data = dataField.getLocalDate();
            if (data == null) {
                showError(this, "Selezionare una data.");
                return;
            }
            List<PazienteInDimissione> risultati = controller.getPazientiInDimissione(data);
            tableModel.setRowCount(0);
            for (PazienteInDimissione pid : risultati) {
                tableModel.addRow(new Object[]{
                    pid.nome() + " " + pid.cognome(),
                    pid.codiceFiscale(),
                    pid.codiceUnivocoLetto(),
                    pid.dataDimissione().format(DATETIME_FMT)
                });
            }
            if (avvisaSeVuoto && risultati.isEmpty()) {
                showInfo(this, "Nessuna dimissione prevista per "
                        + data.format(DATE_FMT));
            }
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    @Override
    public void refresh() {
        cerca(false);
    }
}
