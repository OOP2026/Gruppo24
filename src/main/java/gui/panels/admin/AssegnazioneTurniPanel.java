package gui.panels.admin;

import controller.Controller;
import exceptions.ValidationException;
import gui.components.DatePickerField;
import model.FasciaOraria;
import model.Medico;
import model.Turno;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static gui.utils.GuiUtils.showError;
import static gui.utils.GuiUtils.showInfo;
import static utils.DateFormats.*;
import static utils.Messages.*;
import static utils.TimeZones.EUROPE_ROME;

public class AssegnazioneTurniPanel extends JPanel implements RefreshablePanel {

    private static final String FONT_SANS_SERIF = "SansSerif";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);

    private static final String TUTTE_LE_FASCE = "Tutte";
    private static final String NESSUN_MEDICO  = "(nessuno)";
    private static final String MARCATORE_CORRENTE = "▶ ";
    private static final Color  BG_RIGA_CORRENTE   = new Color(255, 255, 200);
    private static final Color  BG_TURNO_SCOPERTO  = new Color(255, 230, 230);
    private static final Color  FG_TURNO_SCOPERTO  = new Color(153, 0, 0);
    private static final int    COL_MEDICI         = 4;
    private static final int    GIORNI_DEFAULT     = 14;

    private final transient Controller controller;
    private final transient Map<Integer, String> repartiMap = new HashMap<>();
    private final transient List<Turno> turniCorrenti = new ArrayList<>();

    private final JComboBox<Medico> medicoCombo = new JComboBox<>();
    private final JComboBox<String> fasciaFiltroCombo = new JComboBox<>();
    private final JCheckBox scopertiCheck = new JCheckBox("Solo turni scoperti");
    private final DatePickerField dalField;
    private final DatePickerField alField;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton assegnaBtn = new JButton("Assegna Turno Selezionato");
    private final JLabel emptyLabel = new JLabel(" ");

    public AssegnazioneTurniPanel(Controller controller) {
        super(new BorderLayout(5, 10));
        this.controller = controller;
        controller.getReparti().forEach(r -> repartiMap.put(r.getIdReparto(), r.getNomeReparto()));

        LocalDate oggi = LocalDate.now(ZoneId.of(EUROPE_ROME));
        LocalDate finoA = oggi.plusDays(GIORNI_DEFAULT);
        this.dalField = new DatePickerField(oggi);
        this.alField  = new DatePickerField(finoA);

        String[] cols = {"Data", "Fascia", "Inizio", "Fine", "Medici assegnati"};
        this.tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        this.table = creaTabella();

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private JTable creaTabella() {
        JTable t = new JTable(tableModel);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getSelectionModel().addListSelectionListener(e -> aggiornaStatoBottone());
        installRigaRenderer(t);
        return t;
    }

    private void installRigaRenderer(JTable t) {
        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected,
                        hasFocus, row, column);
                boolean scoperto = NESSUN_MEDICO.equals(tbl.getModel().getValueAt(row, COL_MEDICI));
                if (isSelected) {
                    c.setBackground(tbl.getSelectionBackground());
                    c.setForeground(tbl.getSelectionForeground());
                } else if (scoperto) {
                    c.setBackground(BG_TURNO_SCOPERTO);
                    c.setForeground(FG_TURNO_SCOPERTO);
                } else if (isRigaMedicoCorrente(row)) {
                    c.setBackground(BG_RIGA_CORRENTE);
                    c.setForeground(tbl.getForeground());
                } else {
                    c.setBackground(tbl.getBackground());
                    c.setForeground(tbl.getForeground());
                }
                if (c instanceof JLabel label && column == COL_MEDICI) {
                    label.setFont(label.getFont().deriveFont(scoperto ? Font.BOLD : Font.PLAIN));
                }
                return c;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void buildUI() {
        popolaMedicoCombo();
        popolaFasciaFiltroCombo();

        add(buildNordPanel(), BorderLayout.NORTH);
        add(buildCentroPanel(), BorderLayout.CENTER);
        add(buildSudPanel(), BorderLayout.SOUTH);

        medicoCombo.addActionListener(e -> refreshTabellaTurni());
        assegnaBtn.addActionListener(e -> handleAssegna());

        refreshTabellaTurni();
    }

    private void popolaMedicoCombo() {
        controller.getMedici().forEach(medicoCombo::addItem);
        medicoCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Medico m) {
                    String rep = repartiMap.getOrDefault(m.getIdReparto(), "Rep. " + m.getIdReparto());
                    setText(m.getCognome() + " " + m.getNome() + " — " + rep);
                }
                return this;
            }
        });
    }

    private void popolaFasciaFiltroCombo() {
        fasciaFiltroCombo.addItem(TUTTE_LE_FASCE);
        for (FasciaOraria f : FasciaOraria.values()) fasciaFiltroCombo.addItem(f.name());
    }

    private JPanel buildNordPanel() {
        JPanel wrap = new JPanel(new BorderLayout(0, 5));

        JPanel medicoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        medicoPanel.add(new JLabel("Medico da assegnare:"));
        medicoPanel.add(medicoCombo);

        JPanel filtriPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtriPanel.setBorder(BorderFactory.createTitledBorder("Filtri"));
        filtriPanel.add(new JLabel("Da:"));
        filtriPanel.add(dalField);
        filtriPanel.add(new JLabel("A:"));
        filtriPanel.add(alField);
        filtriPanel.add(new JLabel("Fascia:"));
        filtriPanel.add(fasciaFiltroCombo);
        filtriPanel.add(scopertiCheck);

        JButton applicaBtn = new JButton("Applica Filtri");
        JButton ripristinaBtn = new JButton("Ripristina");
        applicaBtn.addActionListener(e -> refreshTabellaTurni());
        ripristinaBtn.addActionListener(e -> ripristinaFiltri());
        filtriPanel.add(applicaBtn);
        filtriPanel.add(ripristinaBtn);

        wrap.add(medicoPanel, BorderLayout.NORTH);
        wrap.add(filtriPanel, BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel buildCentroPanel() {
        JPanel centro = new JPanel(new BorderLayout(5, 5));
        centro.setBorder(BorderFactory.createTitledBorder("Turni Pianificati"));
        emptyLabel.setForeground(Color.GRAY);
        centro.add(new JScrollPane(table), BorderLayout.CENTER);
        centro.add(emptyLabel, BorderLayout.SOUTH);
        return centro;
    }

    private JPanel buildSudPanel() {
        JPanel sud = new JPanel(new FlowLayout(FlowLayout.LEFT));
        assegnaBtn.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));
        assegnaBtn.setEnabled(false);
        sud.add(assegnaBtn);
        return sud;
    }

    private void ripristinaFiltri() {
        LocalDate oggi = LocalDate.now(ZoneId.of(EUROPE_ROME));
        LocalDate finoA = oggi.plusDays(GIORNI_DEFAULT);
        dalField.setLocalDate(oggi);
        alField.setLocalDate(finoA);
        fasciaFiltroCombo.setSelectedItem(TUTTE_LE_FASCE);
        scopertiCheck.setSelected(false);
        refreshTabellaTurni();
    }

    @Override
    public void refresh() {
        refreshTabellaTurni();
    }

    private void refreshTabellaTurni() {
        LocalDate dal = dalField.getLocalDate();
        LocalDate al  = alField.getLocalDate();
        if (dal == null || al == null) {
            showError(this, "Selezionare l'intervallo di date.");
            return;
        }

        Medico medico = (Medico) medicoCombo.getSelectedItem();

        List<Turno> turni;
        try {
            turni = controller.getTurniPianificatiConMedici(dal, al, fasciaFiltroSelezionata(), null);
        } catch (RuntimeException ex) {
            showError(this, ex.getMessage());
            return;
        }
        if (scopertiCheck.isSelected())
            turni.removeIf(Turno::isAssegnato);

        popolaTabella(turni, medico);
    }

    private void popolaTabella(List<Turno> turni, Medico corrente) {
        turniCorrenti.clear();
        turniCorrenti.addAll(turni);
        tableModel.setRowCount(0);
        for (Turno t : turni) {
            tableModel.addRow(new Object[]{
                t.getData().format(DATE_FMT),
                t.getFasciaOraria().name(),
                t.getOraInizio().format(TIME_FMT),
                t.getOraFine().format(TIME_FMT),
                formatMediciAssegnati(t.getMediciAssegnati(), corrente)
            });
        }
        boolean vuota = turni.isEmpty();
        emptyLabel.setText(vuota
                ? "Nessun turno trovato con i filtri applicati. Pianifica nuovi turni nel tab dedicato."
                : " ");
        aggiornaStatoBottone();
    }

    private String formatMediciAssegnati(List<Medico> medici, Medico corrente) {
        if (medici.isEmpty()) return NESSUN_MEDICO;
        return medici.stream()
                .map(m -> etichettaMedico(m, corrente))
                .collect(Collectors.joining(", "));
    }

    private String etichettaMedico(Medico m, Medico corrente) {
        String iniziale = m.getNome().isBlank() ? "" : " " + m.getNome().charAt(0) + ".";
        String base = m.getCognome() + iniziale;
        boolean isCorrente = corrente != null && m.getIdMedico() == corrente.getIdMedico();
        return isCorrente ? MARCATORE_CORRENTE + base : base;
    }

    private boolean isRigaMedicoCorrente(int row) {
        Medico corrente = (Medico) medicoCombo.getSelectedItem();
        if (corrente == null || row < 0 || row >= turniCorrenti.size()) return false;
        return turniCorrenti.get(row).getMediciAssegnati().stream()
                .anyMatch(m -> m.getIdMedico() == corrente.getIdMedico());
    }

    private void aggiornaStatoBottone() {
        int row = table.getSelectedRow();
        Medico medico = (Medico) medicoCombo.getSelectedItem();
        boolean assegnabile = row >= 0 && row < turniCorrenti.size()
                && medico != null && !isRigaMedicoCorrente(row);
        if (assegnabile) {
            Turno turno = turniCorrenti.get(row);
            assegnabile = !controller.isMedicoInMalattia(medico.getIdMedico(), turno.getData());
        }
        assegnaBtn.setEnabled(assegnabile);
    }

    private FasciaOraria fasciaFiltroSelezionata() {
        Object sel = fasciaFiltroCombo.getSelectedItem();
        if (sel == null || TUTTE_LE_FASCE.equals(sel)) return null;
        return FasciaOraria.valueOf((String) sel);
    }

    private void handleAssegna() {
        int row = table.getSelectedRow();
        Medico medico = (Medico) medicoCombo.getSelectedItem();
        if (row < 0 || row >= turniCorrenti.size() || medico == null) {
            showError(this, "Selezionare un turno dalla tabella e un medico.");
            return;
        }
        Turno turno = turniCorrenti.get(row);
        try {
            controller.assegnaTurnoAMedico(medico.getIdMedico(), turno.getData(), turno.getFasciaOraria());
            showInfo(this, "Turno assegnato correttamente.");
            refreshTabellaTurni();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            showError(this, ex.getMessage());
        }
    }
}
