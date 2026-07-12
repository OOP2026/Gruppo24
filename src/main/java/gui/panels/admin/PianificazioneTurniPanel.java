package gui.panels.admin;

import controller.Controller;
import exceptions.ValidationException;
import gui.utils.GuiUtils;
import model.FasciaOraria;
import model.Turno;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static gui.utils.GuiUtils.*;
import static utils.DateFormats.*;
import static utils.Messages.*;
import static utils.TimeZones.EUROPE_ROME;

public class PianificazioneTurniPanel extends JPanel {

    private static final String FONT_SANS_SERIF = "SansSerif";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);

    private static final String NON_ASSEGNATO       = "NON ASSEGNATO";
    private static final int    COL_MEDICO          = 4;
    private static final Color  BG_TURNO_SCOPERTO   = new Color(255, 230, 230);
    private static final Color  FG_TURNO_SCOPERTO   = new Color(153, 0, 0);

    private final transient Controller controller;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);
    private final JTextField dataField;
    private final JComboBox<FasciaOraria> fasciaCombo = new JComboBox<>(FasciaOraria.values());
    private final JSpinner spinnerInizio;
    private final JSpinner spinnerFine;
    private final JTextField dalField;
    private final JTextField alField;

    public PianificazioneTurniPanel(Controller controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;

        String[] cols = {"Data", "Fascia", "Ora Inizio", "Ora Fine", "Medico Assegnato"};
        this.tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        this.table = new JTable(tableModel);
        installRigaScopertaRenderer();
        LocalDate oggi = LocalDate.now(ZoneId.of(EUROPE_ROME));
        this.dataField    = new JTextField(oggi.format(DATE_FMT), 10);
        this.dataField.setToolTipText(DATE_FORMAT_PATTERN);
        this.spinnerInizio = buildTimeSpinner();
        this.spinnerFine   = buildTimeSpinner();
        LocalDate dal = oggi.with(DayOfWeek.MONDAY);
        LocalDate al  = dal.plusWeeks(3).minusDays(1);
        this.dalField = new JTextField(dal.format(DATE_FMT), 10);
        this.alField  = new JTextField(al.format(DATE_FMT), 10);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        aggiornaSpin(spinnerInizio, spinnerFine, (FasciaOraria) fasciaCombo.getSelectedItem());
        fasciaCombo.addActionListener(e ->
                aggiornaSpin(spinnerInizio, spinnerFine, (FasciaOraria) fasciaCombo.getSelectedItem()));
        refreshTabella();
        add(buildNordPanel(), BorderLayout.NORTH);
        add(buildCentroPanel(), BorderLayout.CENTER);
    }

    private JPanel buildNordPanel() {
        JPanel wrap = new JPanel(new BorderLayout(0, 5));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Inserisci Nuovo Turno"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        GuiUtils.addFormRow(form, gbc, 0, "Data (" + DATE_FORMAT_PATTERN + "):", dataField);
        GuiUtils.addFormRow(form, gbc, 1, "Fascia oraria:", fasciaCombo);
        GuiUtils.addFormRow(form, gbc, 2, "Ora inizio:", spinnerInizio);
        GuiUtils.addFormRow(form, gbc, 3, "Ora fine:", spinnerFine);
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
        if (fascia == null) { showError(this, "Selezionare una fascia oraria."); return; }
        try {
            LocalDate data      = LocalDate.parse(dataField.getText().trim(), DATE_FMT);
            LocalTime oraInizio = spinnerToLocalTime(spinnerInizio);
            LocalTime oraFine   = spinnerToLocalTime(spinnerFine);
            controller.pianificaTurno(data, fascia, oraInizio, oraFine);
            refreshTabella();
            showInfo(this, "Turno pianificato correttamente.");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    DATE_FORMAT_MSG_USE + DATE_FORMAT_PATTERN,
                    ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleModifica() {
        int row = table.getSelectedRow();
        if (row < 0) { showError(this, "Selezionare un turno dalla tabella."); return; }
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

            int res = JOptionPane.showConfirmDialog(this, dlg,
                    "Modifica Orari – " + tableModel.getValueAt(row, 0) + " " + fascia,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;

            controller.modificaTurnoPianificato(data, fascia,
                    spinnerToLocalTime(newInizio), spinnerToLocalTime(newFine));
            refreshTabella();
            showInfo(this, "Orari aggiornati.");
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void handleElimina() {
        int row = table.getSelectedRow();
        if (row < 0) { showError(this, "Selezionare un turno dalla tabella."); return; }
        String dataStr   = (String) tableModel.getValueAt(row, 0);
        String fasciaStr = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminare il turno del " + dataStr + " fascia " + fasciaStr
                        + "?\nSaranno eliminate anche le assegnazioni collegate.",
                "Conferma eliminazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            controller.eliminaTurnoPianificato(
                    LocalDate.parse(dataStr, DATE_FMT), FasciaOraria.valueOf(fasciaStr));
            refreshTabella();
            showInfo(this, "Turno eliminato.");
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            showError(this, ex.getMessage());
        }
    }

    private void refreshTabella() {
        try {
            LocalDate dal = LocalDate.parse(dalField.getText().trim(), DATE_FMT);
            LocalDate al  = LocalDate.parse(alField.getText().trim(), DATE_FMT);
            tableModel.setRowCount(0);
            for (Turno t : controller.getTurniPianificati(dal, al)) {
                String medicoAssegnato = t.isAssegnato()
                        ? t.getDescrizioneAssegnazione()
                        : NON_ASSEGNATO;
                tableModel.addRow(new Object[]{
                    t.getData().format(DATE_FMT),
                    t.getFasciaOraria().name(),
                    t.getOraInizio().format(timeFmt),
                    t.getOraFine().format(timeFmt),
                    medicoAssegnato
                });
            }
        } catch (DateTimeParseException ex) {
            // campi filtro non ancora validi — ignorato
        }
    }

    /**
     * Installa un renderer di riga che evidenzia in rosso tenue i turni ancora scoperti
     * (colonna "Medico Assegnato" = NON_ASSEGNATO), lasciando i colori standard di sistema
     * per i turni regolarmente coperti. Preserva l'evidenziazione della riga selezionata.
     */
    private void installRigaScopertaRenderer() {
        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected,
                        hasFocus, row, column);
                Object medicoCell = tbl.getModel().getValueAt(row, COL_MEDICO);
                boolean scoperto = NON_ASSEGNATO.equals(medicoCell);
                if (isSelected) {
                    c.setBackground(tbl.getSelectionBackground());
                    c.setForeground(tbl.getSelectionForeground());
                } else if (scoperto) {
                    c.setBackground(BG_TURNO_SCOPERTO);
                    c.setForeground(FG_TURNO_SCOPERTO);
                } else {
                    c.setBackground(tbl.getBackground());
                    c.setForeground(tbl.getForeground());
                }
                if (c instanceof JLabel label && column == COL_MEDICO) {
                    label.setFont(label.getFont().deriveFont(scoperto ? Font.BOLD : Font.PLAIN));
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
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
}
