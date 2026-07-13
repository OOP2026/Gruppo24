package gui.panels;

import controller.Controller;
import gui.panels.medico.AgendaGiornalieraPanel;
import gui.panels.medico.AgendaSettimanalePanel;
import gui.panels.medico.ModificaEsitiPanel;
import gui.panels.medico.NuovaPrestazionePanel;
import model.Medico;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.SwingConstants.LEFT;

public class MedicoPanel extends JPanel {

    public MedicoPanel(Controller controller) {
        Medico medicoCorrente = controller.getMedicoCorrente();
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane(LEFT);

        AgendaGiornalieraPanel agendaG  = new AgendaGiornalieraPanel(controller, medicoCorrente);
        AgendaSettimanalePanel agendaS  = new AgendaSettimanalePanel(controller, medicoCorrente);
        NuovaPrestazionePanel  nuovaPr  = new NuovaPrestazionePanel(controller, medicoCorrente);
        ModificaEsitiPanel     modEsiti = new ModificaEsitiPanel(controller, medicoCorrente);

        tabs.addTab("Agenda Giornaliera", agendaG);
        tabs.addTab("Agenda Settimanale", agendaS);
        tabs.addTab("Nuova Prestazione",  nuovaPr);
        tabs.addTab("Modifica Esiti",     modEsiti);

        List<Runnable> tabRefreshers = new ArrayList<>();
        tabRefreshers.add(agendaG::aggiorna);
        tabRefreshers.add(agendaS::aggiorna);
        tabRefreshers.add(nuovaPr::aggiornaRicoveri);
        tabRefreshers.add(modEsiti::aggiorna);

        tabs.addChangeListener(e -> {
            int i = tabs.getSelectedIndex();
            if (i >= 0 && i < tabRefreshers.size()) tabRefreshers.get(i).run();
        });

        add(tabs, BorderLayout.CENTER);
    }
}
