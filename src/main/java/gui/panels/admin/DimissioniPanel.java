package gui.panels.admin;

import controller.Controller;
import model.PazienteInDimissione;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static gui.utils.GuiUtils.showError;
import static gui.utils.GuiUtils.showInfo;
import static utils.DateFormats.*;
import static utils.Messages.DATE_FORMAT_MSG;
import static utils.TimeZones.EUROPE_ROME;

public class DimissioniPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);

    private final transient Controller controller;

    public DimissioniPanel(Controller controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        String[] cols = {"Paziente", "CF", "Letto", "Dimissione Prevista"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);

        JTextField dataField = new JTextField(LocalDate.now(ZoneId.of(EUROPE_ROME)).format(DATE_FMT), 10);
        JButton    oggiBtn   = new JButton("Oggi");
        JButton    cercaBtn  = new JButton("Cerca");

        Runnable cerca = () -> {
            try {
                LocalDate data = LocalDate.parse(dataField.getText().trim(), DATE_FMT);
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
                if (risultati.isEmpty())
                    showInfo(this, "Nessuna dimissione prevista per " + dataField.getText().trim());
            } catch (DateTimeParseException ex) {
                showError(this, DATE_FORMAT_MSG + " " + DATE_FORMAT_PATTERN);
            } catch (Exception ex) {
                showError(this, ex.getMessage());
            }
        };

        oggiBtn.addActionListener(e -> { dataField.setText(LocalDate.now(ZoneId.of(EUROPE_ROME)).format(DATE_FMT)); cerca.run(); });
        cercaBtn.addActionListener(e -> cerca.run());
        cerca.run();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Data dimissione " + DATE_FORMAT_PATTERN));
        top.add(dataField);
        top.add(oggiBtn);
        top.add(cercaBtn);

        add(top,                    BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
