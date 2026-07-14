package gui.panels.medico;

import controller.Controller;
import model.Medico;
import model.Turno;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static utils.DateFormats.*;
import static utils.TimeZones.EUROPE_ROME;

public class AgendaSettimanalePanel extends JPanel {

    private final transient Controller controller;
    private final transient Medico medicoCorrente;
    private final DefaultTableModel tableModel;
    private final JLabel settLabel;
    private final LocalDate[] settimana;

    public AgendaSettimanalePanel(Controller controller, Medico medicoCorrente) {
        super(new BorderLayout(5, 5));
        this.controller     = controller;
        this.medicoCorrente = medicoCorrente;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        LocalDate oggi   = LocalDate.now(ZoneId.of(EUROPE_ROME));
        LocalDate lunedi = oggi.minusDays((long) oggi.getDayOfWeek().getValue() - 1);
        this.settimana = new LocalDate[]{lunedi};
        this.settLabel = new JLabel(formatSettimana(lunedi));

        String[] giorni = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
        this.tableModel = new DefaultTableModel(giorni, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        buildUI();
        aggiorna();
    }

    private void buildUI() {
        JTable table = new JTable(tableModel);
        table.setRowHeight(70);

        JButton prevBtn = new JButton("◀ Prec");
        JButton nextBtn = new JButton("Succ ▶");

        prevBtn.addActionListener(e -> { settimana[0] = settimana[0].minusWeeks(1); aggiorna(); });
        nextBtn.addActionListener(e -> { settimana[0] = settimana[0].plusWeeks(1);  aggiorna(); });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        top.add(prevBtn);
        top.add(settLabel);
        top.add(nextBtn);

        add(top,                    BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void aggiorna() {
        LocalDate inizio = settimana[0];
        LocalDate fine   = inizio.plusDays(6);
        List<Turno> turni = controller.getAgendaMedico(medicoCorrente.getIdMedico(), inizio, fine);
        tableModel.setRowCount(0);

        Map<LocalDate, StringBuilder> perGiorno = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) perGiorno.put(inizio.plusDays(i), new StringBuilder());
        for (Turno t : turni) {
            StringBuilder sb = perGiorno.getOrDefault(t.getData(), new StringBuilder());
            if (!sb.isEmpty()) sb.append("<br>");
            sb.append("<b>").append(t.getFasciaOraria()).append("</b> ")
              .append(t.getOraInizio().format(TIME_FMT))
              .append("–").append(t.getOraFine().format(TIME_FMT));
        }

        Object[] row = new Object[7];
        int idx = 0;
        for (Map.Entry<LocalDate, StringBuilder> d : perGiorno.entrySet()) {
            String content = d.getValue().toString();
            row[idx++] = content.isEmpty()
                    ? "<html><font color='gray'><i>–</i></font></html>"
                    : "<html>" + content + "</html>";
        }
        tableModel.addRow(row);
        settLabel.setText(formatSettimana(settimana[0]));
    }

    private String formatSettimana(LocalDate lunedi) {
        return "Settimana: " + lunedi.format(DATE_FMT) + " – " + lunedi.plusDays(6).format(DATE_FMT);
    }
}
