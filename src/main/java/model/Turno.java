package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {

    private LocalDate data;
    private FasciaOraria fasciaOraria;
    private LocalTime oraInizio;
    private LocalTime oraFine;



    public Turno(LocalDate data, FasciaOraria fasciaOraria,
                 LocalTime oraInizio, LocalTime oraFine) {
        this.data = data;
        this.fasciaOraria = fasciaOraria;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
    }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public FasciaOraria getFasciaOraria() { return fasciaOraria; }


    public LocalTime getOraInizio() { return oraInizio; }


    public LocalTime getOraFine() { return oraFine; }

}
