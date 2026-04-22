package model;

import utils.Validators;

import java.time.LocalDate;

public class Paziente {

    private final String codiceFiscale;

    private String nome;
    private String cognome;
    private LocalDate dataNascita;

    public Paziente(String codiceFiscale, String nome, String cognome, LocalDate dataNascita) {

        Validators.validaCodiceFiscale(codiceFiscale);
        Validators.validaStringa(nome, "Nome");
        Validators.validaStringa(cognome, "Cognome");
        Validators.validaDataPassata(dataNascita); // Nessuno può nascere domani!

        this.codiceFiscale = codiceFiscale;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;

    }

    public String getCodiceFiscale() { return codiceFiscale; }

    public String getNome() { return nome; }

    public void setNome(String nome) {

        Validators.validaStringa(nome, "Nome");

        this.nome = nome;

    }

    public String getCognome() { return cognome; }

    public void setCognome(String cognome) {

        Validators.validaStringa(cognome, "Cognome");

        this.cognome = cognome;

    }

    public LocalDate getDataNascita() { return dataNascita; }

    public void setDataNascita(LocalDate dataNascita) {

        Validators.validaDataPassata(dataNascita);

        this.dataNascita = dataNascita;

    }

}