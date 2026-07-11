package gui.panels;

import controller.Controller;
import dao.DAOException;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static javax.swing.SwingConstants.LEFT;
import static utils.DateFormats.*;

public class MedicoPanel extends JPanel {

    private final transient  Controller controller;
    private final transient  Medico medicoCorrente;

    public MedicoPanel(Controller controller) {
        this.controller     = controller;
        this.medicoCorrente = controller.getMedicoCorrente();
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane(LEFT);
        tabs.addTab("Agenda Giornaliera", buildAgendaGiornalieraPanel());
        tabs.addTab("Agenda Settimanale", buildAgendaSettimanalePanel());
        tabs.addTab("Nuova Prestazione", buildNuovaPrestazionePanel());
        tabs.addTab("Modifica Esiti", buildModificaEsitiPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // Agenda giornaliera

    private JPanel buildAgendaGiornalieraPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Ora Inizio", "Ora Fine", "Tipo", "N. Pratica", "Esito"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);

        JTextField dataField  = new JTextField(LocalDate.now().format(DATE_FMT), 10);
        JButton oggiBtn = new JButton("Oggi");
        JButton cercaBtn = new JButton("Visualizza");
        JLabel turniLabel = new JLabel();

        Runnable aggiorna = () -> aggiornaAgenda(panel, tableModel, dataField, turniLabel);

        oggiBtn.addActionListener(e -> {
            dataField.setText(LocalDate.now().format(DATE_FMT));
            aggiorna.run();
        });
        cercaBtn.addActionListener(e -> aggiorna.run());
        aggiorna.run();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Data (dd/MM/yyyy):"));
        top.add(dataField);
        top.add(oggiBtn);
        top.add(cercaBtn);
        top.add(Box.createHorizontalStrut(20));
        top.add(turniLabel);

        panel.add(top,                    BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void aggiornaAgenda(JPanel panel, DefaultTableModel tableModel,
                                JTextField dataField, JLabel turniLabel) {
        try {
            LocalDate data = LocalDate.parse(dataField.getText().trim(), DATE_FMT);
            popolaPrestazioni(tableModel, data);
            aggiornaTurniLabel(turniLabel, data);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(panel, "Formato data: dd/MM/yyyy",
                    "Errore", JOptionPane.ERROR_MESSAGE);
        } catch (DAOException ex) {
            JOptionPane.showMessageDialog(panel, ex.getMessage(),
                    "Errore DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void popolaPrestazioni(DefaultTableModel tableModel, LocalDate data) {
        List<Prestazione> lista = controller.getPrestazioniPerGiorno(
                medicoCorrente.getIdMedico(), data);
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

    private String formatEsitoBreve(String esito) {
        if (esito == null || esito.isEmpty()) return "–";
        if (esito.length() > 40) return esito.substring(0, 40) + "…";
        return esito;
    }

    private void aggiornaTurniLabel(JLabel turniLabel, LocalDate data) {
        List<Turno> turniOggi = controller.getAgendaMedico(
                medicoCorrente.getIdMedico(), data, data);
        if (turniOggi.isEmpty()) {
            turniLabel.setText("<html><i>Nessun turno in questa data</i></html>");
            return;
        }
        turniLabel.setText(buildTurniHtml(turniOggi));
    }

    private String buildTurniHtml(List<Turno> turni) {
        StringBuilder sb = new StringBuilder("<html><b>Turni:</b> ");
        for (Turno t : turni) {
            sb.append(t.getFasciaOraria())
                    .append(" ").append(t.getOraInizio().format(TIME_FMT))
                    .append("–").append(t.getOraFine().format(TIME_FMT))
                    .append("  ");
        }
        sb.append("</html>");
        return sb.toString();
    }

    // Agenda settimanale

    private JPanel buildAgendaSettimanalePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        LocalDate oggi   = LocalDate.now();
        LocalDate lunedi = oggi.minusDays((long) oggi.getDayOfWeek().getValue() - 1);

        JLabel  settLabel = new JLabel(formatSettimana(lunedi));
        JButton prevBtn   = new JButton("◀ Prec");
        JButton nextBtn   = new JButton("Succ ▶");
        final LocalDate[] settimana = {lunedi};

        String[] giorni = {"Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"};
        DefaultTableModel tableModel = new DefaultTableModel(giorni, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(40);

        Runnable aggiornaSettimana = () -> {
            LocalDate inizio = settimana[0];
            LocalDate fine   = inizio.plusDays(6);
            List<Turno> turni = controller.getAgendaMedico(
                    medicoCorrente.getIdMedico(), inizio, fine);
            tableModel.setRowCount(0);

            java.util.Map<LocalDate, StringBuilder> perGiorno = new java.util.LinkedHashMap<>();
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

    // Nuova prestazione

    private JPanel buildNuovaPrestazionePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registra Nuova Prestazione"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<Ricovero>        ricoveroCombo = buildRicoveroCombo();
        JTextField                  inizioField   = new JTextField("dd/MM/yyyy HH:mm", 16);
        JTextField                  fineField     = new JTextField("dd/MM/yyyy HH:mm", 16);
        JComboBox<TipoPrestazione>  tipoCombo     = new JComboBox<>(TipoPrestazione.values());

        addFormRow(formPanel, gbc, 0, "Ricovero:",  ricoveroCombo);
        addFormRow(formPanel, gbc, 1, "Inizio:",    inizioField);
        addFormRow(formPanel, gbc, 2, "Fine:",      fineField);
        addFormRow(formPanel, gbc, 3, "Tipo:",      tipoCombo);

        JTextArea turniInfo = new JTextArea(4, 30);
        turniInfo.setEditable(false);
        turniInfo.setFont(new Font("Monospaced", Font.PLAIN, 11));
        turniInfo.setBackground(new Color(245, 245, 245));
        turniInfo.setBorder(BorderFactory.createTitledBorder("I miei turni questa settimana"));
        LocalDate oggi   = LocalDate.now();
        LocalDate lunedi = oggi.minusDays((long) oggi.getDayOfWeek().getValue() - 1);
        List<Turno> turniSettimana = controller.getAgendaMedico(
                medicoCorrente.getIdMedico(), lunedi, lunedi.plusDays(6));
        if (turniSettimana.isEmpty()) {
            turniInfo.setText("Nessun turno programmato questa settimana.");
        } else {
            StringBuilder sb = new StringBuilder();
            turniSettimana.forEach(t -> sb.append(t.getData().format(DATE_FMT))
                    .append("  ").append(t.getFasciaOraria())
                    .append("  ").append(t.getOraInizio().format(TIME_FMT))
                    .append("–").append(t.getOraFine().format(TIME_FMT)).append("\n"));
            turniInfo.setText(sb.toString());
        }
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
                if (ricovero == null) { statusLabel.setText("Selezionare un ricovero."); return; }
                LocalDateTime inizio = LocalDateTime.parse(inizioField.getText().trim(), DATETIME_FMT);
                LocalDateTime fine   = LocalDateTime.parse(fineField.getText().trim(), DATETIME_FMT);
                TipoPrestazione tipo = (TipoPrestazione) tipoCombo.getSelectedItem();
                controller.registraPrestazione(ricovero.getNumeroPratica(), inizio, fine, tipo);
                statusLabel.setForeground(new Color(39, 174, 96));
                statusLabel.setText("Prestazione registrata con successo.");
                inizioField.setText("");
                fineField.setText("");
            } catch (DateTimeParseException ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Formato data non valido (dd/MM/yyyy HH:mm).");
            } catch (Exception ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText(ex.getMessage());
            }
        });

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    // Modifica esiti

    private JPanel buildModificaEsitiPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Data", "Orario", "Tipo", "N. Pratica", "Esito attuale"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(22);

        AtomicReference<List<Prestazione>> cache = new AtomicReference<>(null);
        Runnable refreshEsiti = () -> refreshEsitiTable(tableModel, cache);
        refreshEsiti.run();

        JTextArea esitoArea = new JTextArea(4, 40);
        esitoArea.setLineWrap(true);
        esitoArea.setWrapStyleWord(true);
        JButton salvaBtn    = new JButton("Salva Esito");
        JButton aggiornaBtn = new JButton("Aggiorna lista");
        salvaBtn.setFont(new Font("SansSerif", Font.BOLD, 12));

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            List<Prestazione> lista = cache.get();
            if (lista == null || row < 0 || row >= lista.size()) return;
            esitoArea.setText(getEsitoOrEmpty(lista.get(row)));
        });

        salvaBtn.addActionListener(e -> handleSalvaEsito(panel, table, cache, esitoArea, refreshEsiti));
        aggiornaBtn.addActionListener(e -> refreshEsiti.run());

        JPanel editPanel = new JPanel(new BorderLayout(5, 5));
        editPanel.setBorder(BorderFactory.createTitledBorder("Compila / Modifica Esito"));
        JPanel bottomEdit = new JPanel(new BorderLayout());
        bottomEdit.add(new JScrollPane(esitoArea), BorderLayout.CENTER);
        bottomEdit.add(salvaBtn, BorderLayout.SOUTH);
        editPanel.add(bottomEdit, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(aggiornaBtn);

        panel.add(btnPanel,               BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(editPanel,              BorderLayout.SOUTH);
        return panel;
    }

    private void refreshEsitiTable(DefaultTableModel tableModel,
                                   AtomicReference<List<Prestazione>> cache) {
        List<Prestazione> lista = controller.getPrestazioniMedico(medicoCorrente.getIdMedico());
        cache.set(lista);
        tableModel.setRowCount(0);
        for (Prestazione p : lista) {
            tableModel.addRow(new Object[]{
                    p.getDataInizioPrestazione().toLocalDate().format(DATE_FMT),
                    p.getDataInizioPrestazione().format(TIME_FMT) + "–"
                            + p.getDataFinePrestazione().format(TIME_FMT),
                    p.getTipologiaPrestazione(),
                    p.getNumeroPratica(),
                    getEsitoDisplay(p)
            });
        }
    }

    private String getEsitoOrEmpty(Prestazione p) {
        return p.getEsito() == null ? "" : p.getEsito();
    }

    private String getEsitoDisplay(Prestazione p) {
        return (p.getEsito() == null || p.getEsito().isEmpty())
                ? "(non compilato)" : p.getEsito();
    }

    private void handleSalvaEsito(JPanel panel, JTable table,
                                  AtomicReference<List<Prestazione>> cache,
                                  JTextArea esitoArea, Runnable refreshEsiti) {
        int row = table.getSelectedRow();
        List<Prestazione> lista = cache.get();
        if (row < 0 || lista == null) {
            showWarning(panel, "Selezionare una prestazione.");
            return;
        }
        String nuovoEsito = esitoArea.getText().trim();
        if (nuovoEsito.isEmpty()) {
            showWarning(panel, "L'esito non può essere vuoto.");
            return;
        }
        try {
            Prestazione p = lista.get(row);
            controller.aggiornaEsito(p.getNumeroPratica(), p.getNumeroPrestazione(), nuovoEsito);
            refreshEsiti.run();
            JOptionPane.showMessageDialog(panel, "Esito salvato.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showWarning(JPanel panel, String msg) {
        JOptionPane.showMessageDialog(panel, msg, "Avviso", JOptionPane.WARNING_MESSAGE);
    }

    // Helper

    private JComboBox<Ricovero> buildRicoveroCombo() {
        JComboBox<Ricovero> cb = new JComboBox<>();
        controller.getRicoveriInCorso().forEach(cb::addItem);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object v,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, v, idx, sel, focus);
                if (v instanceof Ricovero r)
                    setText(r.getNumeroPratica() + " – letto " + r.getCodiceUnivocoLetto());
                return this;
            }
        });
        return cb;
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row,
                             String label, JComponent comp) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }
}
