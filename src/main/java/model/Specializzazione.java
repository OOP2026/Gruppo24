package model;

public class Specializzazione {

    private String nomeSpecializzazione;



    public Specializzazione(String nomeSpecializzazione) {
        this.nomeSpecializzazione = nomeSpecializzazione;
    }

    public String getNomeSpecializzazione() { return nomeSpecializzazione; }


    @Override
    public String toString() {
        return nomeSpecializzazione;
    }
}
