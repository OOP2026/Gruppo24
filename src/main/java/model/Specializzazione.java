package model;

public class Specializzazione {

    private String nomeSpecializzazione;

    public Specializzazione() {}

    public Specializzazione(String nomeSpecializzazione) {
        this.nomeSpecializzazione = nomeSpecializzazione;
    }

    public String getNomeSpecializzazione() { return nomeSpecializzazione; }
    public void setNomeSpecializzazione(String nomeSpecializzazione) { this.nomeSpecializzazione = nomeSpecializzazione; }

    @Override
    public String toString() {
        return nomeSpecializzazione;
    }
}
