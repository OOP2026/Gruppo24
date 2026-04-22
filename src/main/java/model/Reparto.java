package model;

import utils.Validators;

import java.util.ArrayList;
import java.util.List;

public class Reparto {

    private String nome;

    private final List<Stanza> stanze;

    public Reparto(String nome) {

        Validators.validaStringa(nome, "Nome Reparto");

        this.nome = nome;
        this.stanze = new ArrayList<>();

    }

    public void aggiungiStanza(Stanza stanza) {

        Validators.validaOggetto(stanza, "Stanza");

        this.stanze.add(stanza);

    }

    public String getNome() { return nome; }

    public void setNome(String nome) {

        Validators.validaStringa(nome, "Nome Reparto");

        this.nome = nome;

    }

    public List<Stanza> getStanze() { return stanze; }

}