package model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import utils.Validators; 

public class Turno {
    
    private final long idTurno; 
    private final DayOfWeek giornoDellaSettimana;
    private final LocalTime oraInizio;
    private final LocalTime oraFine;
   

    public Turno(long idTurno, DayOfWeek giornoDellaSettimana, LocalTime oraInizio, LocalTime oraFine) {

        Validators.validaOggetto(giornoDellaSettimana, "Giorno della Settimana");
        Validators.validaIntervalloTempo(oraInizio, oraFine);
        this.idTurno = idTurno;
        this.giornoDellaSettimana = giornoDellaSettimana;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
         
    }

    public DayOfWeek getGiornoDellaSettimana() { return giornoDellaSettimana; }
    public LocalTime getOraInizio() { return oraInizio; }
    public LocalTime getOraFine() { return oraFine; }
    public long getIdTurno() { return idTurno; }
}