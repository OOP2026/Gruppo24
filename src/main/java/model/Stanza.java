package model;

public class Stanza {

    private int idReparto;
    private int numeroStanza;
    private int capienzaMax;



    public Stanza(int idReparto, int numeroStanza, int capienzaMax) {
        this.idReparto = idReparto;
        this.numeroStanza = numeroStanza;
        this.capienzaMax = capienzaMax;
    }

    public int getIdReparto() { return idReparto; }
    public void setIdReparto(int idReparto) { this.idReparto = idReparto; }

    public int getNumeroStanza() { return numeroStanza; }


    public int getCapienzaMax() { return capienzaMax; }

}
