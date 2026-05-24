package gui.panels;

import controller.Controller;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Pannello principale per il ruolo Medico.
 * Contiene un JTabbedPane con 4 tab:
 *   1. Agenda Giornaliera  – prestazioni di un dato giorno
 *   2. Agenda Settimanale  – vista a 7 giorni
 *   3. Nuova Prestazione   – form per registrare una prestazione
 *   4. Modifica Esiti      – compilare/modificare l'esito delle prestazioni
 */
public class MedicoPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FMT     = DateTimeFormatter.ofPattern("HH:mm");

    private final Controller controller;
    private final Medico     medicoCorrente;

    public MedicoPanel(Controller controller) {
        this.controller     = controller;
        this.medicoCorrente = controller.getMedicoCorrente();
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.addTab("📆 Agenda Giornaliera",  buildAgendaGiornalieraPanel());
        tabs.addTab("🗓 Agenda Settimanale",  buildAgendaSettimanalePanel());
        tabs.addTab("➕ Nuova Prestazione",   buildNuovaPrestazionePanel());
        tabs.addTab("✏ Modifica Esiti",       buildModificaEsitiPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ════════════════════════════════════════════════════════════════════════
    // TAB 1 – AGENDA GIORNALIERA
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildAgendaGiornalieraPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Ora Inizio", "Ora Fine", "Tipo", "Paziente (CF)", "Esito"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);

        JTextField dataField = new JTextField(LocalDate.now().format(DATE_FMT), 10);
        JButton    oggiBtn   = new JButton("Oggi");
        JButton    cercaBtn  = new JButton("Visualizza");

        Runnable aggiorna = () -> {
            try {
                LocalDate data = LocalDate.parse(dataField.getText().trim(), DATE_FMT);
                List<Prestazione> lista = controller.getAgendaGiornaliera(medicoCorrente, data);
                model.setRowCount(0);
                for (Prestazione p : lista) {
                    String esitoTrunc = p.getEsito().isEmpty() ? "–" :
                            (p.getEsito().length() > 40 ? p.getEsito().substring(0,40)+"…" : p.getEsito());
                    model.addRow(new Object[]{
                        p.getInizio().format(TIME_FMT),
                        p.getFine().format(TIME_FMT),
                        p.getTipo(),
                        p.getRicovero().getPaziente().getCodiceFiscale(),
                        esitoTrunc
                    });
                }
                if (lista.isEmpty()) {
                    JLabel vuoto = new JLabel("Nessuna prestazione per " + dataField.getText().trim(),
                            SwingConstants.CENTER);
                    vuoto.setForeground(Color.GRAY);
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(panel, "Formato data: dd/MM/yyyy", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        };

        oggiBtn.addActionListener(e -> { dataField.setText(LocalDate.now().format(DATE_FMT)); aggiorna.run(); });
        cercaBtn.addActionListener(e -> aggiorna.run());
        aggiorna.run();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Data (dd/MM/yyyy):"));
        top.add(dataField);
        top.add(oggiBtn);
        top.add(cercaBtn);

        // Turni del giorno come reminder
        JLabel turniLabel = buildTurniLabel(LocalDate.now().getDayOfWeek());
        dataField.addActionListener(e -> {
            try {
                DayOfWeek giorno = LocalDate.parse(dataField.getText().trim(), DATE_FMT).getDayOfWeek();
                // aggiorna label dinamicamente sarebbe complesso, mostra turni fissi
            } catch (Exception ignored) {}
        });
        top.add(Box.createHorizontalStrut(20));
        top.add(turniLabel);

        panel.add(top,                    BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JLabel buildTurniLabel(DayOfWeek giorno) {
        StringBuilder sb = new StringBuilder("<html><b>I miei turni oggi:</b> ");
        medicoCorrente.getTurniProgrammati().stream()
                .filter(t -> t.getGiornoDellaSettimana() == giorno)
                .forEach(t -> sb.append(t.getOraInizio()).append("–").append(t.getOraFine()).append("  "));
        boolean haTurni = medicoCorrente.getTurniProgrammati().stream()
                .anyMatch(t -> t.getGiornoDellaSettimana() == giorno);
        if (!haTurni) sb.append("<i>nessun turno</i>");
        sb.append("</html>");
        return new JLabel(sb.toString());
    }

    // ════════════════════════════════════════════════════════════════════════
    // TAB 2 – AGENDA SETTIMANALE
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildAgendaSettimanalePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Inizio settimana = lunedì della settimana corrente
        LocalDate oggi    = LocalDate.now();
        LocalDate lunedi  = oggi.minusDays(oggi.getDayOfWeek().getValue() - 1);

        JLabel   settLabel  = new JLabel(formatSettimana(lunedi));
        JButton  prevBtn    = new JButton("◀ Prev");
        JButton  nextBtn    = new JButton("Succ ▶");
        final LocalDate[]  settimana  = {lunedi};

        // Tabella: colonne = 7 giorni, righe = prestazioni (max per slot)
        String[] giorni = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
        DefaultTableModel tableModel = new DefaultTableModel(giorni, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(55);

        Runnable aggiornaSettimana = () -> {
            Map<LocalDate, List<Prestazione>> agenda =
                    controller.getAgendaSettimanale(medicoCorrente, settimana[0]);
            tableModel.setRowCount(0);

            // Trova max prestazioni in un giorno
            int maxRighe = agenda.values().stream()
                    .mapToInt(List::size).max().orElse(0);
            if (maxRighe == 0) maxRighe = 1;

            for (int i = 0; i < maxRighe; i++) {
                Object[] row = new Object[7];
                int idx = 0;
                for (LocalDate d = settimana[0]; !d.isAfter(settimana[0].plusDays(6)); d = d.plusDays(1)) {
                    List<Prestazione> list = agenda.get(d);
                    if (list != null && i < list.size()) {
                        Prestazione p = list.get(i);
                        row[idx] = "<html>" + p.getInizio().format(TIME_FMT) + "–"
                                + p.getFine().format(TIME_FMT) + "<br>"
                                + "<b>" + p.getTipo() + "</b><br>"
                                + p.getRicovero().getPaziente().getCodiceFiscale()
                                + "</html>";
                    } else {
                        row[idx] = "";
                    }
                    idx++;
                }
                tableModel.addRow(row);
            }

            settLabel.setText(formatSettimana(settimana[0]));
        };

        prevBtn.addActionListener(e -> { settimana[0] = settimana[0].minusWeeks(1); aggiornaSettimana.run(); });
        nextBtn.addActionListener(e -> { settimana[0] = settimana[0].plusWeeks(1);  aggiornaSettimana.run(); });
        aggiornaSettimana.run();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        top.add(prevBtn);
        top.add(settLabel);
        top.add(nextBtn);

        panel.add(top,                    BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private String formatSettimana(LocalDate lunedi) {
        return "Settimana: " + lunedi.format(DATE_FMT) + " – " + lunedi.plusDays(6).format(DATE_FMT);
    }

    // ════════════════════════════════════════════════════════════════════════
    // TAB 3 – NUOVA PRESTAZIONE
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildNuovaPrestazionePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registra Nuova Prestazione"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        // Combo ricoveri in corso
        JComboBox<Ricovero> ricoveroCombo = buildRicoveroCombo();
        JTextField inizioField  = new JTextField("dd/MM/yyyy HH:mm", 16);
        JTextField fineField    = new JTextField("dd/MM/yyyy HH:mm", 16);
        JComboBox<TipoPrestazione> tipoCombo = new JComboBox<>(TipoPrestazione.values());

        addRow(formPanel, gbc, 0, "Ricovero (paziente):", ricoveroCombo);
        addRow(formPanel, gbc, 1, "Inizio:",              inizioField);
        addRow(formPanel, gbc, 2, "Fine:",                fineField);
        addRow(formPanel, gbc, 3, "Tipo:",                tipoCombo);

        // Mostra turni del medico come riferimento
        JTextArea turniInfo = new JTextArea(4, 30);
        turniInfo.setEditable(false);
        turniInfo.setFont(new Font("Monospaced", Font.PLAIN, 11));
        turniInfo.setBackground(new Color(245, 245, 245));
        turniInfo.setBorder(BorderFactory.createTitledBorder("I miei turni"));
        StringBuilder sb = new StringBuilder();
        for (Turno t : medicoCorrente.getTurniProgrammati()) {
            sb.append(t.getGiornoDellaSettimana())
              .append(":  ").append(t.getOraInizio())
              .append("–").append(t.getOraFine()).append("\n");
        }
        turniInfo.setText(sb.length() > 0 ? sb.toString() : "Nessun turno programmato.");

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(turniInfo, gbc);

        JButton registraBtn = new JButton("Registra Prestazione");
        registraBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridy = 5;
        formPanel.add(registraBtn, gbc);

        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        gbc.gridy = 6;
        formPanel.add(statusLabel, gbc);

        registraBtn.addActionListener(e -> {
            try {
                Ricovero ricovero = (Ricovero) ricoveroCombo.getSelectedItem();
                if (ricovero == null) { statusLabel.setText("⚠ Selezionare un ricovero."); return; }
                LocalDateTime inizio = LocalDateTime.parse(inizioField.getText().trim(), DATETIME_FMT);
                LocalDateTime fine   = LocalDateTime.parse(fineField.getText().trim(),   DATETIME_FMT);
                TipoPrestazione tipo = (TipoPrestazione) tipoCombo.getSelectedItem();

                controller.registraPrestazione(medicoCorrente, ricovero, inizio, fine, tipo);
                statusLabel.setForeground(new Color(39, 174, 96));
                statusLabel.setText("✓ Prestazione registrata con successo.");
                inizioField.setText("");
                fineField.setText("");
            } catch (DateTimeParseException ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("⚠ Formato data non valido (dd/MM/yyyy HH:mm).");
            } catch (Exception ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("⚠ " + ex.getMessage());
            }
        });

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    // TAB 4 – MODIFICA ESITI
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildModificaEsitiPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Data", "Ora", "Tipo", "Paziente (CF)", "Esito attuale"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(22);

        Runnable refreshEsiti = () -> {
            tableModel.setRowCount(0);
            for (Prestazione p : medicoCorrente.getPrestazioniErogate()) {
                tableModel.addRow(new Object[]{
                    p.getInizio().toLocalDate().format(DATE_FMT),
                    p.getInizio().format(TIME_FMT) + "–" + p.getFine().format(TIME_FMT),
                    p.getTipo(),
                    p.getRicovero().getPaziente().getCodiceFiscale(),
                    p.getEsito().isEmpty() ? "(non compilato)" : p.getEsito()
                });
            }
        };
        refreshEsiti.run();

        // ── Editor esito ─────────────────────────────────────────────────────
        JPanel editPanel = new JPanel(new BorderLayout(5, 5));
        editPanel.setBorder(BorderFactory.createTitledBorder("Compila / Modifica Esito"));

        JTextArea esitoArea = new JTextArea(4, 40);
        esitoArea.setLineWrap(true);
        esitoArea.setWrapStyleWord(true);
        JButton salvaBtn = new JButton("Salva Esito");
        salvaBtn.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Selezione riga → carica esito corrente
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= medicoCorrente.getPrestazioniErogate().size()) return;
            Prestazione p = medicoCorrente.getPrestazioniErogate().get(row);
            esitoArea.setText(p.getEsito());
        });

        salvaBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(panel, "Selezionare una prestazione.", "Avviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String nuovoEsito = esitoArea.getText().trim();
            if (nuovoEsito.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "L'esito non può essere vuoto.", "Avviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Prestazione p = medicoCorrente.getPrestazioniErogate().get(row);
            controller.aggiornaEsito(p, nuovoEsito);
            refreshEsiti.run();
            JOptionPane.showMessageDialog(panel, "Esito salvato.", "OK", JOptionPane.INFORMATION_MESSAGE);
        });

        editPanel.add(new JScrollPane(esitoArea), BorderLayout.CENTER);
        editPanel.add(salvaBtn,                   BorderLayout.SOUTH);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(editPanel,              BorderLayout.SOUTH);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    // UTILITY
    // ════════════════════════════════════════════════════════════════════════

    private JComboBox<Ricovero> buildRicoveroCombo() {
        JComboBox<Ricovero> cb = new JComboBox<>();
        controller.getRicoveriInCorso().forEach(cb::addItem);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object v,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, v, idx, sel, focus);
                if (v instanceof Ricovero r) {
                    Paziente p = r.getPaziente();
                    setText(p.getNome() + " " + p.getCognome() + " (" + p.getCodiceFiscale() + ")"
                            + " – " + r.getLetto().getCodiceUnivoco());
                }
                return this;
            }
        });
        return cb;
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }
}
