package model;

import java.time.LocalDate;

public class PeriodoMalattia {

    private String codiceCertificato;
    private LocalDate dataInizioMalattia;
    private LocalDate dataFineMalattia;
    private int idMedico;

    public PeriodoMalattia() {}

    public PeriodoMalattia(String codiceCertificato, LocalDate dataInizioMalattia,
                           LocalDate dataFineMalattia, int idMedico) {
        this.codiceCertificato = codiceCertificato;
        this.dataInizioMalattia = dataInizioMalattia;
        this.dataFineMalattia = dataFineMalattia;
        this.idMedico = idMedico;
    }

    public String getCodiceCertificato() { return codiceCertificato; }
    public void setCodiceCertificato(String codiceCertificato) { this.codiceCertificato = codiceCertificato; }

    public LocalDate getDataInizioMalattia() { return dataInizioMalattia; }
    public void setDataInizioMalattia(LocalDate dataInizioMalattia) { this.dataInizioMalattia = dataInizioMalattia; }

    public LocalDate getDataFineMalattia() { return dataFineMalattia; }
    public void setDataFineMalattia(LocalDate dataFineMalattia) { this.dataFineMalattia = dataFineMalattia; }

    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }
}
