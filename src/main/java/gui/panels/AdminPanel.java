package gui.panels;

import controller.Controller;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Pannello principale per il ruolo Amministratore.
 * Contiene un JTabbedPane con 6 tab:
 *   1. Pazienti        – anagrafica pazienti
 *   2. Ricoveri        – gestione ricoveri e dimissioni
 *   3. Letti           – disponibilità letti per reparto
 *   4. Dimissioni      – pazienti in scadenza
 *   5. Medici          – elenco medici per reparto
 *   6. Malattia        – registrazione assenza e medici sostitutivi
 */
public class AdminPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Controller controller;

    public AdminPanel(Controller controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.addTab("+Pazienti",   buildPazientiPanel());
        tabs.addTab("+Ricoveri",   buildRicoveriPanel());
        tabs.addTab("+Malattia",   buildMalattiaPanel());
        tabs.addTab("-Letti",      buildLettiPanel());
        tabs.addTab("-Medici",     buildMediciPanel());
        tabs.addTab("-Dimissioni", buildDimissioniPanel());
        add(tabs, BorderLayout.CENTER);
    }



    private JPanel buildPazientiPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Codice Fiscale", "Nome", "Cognome", "Data Nascita"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        refreshPazienti(model);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Aggiungi / Modifica Paziente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField cfField       = new JTextField(15);
        JTextField nomeField     = new JTextField(12);
        JTextField cognomeField  = new JTextField(12);
        JTextField nascitaField  = new JTextField(10);
        nascitaField.setToolTipText("gg/mm/aaaa");

        addRow(formPanel, gbc, 0, "CF:",           cfField);
        addRow(formPanel, gbc, 1, "Nome:",         nomeField);
        addRow(formPanel, gbc, 2, "Cognome:",      cognomeField);
        addRow(formPanel, gbc, 3, "Data nascita:", nascitaField);

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
            cfField.setText((String) model.getValueAt(row, 0));
            nomeField.setText((String) model.getValueAt(row, 1));
            cognomeField.setText((String) model.getValueAt(row, 2));
            nascitaField.setText((String) model.getValueAt(row, 3));
        });

        addBtn.addActionListener(e -> {
            try {
                LocalDate nascita = LocalDate.parse(nascitaField.getText().trim(), DATE_FMT);
                controller.aggiungiPaziente(cfField.getText().trim().toUpperCase(),
                        nomeField.getText().trim(), cognomeField.getText().trim(), nascita);
                refreshPazienti(model);
                clearFields(cfField, nomeField, cognomeField, nascitaField);
                showInfo(panel, "Paziente aggiunto con successo.");
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showError(panel, "Selezionare un paziente dalla tabella."); return; }
            String cf = (String) model.getValueAt(row, 0);
            Paziente paz = controller.trovaPazientePerCF(cf);
            if (paz == null) return;
            try {
                LocalDate nascita = LocalDate.parse(nascitaField.getText().trim(), DATE_FMT);
                controller.modificaPaziente(paz, nomeField.getText().trim(),
                        cognomeField.getText().trim(), nascita);
                refreshPazienti(model);
                showInfo(panel, "Paziente modificato.");
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshPazienti(DefaultTableModel model) {
        model.setRowCount(0);
        for (Paziente p : controller.getPazienti()) {
            model.addRow(new Object[]{
                p.getCodiceFiscale(), p.getNome(), p.getCognome(),
                p.getDataNascita().format(DATE_FMT)
            });
        }
    }



    private JPanel buildRicoveriPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Paziente (CF)", "Letto", "Inizio", "Dimissione prevista", "In corso"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        refreshRicoveri(model);


        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Nuovo Ricovero"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<Paziente> pazienteCombo = buildPazienteCombo();
        JComboBox<Reparto>  repartoCombo  = buildRepartoCombo();
        JComboBox<Letto>    lettoCombo    = new JComboBox<>();
        JTextField          inizioField   = new JTextField("dd/MM/yyyy HH:mm", 16);
        JTextField          fineField     = new JTextField("dd/MM/yyyy HH:mm (facoltativo)", 22);


        repartoCombo.addActionListener(e -> {
            lettoCombo.removeAllItems();
            Reparto r = (Reparto) repartoCombo.getSelectedItem();
            if (r != null) controller.getTuttiLetti(r).forEach(lettoCombo::addItem);
        });
        if (repartoCombo.getItemCount() > 0)
            repartoCombo.setSelectedIndex(0);

        addRow(formPanel, gbc, 0, "Paziente:",  pazienteCombo);
        addRow(formPanel, gbc, 1, "Reparto:",   repartoCombo);
        addRow(formPanel, gbc, 2, "Letto:",     lettoCombo);
        addRow(formPanel, gbc, 3, "Inizio:",    inizioField);
        addRow(formPanel, gbc, 4, "Dimissione (prev.):", fineField);

        JButton addBtn      = new JButton("Aggiungi Ricovero");
        JButton dimettBtn   = new JButton("Registra Dimissione");
        JPanel  btnPanel    = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addBtn);
        btnPanel.add(dimettBtn);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        addBtn.addActionListener(e -> {
            try {
                Paziente paz  = (Paziente) pazienteCombo.getSelectedItem();
                Letto    letto = (Letto)   lettoCombo.getSelectedItem();
                if (paz == null || letto == null) { showError(panel, "Selezionare paziente e letto."); return; }
                LocalDateTime inizio = LocalDateTime.parse(inizioField.getText().trim(), DATETIME_FMT);
                LocalDateTime fine   = null;
                String fineStr = fineField.getText().trim();
                if (!fineStr.isEmpty() && !fineStr.startsWith("dd/MM")) {
                    fine = LocalDateTime.parse(fineStr, DATETIME_FMT);
                }
                controller.aggiungiRicovero(paz, letto, inizio, fine);
                refreshRicoveri(model);
                showInfo(panel, "Ricovero registrato.");
            } catch (DateTimeParseException ex) {
                showError(panel, "Formato data non valido. Usare: dd/MM/yyyy HH:mm");
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        });

        dimettBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showError(panel, "Selezionare un ricovero dalla tabella."); return; }
            if ("No".equals(model.getValueAt(row, 4))) {
                showError(panel, "Ricovero già concluso."); return;
            }
            String dataStr = JOptionPane.showInputDialog(panel,
                "Data e ora dimissione (dd/MM/yyyy HH:mm):", "Registra Dimissione",
                JOptionPane.QUESTION_MESSAGE);
            if (dataStr == null) return;
            try {
                LocalDateTime dataDimissione = LocalDateTime.parse(dataStr.trim(), DATETIME_FMT);
                Ricovero r = controller.getRicoveri().get(row);
                controller.registraDimissione(r, dataDimissione);
                refreshRicoveri(model);
                showInfo(panel, "Dimissione registrata.");
            } catch (DateTimeParseException ex) {
                showError(panel, "Formato data non valido.");
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshRicoveri(DefaultTableModel model) {
        model.setRowCount(0);
        for (Ricovero r : controller.getRicoveri()) {
            model.addRow(new Object[]{
                r.getPaziente().getCodiceFiscale(),
                r.getLetto().getCodiceUnivoco(),
                r.getDataInizio().format(DATETIME_FMT),
                r.getDataDimissione() != null ? r.getDataDimissione().format(DATETIME_FMT) : "–",
                r.isInCorso() ? "Sì" : "No"
            });
        }
    }


    private JPanel buildLettiPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<Reparto> repartoCombo = buildRepartoCombo();
        JPanel lettiGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JScrollPane scroll = new JScrollPane(lettiGrid);

        Runnable aggiorna = () -> {
            lettiGrid.removeAll();
            Reparto rep = (Reparto) repartoCombo.getSelectedItem();
            if (rep == null) return;
            for (Letto l : controller.getTuttiLetti(rep)) {
                boolean occupato = controller.isLettoOccupatoOra(l);
                JLabel badge = new JLabel(l.getCodiceUnivoco(), SwingConstants.CENTER);
                badge.setOpaque(true);
                badge.setPreferredSize(new Dimension(130, 50));
                badge.setFont(new Font("SansSerif", Font.BOLD, 12));
                badge.setBackground(occupato ? Color.RED : new Color(39, 174, 96));
                badge.setForeground(Color.WHITE);
                badge.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                badge.setToolTipText(occupato ? "OCCUPATO" : "DISPONIBILE");
                lettiGrid.add(badge);
            }
            lettiGrid.revalidate();
            lettiGrid.repaint();
        };

        repartoCombo.addActionListener(e -> aggiorna.run());
        if (repartoCombo.getItemCount() > 0) {
            repartoCombo.setSelectedIndex(0);
            aggiorna.run();
        }

        JButton refreshBtn = new JButton("Aggiorna");
        refreshBtn.addActionListener(e -> aggiorna.run());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Reparto:"));
        top.add(repartoCombo);
        top.add(refreshBtn);

//magari aggiungere che quando il cursore è sul letto questo fa qualcosa(es."disponibile/occupato" in casella testo)

        panel.add(top,    BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }



    private JPanel buildDimissioniPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Paziente", "CF", "Letto", "Dimissione Prevista"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        JTextField dataField = new JTextField(LocalDate.now().format(DATE_FMT), 10);
        JButton    oggiBtn   = new JButton("Oggi");
        JButton    cercaBtn  = new JButton("Cerca");

        Runnable cerca = () -> {
            try {
                LocalDate data = LocalDate.parse(dataField.getText().trim(), DATE_FMT);
                List<Ricovero> risultati = controller.getPazientiInScadenza(data);
                model.setRowCount(0);
                for (Ricovero r : risultati) {
                    Paziente p = r.getPaziente();
                    model.addRow(new Object[]{
                        p.getNome() + " " + p.getCognome(),
                        p.getCodiceFiscale(),
                        r.getLetto().getCodiceUnivoco(),
                        r.getDataDimissione().format(DATETIME_FMT)
                    });
                }
                if (risultati.isEmpty())
                    showInfo(panel, "Nessuna dimissione prevista per " + dataField.getText().trim());
            } catch (DateTimeParseException ex) {
                showError(panel, "Formato data non valido (dd/MM/yyyy).");
            }
        };

        oggiBtn.addActionListener(e -> {
            dataField.setText(LocalDate.now().format(DATE_FMT));
            cerca.run();
        });
        cercaBtn.addActionListener(e -> cerca.run());
        cerca.run(); // carica subito con la data di oggi

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Data dimissione (dd/MM/yyyy):"));
        top.add(dataField);
        top.add(oggiBtn);
        top.add(cercaBtn);

        panel.add(top,                       BorderLayout.NORTH);
        panel.add(new JScrollPane(table),    BorderLayout.CENTER);
        return panel;
    }


    private JPanel buildMediciPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Matricola", "Nome", "Cognome", "Login", "Reparto", "N° Turni", "N° Prestazioni"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        Runnable refreshMedici = () -> {
            model.setRowCount(0);
            for (Medico m : controller.getMedici()) {
                model.addRow(new Object[]{
                    m.getMatricola(), m.getNome(), m.getCognome(), m.getLogin(),
                    m.getReparto().getNome(),
                    m.getTurniProgrammati().size(),
                    m.getPrestazioniErogate().size()
                });
            }
        };
        refreshMedici.run();

        JButton refreshBtn = new JButton("Aggiorna");
        refreshBtn.addActionListener(e -> refreshMedici.run());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);

        panel.add(top,                       BorderLayout.NORTH);
        panel.add(new JScrollPane(table),    BorderLayout.CENTER);
        return panel;
    }



    private JPanel buildMalattiaPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Form registrazione malattia ──────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registra Periodo di Malattia"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<Medico> medicoCombo = buildMedicoCombo();
        JTextField        inizioField = new JTextField("dd/MM/yyyy", 10);
        JTextField        fineField   = new JTextField("dd/MM/yyyy", 10);

        addRow(formPanel, gbc, 0, "Medico:",         medicoCombo);
        addRow(formPanel, gbc, 1, "Inizio malattia:", inizioField);
        addRow(formPanel, gbc, 2, "Fine malattia:",   fineField);

        JButton registraBtn = new JButton("Registra Malattia e Cerca Sostituti");
        registraBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(registraBtn, gbc);


        JTextArea risultatiArea = new JTextArea(12, 40);
        risultatiArea.setEditable(false);
        risultatiArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        risultatiArea.setText("Seleziona un medico e il periodo, poi premi il pulsante.");

        registraBtn.addActionListener(e -> {
            Medico medico = (Medico) medicoCombo.getSelectedItem();
            if (medico == null) { showError(panel, "Selezionare un medico."); return; }
            try {
                LocalDate inizio = LocalDate.parse(inizioField.getText().trim(), DATE_FMT);
                LocalDate fine   = LocalDate.parse(fineField.getText().trim(),   DATE_FMT);

                controller.registraMalattia(medico, inizio, fine);

                List<model.Turno>       turniScoperti      = controller.getTurniScoperti(medico, inizio, fine);
                List<model.Prestazione> prestazioniScoperte = controller.getPrestazioniScoperte(medico, inizio, fine);
                List<Medico>            sostitutivi        = controller.getMediciSostitutivi(medico, inizio, fine);

                StringBuilder sb = new StringBuilder();
                sb.append("═══ MALATTIA REGISTRATA ═══\n");
                sb.append("Medico:  Dr. ").append(medico.getNome()).append(" ").append(medico.getCognome()).append("\n");
                sb.append("Periodo: ").append(inizio.format(DATE_FMT)).append(" → ").append(fine.format(DATE_FMT)).append("\n\n");

                sb.append("─── TURNI SCOPERTI (").append(turniScoperti.size()).append(") ───\n");
                for (model.Turno t : turniScoperti) {
                    sb.append("  • ").append(t.getGiornoDellaSettimana())
                      .append("  ").append(t.getOraInizio()).append("–").append(t.getOraFine()).append("\n");
                }

                sb.append("\n─── PRESTAZIONI SCOPERTE (").append(prestazioniScoperte.size()).append(") ───\n");
                for (model.Prestazione p : prestazioniScoperte) {
                    sb.append("  • ").append(p.getInizio().format(DATETIME_FMT))
                      .append(" → ").append(p.getFine().format(DATETIME_FMT))
                      .append(" [").append(p.getTipo()).append("]\n");
                }

                sb.append("\n═══ MEDICI SOSTITUTIVI DISPONIBILI (").append(sostitutivi.size()).append(") ═══\n");
                if (sostitutivi.isEmpty()) {
                    sb.append("  ⚠  Nessun sostituto disponibile nel reparto.\n");
                } else {
                    for (Medico s : sostitutivi) {
                        sb.append("  ✓ Dr. ").append(s.getNome()).append(" ").append(s.getCognome())
                          .append("  (").append(s.getMatricola()).append(")\n");
                    }
                }
                risultatiArea.setText(sb.toString());

            } catch (DateTimeParseException ex) {
                showError(panel, "Formato data non valido (dd/MM/yyyy).");
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        });

        panel.add(formPanel,                              BorderLayout.NORTH);
        panel.add(new JScrollPane(risultatiArea),         BorderLayout.CENTER);
        return panel;
    }


    private JComboBox<Paziente> buildPazienteCombo() {
        JComboBox<Paziente> cb = new JComboBox<>();
        controller.getPazienti().forEach(cb::addItem);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object v,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, v, idx, sel, focus);
                if (v instanceof Paziente p)
                    setText(p.getNome() + " " + p.getCognome() + " (" + p.getCodiceFiscale() + ")");
                return this;
            }
        });
        return cb;
    }

    private JComboBox<Reparto> buildRepartoCombo() {
        JComboBox<Reparto> cb = new JComboBox<>();
        controller.getReparti().forEach(cb::addItem);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object v,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, v, idx, sel, focus);
                if (v instanceof Reparto r) setText(r.getNome());
                return this;
            }
        });
        return cb;
    }

    private JComboBox<Medico> buildMedicoCombo() {
        JComboBox<Medico> cb = new JComboBox<>();
        controller.getMedici().forEach(cb::addItem);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object v,
                    int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, v, idx, sel, focus);
                if (v instanceof Medico m)
                    setText("Dr. " + m.getNome() + " " + m.getCognome() + " – " + m.getReparto().getNome());
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

    private void clearFields(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }

    private void showError(JPanel parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(JPanel parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
