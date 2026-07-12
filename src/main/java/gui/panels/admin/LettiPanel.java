package gui.panels.admin;

import controller.Controller;
import model.Letto;
import model.Reparto;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static javax.swing.SwingConstants.CENTER;

public class LettiPanel extends JPanel {

    private static final String FONT_SANS_SERIF = "SansSerif";

    private final transient Controller controller;

    public LettiPanel(Controller controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
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
                JLabel badge = createLettoBadge(l, occupati);
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

        add(top,    BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JComboBox<Reparto> buildRepartoCombo() {
        JComboBox<Reparto> cb = new JComboBox<>();
        controller.getReparti().forEach(cb::addItem);
        return cb;
    }

    private static JLabel createLettoBadge(Letto l, Set<String> occupati) {
        boolean isOccupato = occupati.contains(l.getCodiceUnivoco());
        JLabel badge = new JLabel(l.getCodiceUnivoco(), CENTER);
        badge.setOpaque(true);
        badge.setPreferredSize(new Dimension(130, 50));
        badge.setFont(new Font(FONT_SANS_SERIF, Font.BOLD, 12));
        badge.setBackground(isOccupato ? Color.RED : new Color(39, 174, 96));
        badge.setForeground(Color.WHITE);
        badge.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        badge.setToolTipText(isOccupato ? "OCCUPATO" : "DISPONIBILE");
        return badge;
    }
}
