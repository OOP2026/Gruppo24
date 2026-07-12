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

        PianificazioneTurniPanel pianificazione = new PianificazioneTurniPanel(controller);
        AssegnazioneTurniPanel   assegnazione   = new AssegnazioneTurniPanel(controller);
        RicoveriPanel            ricoveri       = new RicoveriPanel(controller);

        tabs.addTab("Pazienti",             new PazientiPanel(controller));
        tabs.addTab("Ricoveri",             ricoveri);
        tabs.addTab("Malattia",             new MalattiaPanel(controller));
        tabs.addTab("Letti",                new LettiPanel(controller));
        tabs.addTab("Medici",               new MediciPanel(controller));
        tabs.addTab("Dimissioni",           new DimissioniPanel(controller));
        tabs.addTab("Pianificazione Turni", pianificazione);
        tabs.addTab("Assegnazione Turni",   assegnazione);

        tabs.addChangeListener(e -> {
            Component selezionato = tabs.getSelectedComponent();
            if (selezionato == pianificazione) pianificazione.refresh();
            else if (selezionato == assegnazione) assegnazione.refresh();
            else if (selezionato == ricoveri) ricoveri.refresh();
        });

        add(tabs, BorderLayout.CENTER);
    }
}
