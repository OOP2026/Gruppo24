package gui.panels;

import controller.Controller;
import exceptions.ValidationException;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static javax.swing.SwingConstants.LEFT;
import static utils.DateFormats.*;
import static utils.Messages.*;
import static utils.TimeZones.EUROPE_ROME;

public class AdminPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);

    private static final String FONT_SANS_SERIF = "SansSerif";

    private final transient  Controller controller;

    public AdminPanel(Controller controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane(LEFT);
        tabs.addTab("Pazienti",             buildPazientiPanel());
        tabs.addTab("Ricoveri",             buildRicoveriPanel());
        tabs.addTab("Malattia",             buildMalattiaPanel());
        tabs.addTab("Letti",                buildLettiPanel());
        tabs.addTab("Medici",               buildMediciPanel());
        tabs.addTab("Dimissioni",           buildDimissioniPanel());
        tabs.addTab("Pianificazione Turni", buildPianificazioneTurniPanel());
        tabs.addTab("Assegnazione Turni",   buildAssegnazioneTurniPanel());
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

            JButton addBtn = new JButton("Registra Ricovero");
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
            formPanel.setBorder(BorderFactory.createTitledBorder("Registra Ricovero"));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 8, 4, 8);

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
                showError(panel, DATE_FORMAT_MSG_USE + DATE_TIME_FORMAT_PATTERN);
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
                showError(panel, DATE_FORMAT_MSG);
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
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
                    showInfo(panel, "Nessuna dimissione prevista per " + dataField.getText().trim());
            } catch (DateTimeParseException ex) {
                showError(panel, DATE_FORMAT_MSG + " " + DATE_FORMAT_PATTERN);
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
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
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);
                for (Sostituto s : sostituti) {
                    sostitutoModel.addRow(new Object[]{
                        s.data().format(DATE_FMT),
                        s.fasciaOraria(),
                        s.oraInizio().format(timeFmt) + "–" + s.oraFine().format(timeFmt),
                        "Dr. " + s.nomeSostituto() + " " + s.cognomeSostituto()
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
                showError(panel, DATE_FORMAT_MSG + " " + DATE_FORMAT_PATTERN);
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

    // Pianificazione Turni

    private JPanel buildPianificazioneTurniPanel() {
        return new PianificazioneTurniTab().build();
    }

    private final class PianificazioneTurniTab {
        private final JPanel panel = new JPanel(new BorderLayout(5, 5));
        private final DefaultTableModel tableModel;
        private final JTable table;
        private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);
        private final JTextField dataField;
        private final JComboBox<FasciaOraria> fasciaCombo = new JComboBox<>(FasciaOraria.values());
        private final JSpinner spinnerInizio;
        private final JSpinner spinnerFine;
        private final JTextField dalField;
        private final JTextField alField;

        PianificazioneTurniTab() {
            String[] cols = {"Data", "Fascia", "Ora Inizio", "Ora Fine"};
            this.tableModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            this.table = new JTable(tableModel);
            LocalDate oggi = LocalDate.now(ZoneId.of(EUROPE_ROME));
            this.dataField    = new JTextField(oggi.format(DATE_FMT), 10);
            this.dataField.setToolTipText(DATE_FORMAT_PATTERN);
            this.spinnerInizio = buildTimeSpinner();
            this.spinnerFine   = buildTimeSpinner();
            LocalDate dal = oggi.with(DayOfWeek.MONDAY);
            LocalDate al  = dal.plusWeeks(3).minusDays(1);
            this.dalField = new JTextField(dal.format(DATE_FMT), 10);
            this.alField  = new JTextField(al.format(DATE_FMT), 10);
        }

        JPanel build() {
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            aggiornaSpin(spinnerInizio, spinnerFine, (FasciaOraria) fasciaCombo.getSelectedItem());
            fasciaCombo.addActionListener(e ->
                    aggiornaSpin(spinnerInizio, spinnerFine, (FasciaOraria) fasciaCombo.getSelectedItem()));
            refreshTabella();
            panel.add(buildNordPanel(), BorderLayout.NORTH);
            panel.add(buildCentroPanel(), BorderLayout.CENTER);
            return panel;
        }

        private JPanel buildNordPanel() {
            JPanel wrap = new JPanel(new BorderLayout(0, 5));

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createTitledBorder("Inserisci Nuovo Turno"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.fill   = GridBagConstraints.HORIZONTAL;
            addFormRow(form, gbc, 0, "Data (" + DATE_FORMAT_PATTERN + "):", dataField);
            addFormRow(form, gbc, 1, "Fascia oraria:", fasciaCombo);
            addFormRow(form, gbc, 2, "Ora inizio:", spinnerInizio);
            addFormRow(form, gbc, 3, "Ora fine:", spinnerFine);
            JButton pianificaBtn = new JButton("Pianifica Turno");
            pianificaBtn.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));
            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
            form.add(pianificaBtn, gbc);
            pianificaBtn.addActionListener(e -> handlePianifica());

            JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filtroPanel.setBorder(BorderFactory.createTitledBorder("Filtro periodo"));
            filtroPanel.add(new JLabel("Dal:"));
            filtroPanel.add(dalField);
            filtroPanel.add(new JLabel(" Al:"));
            filtroPanel.add(alField);
            JButton cercaBtn = new JButton("Cerca");
            cercaBtn.addActionListener(e -> refreshTabella());
            filtroPanel.add(cercaBtn);

            wrap.add(form, BorderLayout.CENTER);
            wrap.add(filtroPanel, BorderLayout.SOUTH);
            return wrap;
        }

        private JPanel buildCentroPanel() {
            JPanel centro = new JPanel(new BorderLayout(5, 5));
            centro.setBorder(BorderFactory.createTitledBorder("Turni Pianificati"));

            JButton modificaBtn = new JButton("Modifica Orari");
            JButton eliminaBtn  = new JButton("Elimina Turno");
            modificaBtn.addActionListener(e -> handleModifica());
            eliminaBtn.addActionListener(e -> handleElimina());

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            btnPanel.add(modificaBtn);
            btnPanel.add(eliminaBtn);

            centro.add(new JScrollPane(table), BorderLayout.CENTER);
            centro.add(btnPanel, BorderLayout.SOUTH);
            return centro;
        }

        private void handlePianifica() {
            FasciaOraria fascia = (FasciaOraria) fasciaCombo.getSelectedItem();
            if (fascia == null) { showError(panel, "Selezionare una fascia oraria."); return; }
            try {
                LocalDate data      = LocalDate.parse(dataField.getText().trim(), DATE_FMT);
                LocalTime oraInizio = spinnerToLocalTime(spinnerInizio);
                LocalTime oraFine   = spinnerToLocalTime(spinnerFine);
                controller.pianificaTurno(data, fascia, oraInizio, oraFine);
                refreshTabella();
                showInfo(panel, "Turno pianificato correttamente.");
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(panel,
                        DATE_FORMAT_MSG_USE + DATE_FORMAT_PATTERN,
                        ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage(),
                        ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
            }
        }

        private void handleModifica() {
            int row = table.getSelectedRow();
            if (row < 0) { showError(panel, "Selezionare un turno dalla tabella."); return; }
            try {
                LocalDate    data   = LocalDate.parse((String) tableModel.getValueAt(row, 0), DATE_FMT);
                FasciaOraria fascia = FasciaOraria.valueOf((String) tableModel.getValueAt(row, 1));

                JSpinner newInizio  = buildTimeSpinner();
                JSpinner newFine    = buildTimeSpinner();
                LocalTime currInizio = LocalTime.parse((String) tableModel.getValueAt(row, 2), timeFmt);
                LocalTime currFine   = LocalTime.parse((String) tableModel.getValueAt(row, 3), timeFmt);
                setSpinnerTime(newInizio, currInizio.getHour(), currInizio.getMinute());
                setSpinnerTime(newFine,   currFine.getHour(),   currFine.getMinute());

                JPanel dlg = new JPanel(new GridLayout(2, 2, 4, 4));
                dlg.add(new JLabel("Ora inizio:")); dlg.add(newInizio);
                dlg.add(new JLabel("Ora fine:"));   dlg.add(newFine);

                int res = JOptionPane.showConfirmDialog(panel, dlg,
                        "Modifica Orari – " + tableModel.getValueAt(row, 0) + " " + fascia,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (res != JOptionPane.OK_OPTION) return;

                controller.modificaTurnoPianificato(data, fascia,
                        spinnerToLocalTime(newInizio), spinnerToLocalTime(newFine));
                refreshTabella();
                showInfo(panel, "Orari aggiornati.");
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage(), ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        }

        private void handleElimina() {
            int row = table.getSelectedRow();
            if (row < 0) { showError(panel, "Selezionare un turno dalla tabella."); return; }
            String dataStr   = (String) tableModel.getValueAt(row, 0);
            String fasciaStr = (String) tableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(panel,
                    "Eliminare il turno del " + dataStr + " fascia " + fasciaStr
                            + "?\nSaranno eliminate anche le assegnazioni collegate.",
                    "Conferma eliminazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                controller.eliminaTurnoPianificato(
                        LocalDate.parse(dataStr, DATE_FMT), FasciaOraria.valueOf(fasciaStr));
                refreshTabella();
                showInfo(panel, "Turno eliminato.");
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage(), ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        }

        private void refreshTabella() {
            try {
                LocalDate dal = LocalDate.parse(dalField.getText().trim(), DATE_FMT);
                LocalDate al  = LocalDate.parse(alField.getText().trim(), DATE_FMT);
                tableModel.setRowCount(0);
                for (Turno t : controller.getTurniPianificati(dal, al)) {
                    tableModel.addRow(new Object[]{
                        t.getData().format(DATE_FMT),
                        t.getFasciaOraria().name(),
                        t.getOraInizio().format(timeFmt),
                        t.getOraFine().format(timeFmt)
                    });
                }
            } catch (DateTimeParseException ex) {
                // campi filtro non ancora validi — ignorato
            }
        }
    }

    // Assegnazione Turni

    private JPanel buildAssegnazioneTurniPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Assegna Turno a Medico"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        Map<Integer, String> repartiMap = new HashMap<>();
        controller.getReparti().forEach(r -> repartiMap.put(r.getIdReparto(), r.getNomeReparto()));

        JComboBox<Medico> medicoCombo = new JComboBox<>();
        controller.getMedici().forEach(medicoCombo::addItem);
        medicoCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Medico) {
                    Medico m = (Medico) value;
                    String rep = repartiMap.getOrDefault(m.getIdReparto(), "Rep. " + m.getIdReparto());
                    setText(m.getCognome() + " " + m.getNome() + " — " + rep);
                }
                return this;
            }
        });

        JTextField dataField = new JTextField(
                LocalDate.now(ZoneId.of(EUROPE_ROME)).format(DATE_FMT), 10);
        dataField.setToolTipText(DATE_FORMAT_PATTERN);

        JComboBox<FasciaOraria> fasciaCombo = new JComboBox<>(FasciaOraria.values());

        JLabel orariLabel = new JLabel(" ");

        JButton assegnaBtn = new JButton("Assegna Turno");
        assegnaBtn.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));
        assegnaBtn.setEnabled(false);

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);

        Runnable checkTurno = () -> {
            try {
                LocalDate    data   = LocalDate.parse(dataField.getText().trim(), DATE_FMT);
                FasciaOraria fascia = (FasciaOraria) fasciaCombo.getSelectedItem();
                if (fascia == null) return;
                Optional<Turno> opt = controller.trovaTurno(data, fascia);
                if (opt.isPresent()) {
                    Turno t = opt.get();
                    orariLabel.setText("Orari pianificati: "
                            + t.getOraInizio().format(timeFmt) + "–" + t.getOraFine().format(timeFmt));
                    orariLabel.setForeground(UIManager.getColor("Label.foreground"));
                    assegnaBtn.setEnabled(true);
                } else {
                    orariLabel.setText("Turno non pianificato per questa data e fascia. Pianificarlo prima nel tab dedicato.");
                    orariLabel.setForeground(Color.RED);
                    assegnaBtn.setEnabled(false);
                }
            } catch (DateTimeParseException ex) {
                orariLabel.setText(" ");
                assegnaBtn.setEnabled(false);
            } catch (Exception ex) {
                orariLabel.setText("Errore: " + ex.getMessage());
                orariLabel.setForeground(Color.RED);
                assegnaBtn.setEnabled(false);
            }
        };

        dataField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { checkTurno.run(); }
            @Override public void removeUpdate(DocumentEvent e)  { checkTurno.run(); }
            @Override public void changedUpdate(DocumentEvent e) { checkTurno.run(); }
        });
        fasciaCombo.addActionListener(e -> checkTurno.run());
        checkTurno.run();

        addFormRow(formPanel, gbc, 0, "Medico:",                          medicoCombo);
        addFormRow(formPanel, gbc, 1, "Data (" + DATE_FORMAT_PATTERN + "):", dataField);
        addFormRow(formPanel, gbc, 2, "Fascia oraria:",                   fasciaCombo);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(orariLabel, gbc);

        gbc.gridy = 4;
        formPanel.add(assegnaBtn, gbc);

        assegnaBtn.addActionListener(e -> {
            Medico       medico = (Medico) medicoCombo.getSelectedItem();
            FasciaOraria fascia = (FasciaOraria) fasciaCombo.getSelectedItem();
            if (medico == null || fascia == null) {
                showError(panel, "Selezionare medico e fascia oraria.");
                return;
            }
            try {
                LocalDate data = LocalDate.parse(dataField.getText().trim(), DATE_FMT);
                controller.assegnaTurnoAMedico(medico.getIdMedico(), data, fascia);
                JOptionPane.showMessageDialog(panel, "Turno assegnato correttamente.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(panel,
                        DATE_FORMAT_MSG_USE + DATE_FORMAT_PATTERN,
                        ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage(),
                        ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                showError(panel, ex.getMessage());
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);
        return panel;
    }

    private JSpinner buildTimeSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, TIME_FORMAT_PATTERN));
        return spinner;
    }

    private LocalTime spinnerToLocalTime(JSpinner spinner) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(EUROPE_ROME));
        cal.setTime((Date) spinner.getValue());
        return LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    private void aggiornaSpin(JSpinner inizio, JSpinner fine, FasciaOraria fascia) {
        if (fascia == null) return;
        switch (fascia) {
            case MATTINA:    setSpinnerTime(inizio, 6, 0);  setSpinnerTime(fine, 14, 0); break;
            case POMERIGGIO: setSpinnerTime(inizio, 14, 0); setSpinnerTime(fine, 22, 0); break;
            case NOTTE:      setSpinnerTime(inizio, 22, 0); setSpinnerTime(fine, 6, 0);  break;
            default: break;
        }
    }

    private void setSpinnerTime(JSpinner spinner, int hour, int minute) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(EUROPE_ROME));
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        spinner.setValue(cal.getTime());
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
        gbc.gridy = row;

        // Colonna 0: Label (fissa a sinistra, non si espande)
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_START;
        p.add(new JLabel(label), gbc);

        // Colonna 1: Componente di input (si espande e riempie la riga)
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
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
