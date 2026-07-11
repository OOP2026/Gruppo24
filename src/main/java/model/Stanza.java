package model;

public class Stanza {

    private int idReparto;
    private String numeroStanza;
    private int capienzaMax;



    public Stanza(int idReparto, String numeroStanza, int capienzaMax) {
        this.idReparto = idReparto;
        this.numeroStanza = numeroStanza;
        this.capienzaMax = capienzaMax;
    }

    public int getIdReparto() { return idReparto; }
    public void setIdReparto(int idReparto) { this.idReparto = idReparto; }

    public String getNumeroStanza() { return numeroStanza; }


    public int getCapienzaMax() { return capienzaMax; }

}
