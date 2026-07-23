package gui.panels.medico;

import controller.Controller;
import gui.components.DateTimePickerField;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static gui.utils.GuiUtils.addFormRow;
import static utils.DateFormats.*;
import static utils.TimeZones.EUROPE_ROME;

public class NuovaPrestazionePanel extends JPanel {

    private final transient Controller controller;
    private final transient Medico medicoCorrente;
    private final JComboBox<Ricovero> ricoveroCombo;

    public NuovaPrestazionePanel(Controller controller, Medico medicoCorrente) {
        super(new BorderLayout(5, 5));
        this.controller     = controller;
        this.medicoCorrente = medicoCorrente;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.ricoveroCombo = buildRicoveroCombo();
        buildUI();
    }

    private void buildUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registra Nuova Prestazione"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        DateTimePickerField inizioField = new DateTimePickerField();
        DateTimePickerField fineField   = new DateTimePickerField();
        JComboBox<TipoPrestazione> tipoCombo = new JComboBox<>(TipoPrestazione.values());
        JLabel malattiaWarning = new JLabel(" ", SwingConstants.CENTER);
        malattiaWarning.setForeground(new Color(192, 57, 43));

        addFormRow(formPanel, gbc, 0, "Ricovero:", ricoveroCombo);
        addFormRow(formPanel, gbc, 1, "Inizio:",   inizioField);
        addFormRow(formPanel, gbc, 2, "Fine:",     fineField);
        addFormRow(formPanel, gbc, 3, "Tipo:",     tipoCombo);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(malattiaWarning, gbc);

        JTextArea turniInfo = buildTurniInfo();
        gbc.gridy = 5;
        formPanel.add(turniInfo, gbc);

        JButton registraBtn = new JButton("Registra Prestazione");
        registraBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridy = 6;
        formPanel.add(registraBtn, gbc);

        JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        gbc.gridy = 7;
        formPanel.add(statusLabel, gbc);

        Runnable aggiornaAvvisoMalattia = () -> {
            LocalDateTime inizio = inizioField.getLocalDateTime();
            if (inizio != null && controller.isMedicoInMalattia(medicoCorrente.getIdMedico(), inizio.toLocalDate())) {
                malattiaWarning.setText("Attenzione: sei in malattia in questa data. Non puoi registrare nuove prestazioni.");
                registraBtn.setEnabled(false);
            } else {
                malattiaWarning.setText(" ");
                registraBtn.setEnabled(true);
            }
        };
        inizioField.addChangeListener(aggiornaAvvisoMalattia);

        registraBtn.addActionListener(e -> {
            try {
                Ricovero ricovero = (Ricovero) ricoveroCombo.getSelectedItem();
                if (ricovero == null) { statusLabel.setText("Selezionare un ricovero."); return; }
                LocalDateTime inizio = inizioField.getLocalDateTime();
                LocalDateTime fine   = fineField.getLocalDateTime();
                if (inizio == null || fine == null) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Selezionare data e ora di inizio e fine.");
                    return;
                }
                TipoPrestazione tipo = (TipoPrestazione) tipoCombo.getSelectedItem();

                // Nessun record, usi direttamente il Ricovero
                controller.registraPrestazione(ricovero.getNumeroPratica(), inizio, fine, tipo);

                statusLabel.setForeground(new Color(39, 174, 96));
                statusLabel.setText("Prestazione registrata con successo.");
                inizioField.clear();
                fineField.clear();
            } catch (Exception ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText(ex.getMessage());
            }
        });

        add(formPanel, BorderLayout.CENTER);
    }

    public void aggiornaRicoveri() {
        Object selezionato = ricoveroCombo.getSelectedItem();
        ricoveroCombo.removeAllItems();
        controller.getRicoveriInCorso().forEach(ricoveroCombo::addItem);
        if (selezionato != null) ricoveroCombo.setSelectedItem(selezionato);
    }

    private JComboBox<Ricovero> buildRicoveroCombo() {
        JComboBox<Ricovero> cb = new JComboBox<>();

        controller.getRicoveriInCorso().forEach(cb::addItem);

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object v,
                                                          int idx, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, v, idx, sel, focus);

                if (v instanceof Ricovero r) {
                    controller.getPazienteById(r.getIdPaziente()).ifPresentOrElse(
                            p -> setText(String.format("%s %s (%s) – Pratica: %s [Letto %s]",
                                    p.getNome(), p.getCognome(), p.getCodiceFiscale(),
                                    r.getNumeroPratica(), r.getCodiceUnivocoLetto())),
                            () -> setText(r.getNumeroPratica() + " – Letto " + r.getCodiceUnivocoLetto())
                    );
                }
                return this;
            }
        });
        return cb;
    }

    private JTextArea buildTurniInfo() {
        JTextArea turniInfo = new JTextArea(4, 30);
        turniInfo.setEditable(false);
        turniInfo.setFont(new Font("Monospaced", Font.PLAIN, 11));
        turniInfo.setBackground(new Color(245, 245, 245));
        turniInfo.setBorder(BorderFactory.createTitledBorder("I miei turni questa settimana"));

        LocalDate oggi   = LocalDate.now(ZoneId.of(EUROPE_ROME));
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
        return turniInfo;
    }
}