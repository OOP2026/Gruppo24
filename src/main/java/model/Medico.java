package model;

import java.util.ArrayList;
import java.util.List;
import utils.Validators;

public class Medico extends Utente {

    private String nome;
    private String cognome;
    private final String matricola;
    private Reparto reparto;

    private final List<Turno> turniProgrammati;
    private final List<Prestazione> prestazioniErogate;
    private final List<Malattia> periodiMalattia;

    public Medico(String login, String password, String nome, String cognome, String matricola, Reparto reparto) {

        super(login, password);

        Validators.validaStringa(nome, "Nome Medico");
        Validators.validaStringa(cognome, "Cognome Medico");
        Validators.validaStringa(matricola, "Matricola");
        Validators.validaOggetto(reparto, "Reparto di afferenza");

        this.nome = nome;
        this.cognome = cognome;
        this.matricola = matricola;
        this.reparto = reparto;

        this.turniProgrammati = new ArrayList<>();
        this.prestazioniErogate = new ArrayList<>();
        this.periodiMalattia = new ArrayList<>();
    }

    public void aggiungiTurno(Turno turno) {
        Validators.validaOggetto(turno, "Turno");
        this.turniProgrammati.add(turno);
    }

    public void aggiungiPrestazione(Prestazione prestazione) {
        Validators.validaOggetto(prestazione, "Prestazione");
        this.prestazioniErogate.add(prestazione);
    }

    public void aggiungiMalattia(Malattia malattia) {
        Validators.validaOggetto(malattia, "Malattia");
        this.periodiMalattia.add(malattia);
    }

    public String getNome() { return nome; }

    public void setNome(String nome) {
        Validators.validaStringa(nome, "Nome");
        this.nome = nome;
    }

    public String getCognome() { return cognome; }

    public void setCognome(String cognome) {

        Validators.validaStringa(cognome, "Cognome");

        this.cognome = cognome;

    }

    public String getMatricola() { return matricola; }

    public Reparto getReparto() { return reparto; }

    public void setReparto(Reparto reparto) {

        Validators.validaOggetto(reparto, "Reparto");

        this.reparto = reparto;

    }

    public List<Turno> getTurniProgrammati() { return turniProgrammati; }
    public List<Prestazione> getPrestazioniErogate() { return prestazioniErogate; }
    public List<Malattia> getPeriodiMalattia() { return periodiMalattia; }
}