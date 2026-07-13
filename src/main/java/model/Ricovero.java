package model;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static utils.TimeZones.EUROPE_ROME;

public class Ricovero {

    private String numeroPratica;
    private LocalDateTime dataInizioRicovero;
    private LocalDateTime dataDimissioneRicovero;
    private String motivoRicovero;
    private int idPaziente;
    private String codiceUnivocoLetto;



    public Ricovero(String numeroPratica, LocalDateTime dataInizioRicovero,
                    LocalDateTime dataDimissioneRicovero, String motivoRicovero,
                    int idPaziente, String codiceUnivocoLetto) {
        this.numeroPratica = numeroPratica;
        this.dataInizioRicovero = dataInizioRicovero;
        this.dataDimissioneRicovero = dataDimissioneRicovero;
        this.motivoRicovero = motivoRicovero;
        this.idPaziente = idPaziente;
        this.codiceUnivocoLetto = codiceUnivocoLetto;
    }

    public boolean isInCorso() {
        return dataDimissioneRicovero == null
                || dataDimissioneRicovero.isAfter(LocalDateTime.now(ZoneId.of(EUROPE_ROME)));
    }

    public String getNumeroPratica() { return numeroPratica; }

    public LocalDateTime getDataInizioRicovero() { return dataInizioRicovero; }


    public LocalDateTime getDataDimissioneRicovero() { return dataDimissioneRicovero; }
    public void setDataDimissioneRicovero(LocalDateTime dataDimissioneRicovero) { this.dataDimissioneRicovero = dataDimissioneRicovero; }

    public String getMotivoRicovero() { return motivoRicovero; }

    public int getIdPaziente() { return idPaziente; }

    public String getCodiceUnivocoLetto() { return codiceUnivocoLetto; }


    @Override
    public String toString() {
        return numeroPratica + " – letto " + codiceUnivocoLetto;
    }
}
