package gui.panels.medico;

import controller.Controller;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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

        JTextField inizioField = new JTextField("dd/MM/yyyy HH:mm", 16);
        JTextField fineField   = new JTextField("dd/MM/yyyy HH:mm", 16);
        JComboBox<TipoPrestazione> tipoCombo = new JComboBox<>(TipoPrestazione.values());

        addFormRow(formPanel, gbc, 0, "Ricovero:", ricoveroCombo);
        addFormRow(formPanel, gbc, 1, "Inizio:",   inizioField);
        addFormRow(formPanel, gbc, 2, "Fine:",     fineField);
        addFormRow(formPanel, gbc, 3, "Tipo:",     tipoCombo);

        JTextArea turniInfo = buildTurniInfo();
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

                // Nessun record, usi direttamente il Ricovero
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