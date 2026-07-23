package gui.panels.medico;

import controller.Controller;
import exceptions.DAOException;
import gui.components.DatePickerField;
import model.Medico;
import model.Prestazione;
import model.Turno;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static utils.DateFormats.*;
import static utils.TimeZones.EUROPE_ROME;

public class AgendaGiornalieraPanel extends JPanel {

    private final transient Controller controller;
    private final transient Medico medicoCorrente;
    private final DefaultTableModel tableModel;
    private final DatePickerField dataField;
    private final JLabel turniLabel = new JLabel();

    public AgendaGiornalieraPanel(Controller controller, Medico medicoCorrente) {
        super(new BorderLayout(5, 5));
        this.controller      = controller;
        this.medicoCorrente  = medicoCorrente;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Ora Inizio Prestazione", "Ora Fine Prestazione", "Tipo Prestazione", "N. Pratica", "Esito"};
        this.tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        this.dataField = new DatePickerField(LocalDate.now(ZoneId.of(EUROPE_ROME)));
        buildUI();
        aggiorna();
    }

    private void buildUI() {
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);

        JButton oggiBtn  = new JButton("Oggi");
        JButton cercaBtn = new JButton("Visualizza");

        oggiBtn.addActionListener(e -> {
            dataField.setLocalDate(LocalDate.now(ZoneId.of(EUROPE_ROME)));
            aggiorna();
        });
        cercaBtn.addActionListener(e -> aggiorna());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Data:"));
        top.add(dataField);
        top.add(oggiBtn);
        top.add(cercaBtn);
        top.add(Box.createHorizontalStrut(20));
        top.add(turniLabel);

        add(top,                    BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void aggiorna() {
        try {
            LocalDate data = dataField.getLocalDate();
            if (data == null) {
                JOptionPane.showMessageDialog(this, "Selezionare una data.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }
            popolaPrestazioni(data);
            aggiornaTurniLabel(data);
        } catch (DAOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void popolaPrestazioni(LocalDate data) {
        List<Prestazione> lista = controller.getPrestazioniPerGiorno(medicoCorrente.getIdMedico(), data);
        tableModel.setRowCount(0);
        for (Prestazione p : lista) {
            tableModel.addRow(new Object[]{
                p.getDataInizioPrestazione().format(TIME_FMT),
                p.getDataFinePrestazione().format(TIME_FMT),
                p.getTipologiaPrestazione(),
                p.getNumeroPratica(),
                formatEsitoBreve(p.getEsito())
            });
        }
    }

    private void aggiornaTurniLabel(LocalDate data) {
        List<Turno> turniOggi = controller.getAgendaMedico(medicoCorrente.getIdMedico(), data, data);
        if (turniOggi.isEmpty()) {
            turniLabel.setText("<html><i>Nessun turno in questa data</i></html>");
            return;
        }
        StringBuilder sb = new StringBuilder("<html><b>Turni:</b> ");
        for (Turno t : turniOggi) {
            sb.append(t.getFasciaOraria())
              .append(" ").append(t.getOraInizio().format(TIME_FMT))
              .append("–").append(t.getOraFine().format(TIME_FMT))
              .append("  ");
        }
        sb.append("</html>");
        turniLabel.setText(sb.toString());
    }

    private static String formatEsitoBreve(String esito) {
        if (esito == null || esito.isEmpty()) return "–";
        if (esito.length() > 40) return esito.substring(0, 40) + "…";
        return esito;
    }
}
