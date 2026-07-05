package model;

public class Stanza {

    private int idReparto;
    private String numeroStanza;
    private int capienzaMax;

    public Stanza() {}

    public Stanza(int idReparto, String numeroStanza, int capienzaMax) {
        this.idReparto = idReparto;
        this.numeroStanza = numeroStanza;
        this.capienzaMax = capienzaMax;
    }

    public int getIdReparto() { return idReparto; }
    public void setIdReparto(int idReparto) { this.idReparto = idReparto; }

    public String getNumeroStanza() { return numeroStanza; }
    public void setNumeroStanza(String numeroStanza) { this.numeroStanza = numeroStanza; }

    public int getCapienzaMax() { return capienzaMax; }
    public void setCapienzaMax(int capienzaMax) { this.capienzaMax = capienzaMax; }
}
