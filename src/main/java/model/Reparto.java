package model;

public class Reparto {

    private int idReparto;
    private String nomeReparto;
    private int piano;
    private String ala;

    public Reparto() {}

    public Reparto(int idReparto, String nomeReparto, int piano, String ala) {
        this.idReparto = idReparto;
        this.nomeReparto = nomeReparto;
        this.piano = piano;
        this.ala = ala;
    }

    public int getIdReparto() { return idReparto; }
    public void setIdReparto(int idReparto) { this.idReparto = idReparto; }

    public String getNomeReparto() { return nomeReparto; }


    public int getPiano() { return piano; }

    public String getAla() { return ala; }


    @Override
    public String toString() {
        return nomeReparto;
    }
}
