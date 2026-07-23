package gui.panels.admin;

import controller.Controller;
import exceptions.ValidationException;
import gui.components.DatePickerField;
import gui.utils.GuiUtils;
import model.Medico;
import model.PeriodoMalattia;
import model.PrestazioneScoperta;
import model.RiassegnazionePrestazione;
import model.RiassegnazioneTurno;
import model.TurnoScoperto;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import static gui.utils.GuiUtils.*;
import static utils.DateFormats.*;
import static utils.Messages.*;
import static utils.TimeZones.EUROPE_ROME;

public class MalattiaPanel extends JPanel {

    private static final String FONT_SANS_SERIF = "SansSerif";
    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    private static final DateTimeFormatter TIME_FMT     = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);

    private static final String NON_RIASSEGNARE = "-- Non riassegnare --";
    private static final String NESSUNO         = "Nessuno disponibile";
    private static final int    TURNO_COL_SOST  = 3;
    private static final int    PREST_COL_SOST  = 4;

    private final transient Controller controller;

    private final JComboBox<Medico> medicoCombo;
    private final JTextField codiceField = new JTextField(15);
    private final DatePickerField inizioField = new DatePickerField(LocalDate.now(ZoneId.of(EUROPE_ROME)));
    private final DatePickerField fineField   = new DatePickerField(LocalDate.now(ZoneId.of(EUROPE_ROME)));
    private final JComboBox<PeriodoMalattia> caricaMalattiaCombo = new JComboBox<>();

    private final DefaultTableModel turnoModel;
    private final DefaultTableModel prestazioneModel;
    private final JTable turnoTable;
    private final JTable prestazioneTable;
    private final JLabel turniHeader       = new JLabel("Turni scoperti:");
    private final JLabel prestazioniHeader = new JLabel("Prestazioni scoperte:");
    private final JButton applicaBtn = new JButton("Riassegna Turni e Prestazioni Selezionati");

    private final transient Runnable refreshTurniPanels;

    private transient List<TurnoScoperto> turniScopertiList = new ArrayList<>();
    private transient List<PrestazioneScoperta> prestazioniScoperteList = new ArrayList<>();

    private int idMedicoAssente;
    private LocalDate periodoInizio;
    private LocalDate periodoFine;
    private String codiceCorrente;

    public MalattiaPanel(Controller controller) {
        this(controller, () -> {});
    }

    public MalattiaPanel(Controller controller, Runnable refreshTurniPanels) {
        super(new BorderLayout(5, 10));
        this.controller = controller;
        this.refreshTurniPanels = refreshTurniPanels != null ? refreshTurniPanels : () -> {};
        this.medicoCombo = buildMedicoCombo();

        this.turnoModel = new DefaultTableModel(new String[]{"Data", "Fascia", "Orario", "Sostituto"}, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == TURNO_COL_SOST && r < turniScopertiList.size()
                        && !turniScopertiList.get(r).sostitutiCandidati().isEmpty();
            }
        };
        this.prestazioneModel = new DefaultTableModel(
                new String[]{"Pratica", "N.", "Data/Ora", "Tipo", "Sostituto"}, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c == PREST_COL_SOST && r < prestazioniScoperteList.size()
                        && !prestazioniScoperteList.get(r).sostitutiCandidati().isEmpty();
            }
        };
        this.turnoTable = new JTable(turnoModel);
        this.prestazioneTable = new JTable(prestazioneModel);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildCaricaMalattiaCombo();
        buildUI();
    }

    private void buildCaricaMalattiaCombo() {
        caricaMalattiaCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("-- Seleziona certificato --");
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else if (value instanceof PeriodoMalattia pm) {
                    setFont(getFont().deriveFont(Font.PLAIN));
                    setText(formatPeriodoMalattia(pm));
                }
                return this;
            }
        });
        refreshCaricaMalattiaCombo();
    }

    private void refreshCaricaMalattiaCombo() {
        caricaMalattiaCombo.removeAllItems();
        caricaMalattiaCombo.addItem(null);
        controller.getPeriodiMalattia().forEach(caricaMalattiaCombo::addItem);
    }

    private String formatPeriodoMalattia(PeriodoMalattia pm) {
        String medico = controller.getMedici().stream()
                .filter(m -> m.getIdMedico() == pm.getIdMedico())
                .findFirst()
                .map(MalattiaPanel::formatMedico)
                .orElse("Medico " + pm.getIdMedico());
        return pm.getCodiceCertificato() + " — " + medico
                + " (" + pm.getDataInizioMalattia().format(DATE_FMT)
                + " – " + pm.getDataFineMalattia().format(DATE_FMT) + ")";
    }

    private void buildUI() {
        IntFunction<List<Medico>> candidatiTurno = row ->
                row < turniScopertiList.size() ? turniScopertiList.get(row).sostitutiCandidati() : List.of();
        IntFunction<List<Medico>> candidatiPrest = row ->
                row < prestazioniScoperteList.size() ? prestazioniScoperteList.get(row).sostitutiCandidati() : List.of();

        installSostitutoColumn(turnoTable, TURNO_COL_SOST, candidatiTurno);
        installSostitutoColumn(prestazioneTable, PREST_COL_SOST, candidatiPrest);

        turnoModel.addTableModelListener(e -> aggiornaStatoApplica());
        prestazioneModel.addTableModelListener(e -> aggiornaStatoApplica());

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildCentroPanel(), BorderLayout.CENTER);
        add(buildSudPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registra Periodo di Malattia"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        GuiUtils.addFormRow(formPanel, gbc, 0, "Medico:",             medicoCombo);
        GuiUtils.addFormRow(formPanel, gbc, 1, "Codice Certificato:", codiceField);
        GuiUtils.addFormRow(formPanel, gbc, 2, "Inizio malattia:",    inizioField);
        GuiUtils.addFormRow(formPanel, gbc, 3, "Fine malattia:",      fineField);

        JButton registraBtn = new JButton("Registra Malattia e Cerca Coperture");
        registraBtn.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));
        registraBtn.addActionListener(e -> handleRegistra());
        JButton aggiornaBtn = new JButton("Aggiorna Date Periodo Corrente");
        aggiornaBtn.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));
        aggiornaBtn.addActionListener(e -> handleAggiornaPeriodo());

        JPanel bottoniPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottoniPanel.add(registraBtn);
        bottoniPanel.add(aggiornaBtn);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(bottoniPanel, gbc);

        GuiUtils.addFormRow(formPanel, gbc, 5, "Carica malattia esistente:", caricaMalattiaCombo);
        JButton caricaBtn = new JButton("Carica Coperture");
        caricaBtn.addActionListener(e -> handleCarica());
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        formPanel.add(caricaBtn, gbc);
        return formPanel;
    }

    private JComponent buildCentroPanel() {
        turniHeader.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));
        prestazioniHeader.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));

        JPanel turniPanel = new JPanel(new BorderLayout(0, 3));
        turniPanel.add(turniHeader, BorderLayout.NORTH);
        turniPanel.add(new JScrollPane(turnoTable), BorderLayout.CENTER);

        JPanel prestazioniPanel = new JPanel(new BorderLayout(0, 3));
        prestazioniPanel.add(prestazioniHeader, BorderLayout.NORTH);
        prestazioniPanel.add(new JScrollPane(prestazioneTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, turniPanel, prestazioniPanel);
        split.setResizeWeight(0.5);
        return split;
    }

    private JPanel buildSudPanel() {
        JPanel sud = new JPanel(new FlowLayout(FlowLayout.LEFT));
        applicaBtn.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));
        applicaBtn.setToolTipText("Salva le riassegnazioni selezionate nei menu a tendina e aggiorna i panel turni");
        applicaBtn.setEnabled(false);
        applicaBtn.addActionListener(e -> handleApplica());
        sud.add(applicaBtn);
        return sud;
    }

    private void installSostitutoColumn(JTable table, int col, IntFunction<List<Medico>> candidati) {
        table.getColumnModel().getColumn(col).setCellRenderer(new SostitutoCellRenderer(candidati));
        table.getColumnModel().getColumn(col).setCellEditor(new SostitutoCellEditor(candidati));
    }

    private void handleRegistra() {
        Medico medico = (Medico) medicoCombo.getSelectedItem();
        if (medico == null) { showError(this, "Selezionare un medico."); return; }

        LocalDate inizio = inizioField.getLocalDate();
        LocalDate fine   = fineField.getLocalDate();
        if (inizio == null || fine == null) {
            showError(this, "Selezionare le date di inizio e fine malattia.");
            return;
        }

        String codice = codiceField.getText().trim();
        try {
            controller.registraPeriodoMalattia(codice, medico.getIdMedico(), inizio, fine);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
            return;
        }

        codiceCorrente  = codice;
        idMedicoAssente = medico.getIdMedico();
        periodoInizio   = inizio;
        periodoFine     = fine;

        boolean vuoto;
        try {
            vuoto = caricaScoperti();
        } catch (ValidationException ex) {
            showError(this, ex.getMessage());
            return;
        }
        clearFields(codiceField);
        clearDatePickers(inizioField, fineField);
        refreshCaricaMalattiaCombo();
        refreshTurniPanels.run();
        if (vuoto)
            showInfo(this, "Malattia registrata. Nessun turno o prestazione da riassegnare nel periodo indicato.");
    }

    private void handleCarica() {
        PeriodoMalattia pm = (PeriodoMalattia) caricaMalattiaCombo.getSelectedItem();
        if (pm == null) {
            showError(this, "Selezionare un certificato di malattia.");
            return;
        }

        codiceCorrente  = pm.getCodiceCertificato();
        idMedicoAssente = pm.getIdMedico();
        periodoInizio   = pm.getDataInizioMalattia();
        periodoFine     = pm.getDataFineMalattia();
        inizioField.setLocalDate(periodoInizio);
        fineField.setLocalDate(periodoFine);
        selezionaMedicoCombo(idMedicoAssente);

        boolean vuoto;
        try {
            vuoto = caricaScoperti();
        } catch (ValidationException ex) {
            showError(this, ex.getMessage());
            return;
        }
        caricaMalattiaCombo.setSelectedIndex(0);
        refreshTurniPanels.run();
        if (vuoto)
            showInfo(this, "Malattia caricata. Nessun turno o prestazione da riassegnare nel periodo indicato.");
    }

    private void handleAggiornaPeriodo() {
        if (codiceCorrente == null) {
            showError(this, "Registrare o caricare prima una malattia da aggiornare.");
            return;
        }

        LocalDate inizio = inizioField.getLocalDate();
        LocalDate fine   = fineField.getLocalDate();
        if (inizio == null || fine == null) {
            showError(this, "Selezionare le date di inizio e fine malattia.");
            return;
        }

        try {
            controller.aggiornaPeriodoMalattia(codiceCorrente, inizio, fine);
        } catch (ValidationException ex) {
            showError(this, ex.getMessage());
            return;
        }

        periodoInizio = inizio;
        periodoFine   = fine;
        refreshCaricaMalattiaCombo();
        try {
            caricaScoperti();
            refreshTurniPanels.run();
            showInfo(this, "Periodo di malattia aggiornato.");
        } catch (ValidationException ex) {
            showError(this, ex.getMessage());
        }
    }

    private void selezionaMedicoCombo(int idMedico) {
        for (int i = 0; i < medicoCombo.getItemCount(); i++) {
            Medico m = medicoCombo.getItemAt(i);
            if (m.getIdMedico() == idMedico) {
                medicoCombo.setSelectedItem(m);
                return;
            }
        }
    }

    private boolean caricaScoperti() throws ValidationException {
        turniScopertiList       = controller.turniScoperti(idMedicoAssente, periodoInizio, periodoFine);
        prestazioniScoperteList = controller.prestazioniScoperte(idMedicoAssente, periodoInizio, periodoFine);
        popolaTurni();
        popolaPrestazioni();
        aggiornaStatoApplica();
        return turniScopertiList.isEmpty() && prestazioniScoperteList.isEmpty();
    }

    private void popolaTurni() {
        turnoModel.setRowCount(0);
        for (TurnoScoperto t : turniScopertiList) {
            turnoModel.addRow(new Object[]{
                t.data().format(DATE_FMT),
                t.fasciaOraria().name(),
                t.oraInizio().format(TIME_FMT) + "–" + t.oraFine().format(TIME_FMT),
                NON_RIASSEGNARE
            });
        }
        turniHeader.setText("Turni scoperti (" + turniScopertiList.size() + "):");
    }

    private void popolaPrestazioni() {
        prestazioneModel.setRowCount(0);
        for (PrestazioneScoperta p : prestazioniScoperteList) {
            prestazioneModel.addRow(new Object[]{
                p.numeroPratica(),
                p.numeroPrestazione(),
                p.dataInizioPrestazione().format(DATETIME_FMT),
                p.tipologia().name(),
                NON_RIASSEGNARE
            });
        }
        prestazioniHeader.setText("Prestazioni scoperte (" + prestazioniScoperteList.size() + "):");
    }

    private void handleApplica() {
        stopEditing(turnoTable);
        stopEditing(prestazioneTable);

        List<RiassegnazioneTurno> turni = new ArrayList<>();
        for (int i = 0; i < turnoModel.getRowCount(); i++) {
            if (turnoModel.getValueAt(i, TURNO_COL_SOST) instanceof Medico m) {
                TurnoScoperto t = turniScopertiList.get(i);
                turni.add(new RiassegnazioneTurno(idMedicoAssente, m.getIdMedico(), t.data(), t.fasciaOraria()));
            }
        }
        List<RiassegnazionePrestazione> prestazioni = new ArrayList<>();
        for (int i = 0; i < prestazioneModel.getRowCount(); i++) {
            if (prestazioneModel.getValueAt(i, PREST_COL_SOST) instanceof Medico m) {
                PrestazioneScoperta p = prestazioniScoperteList.get(i);
                prestazioni.add(new RiassegnazionePrestazione(p.numeroPratica(), p.numeroPrestazione(), m.getIdMedico()));
            }
        }

        if (turni.isEmpty() && prestazioni.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessuna riassegnazione selezionata.",
                    ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            controller.applicaRiassegnazioni(turni, prestazioni);
            showInfo(this, "Riassegnazioni applicate con successo.");
            caricaScoperti();
            refreshTurniPanels.run();
        } catch (ValidationException ex) {
            showError(this, "Errore. Nessuna riassegnazione applicata. " + ex.getMessage());
        }
    }

    private void aggiornaStatoApplica() {
        applicaBtn.setEnabled(haScelte(turnoModel, TURNO_COL_SOST)
                || haScelte(prestazioneModel, PREST_COL_SOST)
                || haSceltaInEditing(turnoTable)
                || haSceltaInEditing(prestazioneTable));
    }

    private boolean haSceltaInEditing(JTable table) {
        if (!table.isEditing()) return false;
        TableCellEditor editor = table.getCellEditor();
        if (editor == null) return false;
        Object value = editor.getCellEditorValue();
        return value instanceof Medico;
    }

    private boolean haScelte(DefaultTableModel model, int col) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, col) instanceof Medico) return true;
        }
        return false;
    }

    private void stopEditing(JTable table) {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
    }

    private JComboBox<Medico> buildMedicoCombo() {
        JComboBox<Medico> cb = new JComboBox<>();
        controller.getMedici().forEach(cb::addItem);
        return cb;
    }

    private static String formatMedico(Medico m) {
        return m.getCognome() + " " + m.getNome();
    }

    private class SostitutoCellRenderer extends DefaultTableCellRenderer {
        private final transient IntFunction<List<Medico>> candidatiPerRiga;

        SostitutoCellRenderer(IntFunction<List<Medico>> candidatiPerRiga) {
            this.candidatiPerRiga = candidatiPerRiga;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            boolean vuoto = candidatiPerRiga.apply(row).isEmpty();
            String text;
            if (vuoto) text = NESSUNO;
            else if (value instanceof Medico m) text = formatMedico(m);
            else text = value == null ? "" : value.toString();

            Component c = super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
            c.setFont(c.getFont().deriveFont(vuoto ? Font.ITALIC : Font.PLAIN));
            if (!isSelected) c.setForeground(vuoto ? Color.GRAY : table.getForeground());
            return c;
        }
    }

    private class SostitutoCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JComboBox<Object> comboBox = new JComboBox<>();
        private final transient IntFunction<List<Medico>> candidatiPerRiga;

        SostitutoCellEditor(IntFunction<List<Medico>> candidatiPerRiga) {
            this.candidatiPerRiga = candidatiPerRiga;
            comboBox.addPopupMenuListener(new PopupMenuListener() {
                @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    // no-op: nothing to commit when the popup opens, only on close/selection
                }

                @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    commitEditing();
                }

                @Override public void popupMenuCanceled(PopupMenuEvent e) {
                    commitEditing();
                }
            });
            comboBox.addActionListener(e -> {
                if (!comboBox.isPopupVisible()) commitEditing();
            });
            comboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Medico m) setText(formatMedico(m));
                    return this;
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            comboBox.removeAllItems();
            comboBox.addItem(NON_RIASSEGNARE);
            List<Medico> candidati = candidatiPerRiga.apply(row);
            for (Medico m : candidati) comboBox.addItem(m);
            comboBox.setEnabled(!candidati.isEmpty());
            comboBox.setSelectedItem(value instanceof Medico ? value : NON_RIASSEGNARE);
            return comboBox;
        }

        @Override
        public Object getCellEditorValue() {
            return comboBox.getSelectedItem();
        }

        private void commitEditing() {
            fireEditingStopped();
            aggiornaStatoApplica();
        }
    }
}
