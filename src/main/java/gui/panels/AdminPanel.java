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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.swing.SwingConstants.LEFT;
import static utils.DateFormats.DATE_FORMAT_PATTERN;
import static utils.DateFormats.DATE_TIME_FORMAT_PATTERN;

public class AdminPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);

    private static final String FONT_SANS_SERIF = "SansSerif";

    private transient final Controller controller;

    public AdminPanel(Controller controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane(LEFT);
        tabs.addTab("Pazienti",    buildPazientiPanel());
        tabs.addTab("Ricoveri",    buildRicoveriPanel());
        tabs.addTab("Malattia",    buildMalattiaPanel());
        tabs.addTab("Letti",       buildLettiPanel());
        tabs.addTab("Medici",      buildMediciPanel());
        tabs.addTab("Dimissioni",  buildDimissioniPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // Pazienti

    private JPanel buildPazientiPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        addFormRow(formPanel, gbc, 0, "CF:",           cfField);
        addFormRow(formPanel, gbc, 1, "Nome:",         nomeField);
        addFormRow(formPanel, gbc, 2, "Cognome:",      cognomeField);
        addFormRow(formPanel, gbc, 3, "Data nascita:", nascitaField);

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
                showInfo(panel, "Paziente aggiunto con successo.");
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showError(panel, "Selezionare un paziente dalla tabella."); return; }
            int idPaziente = (int) tableModel.getValueAt(row, 4);
            try {
                LocalDate nascita = LocalDate.parse(nascitaField.getText().trim(), DATE_FMT);
                controller.modificaPaziente(idPaziente, nomeField.getText().trim(),
                        cognomeField.getText().trim(), nascita);
                refreshPazienti(tableModel);
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
                p.getDataNascita().format(DATE_FMT),
                p.getIdPaziente()
            });
        }
    }

    private JPanel buildRicoveriPanel() {
        return new RicoveriTab().build();
    }

    // Ricoveri
    private final class RicoveriTab {
        private final JPanel panel = new JPanel(new BorderLayout(5, 5));
        private final DefaultTableModel tableModel;
        private final JTable table;
        private final JTextField praticaField  = new JTextField(15);
        private final JComboBox<Paziente> pazienteCombo = new JComboBox<>();
        private final JComboBox<Reparto>  repartoCombo  = new JComboBox<>();
        private final JComboBox<Letto>    lettoCombo    = new JComboBox<>();
        private final JTextField inizioField = new JTextField(DATE_TIME_FORMAT_PATTERN, 22);
        private final JTextField fineField   = new JTextField(DATE_TIME_FORMAT_PATTERN + " (facoltativo)", 22);
        private final JTextField motivoField = new JTextField(20);

        RicoveriTab() {
            String[] cols = {"N. Pratica", "Letto", "Inizio", "Dimissione", "In corso"};
            this.tableModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            this.table = new JTable(tableModel);
        }

        JPanel build() {
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            refreshRicoveri(tableModel);

            controller.getPazienti().forEach(pazienteCombo::addItem);
            controller.getReparti().forEach(repartoCombo::addItem);

            repartoCombo.addActionListener(e -> aggiornaLetti());
            if (repartoCombo.getItemCount() > 0) repartoCombo.setSelectedIndex(0);

            JButton addBtn    = new JButton("Registra Ricovero");
            JButton dimettBtn = new JButton("Registra Dimissione");
            addBtn.addActionListener(e -> handleAggiungiRicovero());
            dimettBtn.addActionListener(e -> handleDimissione());

            JPanel btnPanel = new JPanel();
            btnPanel.add(addBtn);
            btnPanel.add(dimettBtn);

            panel.add(buildFormPanel(), BorderLayout.NORTH);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.add(btnPanel, BorderLayout.SOUTH);
            return panel;
        }

        private JPanel buildFormPanel() {
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            addFormRow(formPanel, gbc, 0, "N. Pratica:", praticaField);
            addFormRow(formPanel, gbc, 1, "Paziente:",   pazienteCombo);
            addFormRow(formPanel, gbc, 2, "Reparto:",    repartoCombo);
            addFormRow(formPanel, gbc, 3, "Letto:",      lettoCombo);
            addFormRow(formPanel, gbc, 4, "Inizio:",     inizioField);
            addFormRow(formPanel, gbc, 5, "Fine:",       fineField);
            addFormRow(formPanel, gbc, 6, "Motivo:",     motivoField);
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
                    showError(panel, "Selezionare paziente e letto.");
                    return;
                }
                LocalDateTime inizio = LocalDateTime.parse(inizioField.getText().trim(), DATETIME_FMT);
                LocalDateTime fine   = parseFineOptional(fineField.getText().trim());

                controller.registraRicovero(praticaField.getText().trim(), paz.getIdPaziente(),
                        letto.getCodiceUnivoco(), inizio, fine, motivoField.getText().trim());
                refreshRicoveri(tableModel);
                clearFields(praticaField, inizioField, fineField, motivoField);
                showInfo(panel, "Ricovero registrato.");
            } catch (DateTimeParseException ex) {
                showError(panel, "Formato data non valido. Usare: " + DATE_TIME_FORMAT_PATTERN);
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        }

        private void handleDimissione() {
            int row = table.getSelectedRow();
            if (row < 0) {
                showError(panel, "Selezionare un ricovero dalla tabella.");
                return;
            }
            if ("No".equals(tableModel.getValueAt(row, 4))) {
                showError(panel, "Ricovero già concluso.");
                return;
            }
            String numeroPratica = (String) tableModel.getValueAt(row, 0);
            String dataStr = JOptionPane.showInputDialog(panel,
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
                showInfo(panel, "Dimissione registrata.");
            } catch (DateTimeParseException ex) {
                showError(panel, "Formato data non valido.");
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
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

    // Letti

    private JPanel buildLettiPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<Reparto> repartoCombo = buildRepartoCombo();
        JPanel lettiGrid  = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JScrollPane scroll = new JScrollPane(lettiGrid);

        Runnable aggiorna = () -> {
            lettiGrid.removeAll();
            Reparto rep = (Reparto) repartoCombo.getSelectedItem();
            if (rep == null) return;
            Set<String> occupati = new HashSet<>();
            controller.getLettiOccupati().forEach(l -> occupati.add(l.getCodiceUnivoco()));
            for (Letto l : controller.getLettiPerReparto(rep.getIdReparto())) {
                JLabel badge = getJLabel(l, occupati);
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
        JLabel legenda = new JLabel("  ■ Rosso = Occupato   ■ Verde = Disponibile");
        legenda.setFont(new Font(FONT_SANS_SERIF, Font.PLAIN, 11));
        top.add(legenda);

        panel.add(top,    BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel getJLabel(Letto l, Set<String> occupati) {
        boolean isOccupato = occupati.contains(l.getCodiceUnivoco());
        JLabel badge = new JLabel(l.getCodiceUnivoco(), SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setPreferredSize(new Dimension(130, 50));
        badge.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));
        badge.setBackground(isOccupato ? Color.RED : new Color(39, 174, 96));
        badge.setForeground(Color.WHITE);
        badge.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        badge.setToolTipText(isOccupato ? "OCCUPATO" : "DISPONIBILE");
        return badge;
    }

    // Dimissioni

    private JPanel buildDimissioniPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Paziente", "CF", "Letto", "Dimissione Prevista"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);

        JTextField dataField = new JTextField(LocalDate.now().format(DATE_FMT), 10);
        JButton    oggiBtn   = new JButton("Oggi");
        JButton    cercaBtn  = new JButton("Cerca");

        Runnable cerca = () -> {
            try {
                LocalDate data = LocalDate.parse(dataField.getText().trim(), DATE_FMT);
                List<PazienteInDimissione> risultati = controller.getPazientiInDimissione(data);
                tableModel.setRowCount(0);
                for (PazienteInDimissione pid : risultati) {
                    tableModel.addRow(new Object[]{
                        pid.getNome() + " " + pid.getCognome(),
                        pid.getCodiceFiscale(),
                        pid.getCodiceUnivocoLetto(),
                        pid.getDataDimissione().format(DATETIME_FMT)
                    });
                }
                if (risultati.isEmpty())
                    showInfo(panel, "Nessuna dimissione prevista per " + dataField.getText().trim());
            } catch (DateTimeParseException ex) {
                showError(panel, "Formato data non valido " + DATE_FORMAT_PATTERN);
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        };

        oggiBtn.addActionListener(e -> { dataField.setText(LocalDate.now().format(DATE_FMT)); cerca.run(); });
        cercaBtn.addActionListener(e -> cerca.run());
        cerca.run();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Data dimissione " + DATE_FORMAT_PATTERN));
        top.add(dataField);
        top.add(oggiBtn);
        top.add(cercaBtn);

        panel.add(top,                    BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // Medici

    private JPanel buildMediciPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Matricola", "Nome", "Cognome", "Login", "Reparto"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);

        java.util.Map<Integer, String> repartiMap = new java.util.HashMap<>();
        controller.getReparti().forEach(r -> repartiMap.put(r.getIdReparto(), r.getNomeReparto()));

        Runnable refresh = () -> {
            tableModel.setRowCount(0);
            for (Medico m : controller.getMedici()) {
                tableModel.addRow(new Object[]{
                    m.getMatricola(), m.getNome(), m.getCognome(), m.getLogin(),
                    repartiMap.getOrDefault(m.getIdReparto(), "Reparto " + m.getIdReparto())
                });
            }
        };
        refresh.run();

        JButton refreshBtn = new JButton("Aggiorna");
        refreshBtn.addActionListener(e -> refresh.run());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);

        panel.add(top,                    BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // Malattia

    private JPanel buildMalattiaPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registra Periodo di Malattia"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<Medico> medicoCombo   = buildMedicoCombo();
        JTextField        codiceField   = new JTextField(15);
        JTextField        inizioField   = new JTextField(DATE_FORMAT_PATTERN, 10);
        JTextField        fineField     = new JTextField(DATE_FORMAT_PATTERN, 10);

        addFormRow(formPanel, gbc, 0, "Medico:",              medicoCombo);
        addFormRow(formPanel, gbc, 1, "Codice Certificato:",  codiceField);
        addFormRow(formPanel, gbc, 2, "Inizio malattia:",     inizioField);
        addFormRow(formPanel, gbc, 3, "Fine malattia:",       fineField);

        JButton registraBtn = new JButton("Registra Malattia e Cerca Sostituti");
        registraBtn.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(registraBtn, gbc);

        String[] sostitutoCols = {"Data", "Fascia", "Orario", "Sostituto"};
        DefaultTableModel sostitutoModel = new DefaultTableModel(sostitutoCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable sostitutoTable = new JTable(sostitutoModel);
        JLabel sostitutoHeader = new JLabel("Sostituti disponibili per i turni scoperti:", LEFT);
        sostitutoHeader.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));

        registraBtn.addActionListener(e -> {
            Medico medico = (Medico) medicoCombo.getSelectedItem();
            if (medico == null) { showError(panel, "Selezionare un medico."); return; }
            try {
                LocalDate inizio = LocalDate.parse(inizioField.getText().trim(), DATE_FMT);
                LocalDate fine   = LocalDate.parse(fineField.getText().trim(), DATE_FMT);

                controller.registraPeriodoMalattia(codiceField.getText().trim(),
                        medico.getIdMedico(), inizio, fine);

                List<Sostituto> sostituti = controller.trovaSostituti(medico.getIdMedico(), inizio, fine);
                sostitutoModel.setRowCount(0);
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
                for (Sostituto s : sostituti) {
                    sostitutoModel.addRow(new Object[]{
                        s.getData().format(DATE_FMT),
                        s.getFasciaOraria(),
                        s.getOraInizio().format(timeFmt) + "–" + s.getOraFine().format(timeFmt),
                        "Dr. " + s.getNomeSostituto() + " " + s.getCognomeSostituto()
                    });
                }
                if (sostituti.isEmpty())
                    sostitutoHeader.setText("Nessun sostituto disponibile per i turni scoperti.");
                else
                    sostitutoHeader.setText("Sostituti disponibili (" + sostituti.size() + " righe):");
                clearFields(codiceField, inizioField, fineField);
                showInfo(panel, "Periodo di malattia registrato per Dr. "
                        + medico.getNome() + " " + medico.getCognome() + ".");
            } catch (DateTimeParseException ex) {
                showError(panel, "Formato data non valido " + DATE_FORMAT_PATTERN);
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        });

        JPanel risultatiPanel = new JPanel(new BorderLayout(5, 5));
        risultatiPanel.add(sostitutoHeader,              BorderLayout.NORTH);
        risultatiPanel.add(new JScrollPane(sostitutoTable), BorderLayout.CENTER);

        panel.add(formPanel,      BorderLayout.NORTH);
        panel.add(risultatiPanel, BorderLayout.CENTER);
        return panel;
    }

    // Helper

    private JComboBox<Reparto> buildRepartoCombo() {
        JComboBox<Reparto> cb = new JComboBox<>();
        controller.getReparti().forEach(cb::addItem);
        return cb;
    }

    private JComboBox<Medico> buildMedicoCombo() {
        JComboBox<Medico> cb = new JComboBox<>();
        controller.getMedici().forEach(cb::addItem);
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
