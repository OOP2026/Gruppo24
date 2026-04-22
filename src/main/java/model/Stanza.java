package model;

import utils.Validators;

import java.util.ArrayList;
import java.util.List;

public class Stanza {
    private int numero;
    private List<Letto> letti;

    public Stanza(int numero) {
        Validators.validaNumeroPositivo(numero, "Numero Stanza");
        this.numero = numero;
        this.letti = new ArrayList<>();
    }

    public int getNumero() { return numero; }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public List<Letto> getLetti() {
        return letti;
    }

    public void setLetti(List<Letto> letti) {
        this.letti = letti;
    }
}
