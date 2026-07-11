package model;

import java.time.LocalDateTime;

public class Prestazione {

    private String numeroPratica;
    private int numeroPrestazione;
    private LocalDateTime dataInizioPrestazione;
    private LocalDateTime dataFinePrestazione;
    private String esito;
    private TipoPrestazione tipologiaPrestazione;
    private int idMedico;



    public Prestazione(String numeroPratica, int numeroPrestazione,
                       LocalDateTime dataInizioPrestazione, LocalDateTime dataFinePrestazione,
                       String esito, TipoPrestazione tipologiaPrestazione, int idMedico) {
        this.numeroPratica = numeroPratica;
        this.numeroPrestazione = numeroPrestazione;
        this.dataInizioPrestazione = dataInizioPrestazione;
        this.dataFinePrestazione = dataFinePrestazione;
        this.esito = esito;
        this.tipologiaPrestazione = tipologiaPrestazione;
        this.idMedico = idMedico;
    }

    public String getNumeroPratica() { return numeroPratica; }


    public int getNumeroPrestazione() { return numeroPrestazione; }
    public void setNumeroPrestazione(int numeroPrestazione) { this.numeroPrestazione = numeroPrestazione; }

    public LocalDateTime getDataInizioPrestazione() { return dataInizioPrestazione; }


    public LocalDateTime getDataFinePrestazione() { return dataFinePrestazione; }


    public String getEsito() { return esito; }


    public TipoPrestazione getTipologiaPrestazione() { return tipologiaPrestazione; }


    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }
}
