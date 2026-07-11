package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Sostituto {

    private LocalDate data;
    private FasciaOraria fasciaOraria;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private int idSostituto;
    private String nomeSostituto;
    private String cognomeSostituto;

    public Sostituto() {}

    public Sostituto(LocalDate data, FasciaOraria fasciaOraria,
                     LocalTime oraInizio, LocalTime oraFine,
                     int idSostituto, String nomeSostituto, String cognomeSostituto) {
        this.data = data;
        this.fasciaOraria = fasciaOraria;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.idSostituto = idSostituto;
        this.nomeSostituto = nomeSostituto;
        this.cognomeSostituto = cognomeSostituto;
    }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public FasciaOraria getFasciaOraria() { return fasciaOraria; }
    public void setFasciaOraria(FasciaOraria fasciaOraria) { this.fasciaOraria = fasciaOraria; }

    public LocalTime getOraInizio() { return oraInizio; }
    public void setOraInizio(LocalTime oraInizio) { this.oraInizio = oraInizio; }

    public LocalTime getOraFine() { return oraFine; }
    public void setOraFine(LocalTime oraFine) { this.oraFine = oraFine; }

    public int getIdSostituto() { return idSostituto; }
    public void setIdSostituto(int idSostituto) { this.idSostituto = idSostituto; }

    public String getNomeSostituto() { return nomeSostituto; }
    public void setNomeSostituto(String nomeSostituto) { this.nomeSostituto = nomeSostituto; }

    public String getCognomeSostituto() { return cognomeSostituto; }
    public void setCognomeSostituto(String cognomeSostituto) { this.cognomeSostituto = cognomeSostituto; }
}
