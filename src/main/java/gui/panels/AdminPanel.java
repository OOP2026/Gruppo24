package gui.panels;

import controller.Controller;
import gui.panels.admin.*;

import javax.swing.*;
import java.awt.*;

import static javax.swing.SwingConstants.LEFT;

public class AdminPanel extends JPanel {

    public AdminPanel(Controller controller) {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane(LEFT);

        tabs.addTab("Pazienti",             new PazientiPanel(controller));
        tabs.addTab("Ricoveri",             new RicoveriPanel(controller));
        tabs.addTab("Malattia",             new MalattiaPanel(controller));
        tabs.addTab("Letti",                new LettiPanel(controller));
        tabs.addTab("Medici",               new MediciPanel(controller));
        tabs.addTab("Dimissioni",           new DimissioniPanel(controller));
        tabs.addTab("Pianificazione Turni", new PianificazioneTurniPanel(controller));
        tabs.addTab("Assegnazione Turni",   new AssegnazioneTurniPanel(controller));

        tabs.addChangeListener(e -> {
            Component selezionato = tabs.getSelectedComponent();
            if (selezionato instanceof RefreshablePanel refreshable) refreshable.refresh();
        });

        add(tabs, BorderLayout.CENTER);
    }
}
