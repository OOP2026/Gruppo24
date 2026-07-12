package gui.panels.admin;

import controller.Controller;
import gui.utils.GuiUtils;
import model.Medico;
import model.Sostituto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static gui.utils.GuiUtils.*;
import static javax.swing.SwingConstants.LEFT;
import static utils.DateFormats.*;
import static utils.Messages.DATE_FORMAT_MSG;

public class MalattiaPanel extends JPanel {

    private static final String FONT_SANS_SERIF = "SansSerif";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);

    private final transient Controller controller;

    public MalattiaPanel(Controller controller) {
        super(new BorderLayout(5, 10));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registra Periodo di Malattia"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<Medico> medicoCombo   = buildMedicoCombo();
        JTextField        codiceField   = new JTextField(15);
        JTextField        inizioField   = new JTextField(DATE_FORMAT_PATTERN, 10);
        JTextField        fineField     = new JTextField(DATE_FORMAT_PATTERN, 10);

        GuiUtils.addFormRow(formPanel, gbc, 0, "Medico:",              medicoCombo);
        GuiUtils.addFormRow(formPanel, gbc, 1, "Codice Certificato:",  codiceField);
        GuiUtils.addFormRow(formPanel, gbc, 2, "Inizio malattia:",     inizioField);
        GuiUtils.addFormRow(formPanel, gbc, 3, "Fine malattia:",       fineField);

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
            if (medico == null) { showError(this, "Selezionare un medico."); return; }
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
                showInfo(this, "Periodo di malattia registrato per Dr. "
                        + medico.getNome() + " " + medico.getCognome() + ".");
            } catch (DateTimeParseException ex) {
                showError(this, DATE_FORMAT_MSG + " " + DATE_FORMAT_PATTERN);
            } catch (Exception ex) {
                showError(this, ex.getMessage());
            }
        });

        JPanel risultatiPanel = new JPanel(new BorderLayout(5, 5));
        risultatiPanel.add(sostitutoHeader,              BorderLayout.NORTH);
        risultatiPanel.add(new JScrollPane(sostitutoTable), BorderLayout.CENTER);

        add(formPanel,      BorderLayout.NORTH);
        add(risultatiPanel, BorderLayout.CENTER);
    }

    private JComboBox<Medico> buildMedicoCombo() {
        JComboBox<Medico> cb = new JComboBox<>();
        controller.getMedici().forEach(cb::addItem);
        return cb;
    }
}
