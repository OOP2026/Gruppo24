package model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import utils.Validators; 

public class Turno {
    // Classe immutabile, creare un nuovo turno per apportare modifiche

    private final DayOfWeek giornoDellaSettimana;
    private final LocalTime oraInizio;
    private final LocalTime oraFine;
    private final StatoTurno statoTurno;

    public Turno(DayOfWeek giornoDellaSettimana, LocalTime oraInizio, LocalTime oraFine,StatoTurno statoTurno) {

        Validators.validaOggetto(giornoDellaSettimana, "Giorno della Settimana");
        Validators.validaIntervalloTempo(oraInizio, oraFine);

        this.giornoDellaSettimana = giornoDellaSettimana;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.statoTurno=statoTurno; 
    }

    public DayOfWeek getGiornoDellaSettimana() { return giornoDellaSettimana; }
    public LocalTime getOraInizio() { return oraInizio; }
    public LocalTime getOraFine() { return oraFine; }
    public LocalTime getStatoTurno() { return statoTurno; }
}