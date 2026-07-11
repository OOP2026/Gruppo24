package model;

import java.time.LocalDate;

public class PeriodoMalattia {

    private String codiceCertificato;
    private LocalDate dataInizioMalattia;
    private LocalDate dataFineMalattia;
    private int idMedico;



    public PeriodoMalattia(String codiceCertificato, LocalDate dataInizioMalattia,
                           LocalDate dataFineMalattia, int idMedico) {
        this.codiceCertificato = codiceCertificato;
        this.dataInizioMalattia = dataInizioMalattia;
        this.dataFineMalattia = dataFineMalattia;
        this.idMedico = idMedico;
    }

    public String getCodiceCertificato() { return codiceCertificato; }

    public LocalDate getDataInizioMalattia() { return dataInizioMalattia; }

    public LocalDate getDataFineMalattia() { return dataFineMalattia; }


    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }
}
