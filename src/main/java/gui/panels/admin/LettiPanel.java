package gui.panels.admin;

import controller.Controller;
import model.Letto;
import model.Reparto;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static javax.swing.SwingConstants.CENTER;

public class LettiPanel extends JPanel implements RefreshablePanel {

    private static final String FONT_SANS_SERIF = "SansSerif";

    private final transient Controller controller;
    private final JComboBox<Reparto> repartoCombo;
    private final JPanel lettiGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

    public LettiPanel(Controller controller) {
        super(new BorderLayout(5, 5));
        this.controller = controller;
        this.repartoCombo = buildRepartoCombo();
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        JScrollPane scroll = new JScrollPane(lettiGrid);

        repartoCombo.addActionListener(e -> aggiorna());
        if (repartoCombo.getItemCount() > 0) {
            repartoCombo.setSelectedIndex(0);
            aggiorna();
        }

        JButton refreshBtn = new JButton("Aggiorna");
        refreshBtn.addActionListener(e -> aggiorna());

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

    private void aggiorna() {
        lettiGrid.removeAll();
        Reparto rep = (Reparto) repartoCombo.getSelectedItem();
        if (rep != null) {
            Set<String> occupati = new HashSet<>();
            controller.getLettiOccupati().forEach(l -> occupati.add(l.getCodiceUnivoco()));
            for (Letto l : controller.getLettiPerReparto(rep.getIdReparto())) {
                lettiGrid.add(createLettoBadge(l, occupati));
            }
        }
        lettiGrid.revalidate();
        lettiGrid.repaint();
    }

    @Override
    public void refresh() {
        aggiorna();
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
