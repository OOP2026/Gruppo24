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



    public String getCodiceFiscale() { return codiceFiscale; }


    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }




    public String getCodiceUnivocoLetto() { return codiceUnivocoLetto; }


    public LocalDateTime getDataDimissione() { return dataDimissione; }

}
