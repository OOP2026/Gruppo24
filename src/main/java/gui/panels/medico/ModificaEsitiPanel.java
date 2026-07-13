package gui.panels.medico;

import controller.Controller;
import model.Medico;
import model.Prestazione;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static utils.DateFormats.*;

public class ModificaEsitiPanel extends JPanel {

    private final transient Controller controller;
    private final transient Medico medicoCorrente;
    private final DefaultTableModel tableModel;
    private final transient AtomicReference<List<Prestazione>> cache = new AtomicReference<>(null);
    private final JTextArea esitoArea;

    public ModificaEsitiPanel(Controller controller, Medico medicoCorrente) {
        super(new BorderLayout(5, 5));
        this.controller     = controller;
        this.medicoCorrente = medicoCorrente;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Data", "Orario", "Tipo", "N. Pratica", "Esito attuale"};
        this.tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        this.esitoArea = new JTextArea(4, 40);
        buildUI();
        aggiorna();
    }

    private void buildUI() {
        JTable table = new JTable(tableModel);
        table.setRowHeight(22);

        esitoArea.setLineWrap(true);
        esitoArea.setWrapStyleWord(true);

        JButton salvaBtn    = new JButton("Salva Esito");
        JButton aggiornaBtn = new JButton("Aggiorna lista");
        salvaBtn.setFont(new Font("SansSerif", Font.BOLD, 12));

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            List<Prestazione> lista = cache.get();
            if (lista == null || row < 0 || row >= lista.size()) return;
            Prestazione p = lista.get(row);
            esitoArea.setText(p.getEsito() == null ? "" : p.getEsito());
        });

        salvaBtn.addActionListener(e -> handleSalvaEsito(table));
        aggiornaBtn.addActionListener(e -> aggiorna());

        JPanel editPanel = new JPanel(new BorderLayout(5, 5));
        editPanel.setBorder(BorderFactory.createTitledBorder("Compila / Modifica Esito"));
        JPanel bottomEdit = new JPanel(new BorderLayout());
        bottomEdit.add(new JScrollPane(esitoArea), BorderLayout.CENTER);
        bottomEdit.add(salvaBtn, BorderLayout.SOUTH);
        editPanel.add(bottomEdit, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(aggiornaBtn);

        add(btnPanel,               BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(editPanel,              BorderLayout.SOUTH);
    }

    public void aggiorna() {
        List<Prestazione> lista = controller.getPrestazioniMedico(medicoCorrente.getIdMedico());
        cache.set(lista);
        tableModel.setRowCount(0);
        for (Prestazione p : lista) {
            tableModel.addRow(new Object[]{
                p.getDataInizioPrestazione().toLocalDate().format(DATE_FMT),
                p.getDataInizioPrestazione().format(TIME_FMT) + "–" + p.getDataFinePrestazione().format(TIME_FMT),
                p.getTipologiaPrestazione(),
                p.getNumeroPratica(),
                (p.getEsito() == null || p.getEsito().isEmpty()) ? "(non compilato)" : p.getEsito()
            });
        }
    }

    private void handleSalvaEsito(JTable table) {
        int row = table.getSelectedRow();
        List<Prestazione> lista = cache.get();
        if (row < 0 || lista == null) {
            JOptionPane.showMessageDialog(this, "Selezionare una prestazione.", "Avviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nuovoEsito = esitoArea.getText().trim();
        if (nuovoEsito.isEmpty()) {
            JOptionPane.showMessageDialog(this, "L'esito non può essere vuoto.", "Avviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Prestazione p = lista.get(row);
            controller.aggiornaEsito(p.getNumeroPratica(), p.getNumeroPrestazione(), nuovoEsito);
            aggiorna();
            JOptionPane.showMessageDialog(this, "Esito salvato.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }
}
