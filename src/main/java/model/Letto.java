package model;

public class Letto {

    private String codiceUnivoco;
    private int idReparto;
    private String numeroStanza;



    public Letto(String codiceUnivoco, int idReparto, String numeroStanza) {
        this.codiceUnivoco = codiceUnivoco;
        this.idReparto = idReparto;
        this.numeroStanza = numeroStanza;
    }

    public String getCodiceUnivoco() { return codiceUnivoco; }


    public int getIdReparto() { return idReparto; }
    public void setIdReparto(int idReparto) { this.idReparto = idReparto; }

    public String getNumeroStanza() { return numeroStanza; }


    @Override
    public String toString() {
        return codiceUnivoco;
    }
}
