package gui.panels.admin;

import controller.Controller;
import exceptions.ValidationException;
import gui.utils.GuiUtils;
import model.FasciaOraria;
import model.Medico;
import model.Turno;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static gui.utils.GuiUtils.showError;
import static utils.DateFormats.*;
import static utils.Messages.*;
import static utils.TimeZones.EUROPE_ROME;

public class AssegnazioneTurniPanel extends JPanel {

    private static final String FONT_SANS_SERIF = "SansSerif";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);

    private final transient Controller controller;

    public AssegnazioneTurniPanel(Controller controller) {
        super(new BorderLayout(5, 10));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
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
                if (value instanceof Medico m) {
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

        GuiUtils.addFormRow(formPanel, gbc, 0, "Medico:",                          medicoCombo);
        GuiUtils.addFormRow(formPanel, gbc, 1, "Data (" + DATE_FORMAT_PATTERN + "):", dataField);
        GuiUtils.addFormRow(formPanel, gbc, 2, "Fascia oraria:",                   fasciaCombo);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(orariLabel, gbc);

        gbc.gridy = 4;
        formPanel.add(assegnaBtn, gbc);

        assegnaBtn.addActionListener(e -> {
            Medico       medico = (Medico) medicoCombo.getSelectedItem();
            FasciaOraria fascia = (FasciaOraria) fasciaCombo.getSelectedItem();
            if (medico == null || fascia == null) {
                showError(this, "Selezionare medico e fascia oraria.");
                return;
            }
            try {
                LocalDate data = LocalDate.parse(dataField.getText().trim(), DATE_FMT);
                controller.assegnaTurnoAMedico(medico.getIdMedico(), data, fascia);
                JOptionPane.showMessageDialog(this, "Turno assegnato correttamente.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        DATE_FORMAT_MSG_USE + DATE_FORMAT_PATTERN,
                        ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        ATTENZIONE_MSG, JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                showError(this, ex.getMessage());
            }
        });

        add(formPanel, BorderLayout.NORTH);
    }
}
