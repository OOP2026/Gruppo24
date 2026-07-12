package gui.panels.admin;

import controller.Controller;
import model.Medico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MediciPanel extends JPanel {

    private final transient Controller controller;

    public MediciPanel(Controller controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        String[] cols = {"Matricola", "Nome", "Cognome", "Login", "Reparto"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);

        Map<Integer, String> repartiMap = new HashMap<>();
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

        add(top,                    BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
