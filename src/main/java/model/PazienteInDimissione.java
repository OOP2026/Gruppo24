package model;

import java.time.LocalDateTime;

public class PazienteInDimissione {

    private int idPaziente;
    private String codiceFiscale;
    private String nome;
    private String cognome;
    private String numeroPratica;
    private String codiceUnivocoLetto;
    private LocalDateTime dataDimissione;

    public PazienteInDimissione() {}

    public PazienteInDimissione(int idPaziente, String codiceFiscale, String nome,
                                String cognome, String numeroPratica,
                                String codiceUnivocoLetto, LocalDateTime dataDimissione) {
        this.idPaziente = idPaziente;
        this.codiceFiscale = codiceFiscale;
        this.nome = nome;
        this.cognome = cognome;
        this.numeroPratica = numeroPratica;
        this.codiceUnivocoLetto = codiceUnivocoLetto;
        this.dataDimissione = dataDimissione;
    }

    public int getIdPaziente() { return idPaziente; }
    public void setIdPaziente(int idPaziente) { this.idPaziente = idPaziente; }

    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getNumeroPratica() { return numeroPratica; }
    public void setNumeroPratica(String numeroPratica) { this.numeroPratica = numeroPratica; }

    public String getCodiceUnivocoLetto() { return codiceUnivocoLetto; }
    public void setCodiceUnivocoLetto(String codiceUnivocoLetto) { this.codiceUnivocoLetto = codiceUnivocoLetto; }

    public LocalDateTime getDataDimissione() { return dataDimissione; }
    public void setDataDimissione(LocalDateTime dataDimissione) { this.dataDimissione = dataDimissione; }
}
