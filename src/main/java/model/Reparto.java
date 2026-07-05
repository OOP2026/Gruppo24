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
    public void setNomeReparto(String nomeReparto) { this.nomeReparto = nomeReparto; }

    public int getPiano() { return piano; }
    public void setPiano(int piano) { this.piano = piano; }

    public String getAla() { return ala; }
    public void setAla(String ala) { this.ala = ala; }

    @Override
    public String toString() {
        return nomeReparto;
    }
}
