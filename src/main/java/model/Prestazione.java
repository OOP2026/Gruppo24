package model;

import java.time.LocalDateTime;
import utils.Validators;

public class Prestazione {

    private final LocalDateTime inizio;
    private final LocalDateTime fine;
    private final TipoPrestazione tipo;
    private final Medico medicoEsecutore;
    private final Ricovero ricovero;
    private String esito;

    public Prestazione(LocalDateTime inizio, LocalDateTime fine, TipoPrestazione tipo, Medico medicoEsecutore, Ricovero ricovero) {

        Validators.validaIntervalloTempo(inizio, fine);
        Validators.validaOggetto(tipo, "Tipo Prestazione");
        Validators.validaOggetto(medicoEsecutore, "Medico Esecutore");
        Validators.validaOggetto(ricovero, "Ricovero");

        this.inizio = inizio;
        this.fine = fine;
        this.tipo = tipo;
        this.medicoEsecutore = medicoEsecutore;
        this.ricovero = ricovero;
        this.esito = "";

    }

    public LocalDateTime getInizio() { return inizio; }
    public LocalDateTime getFine() { return fine; }
    public TipoPrestazione getTipo() { return tipo; }
    public Medico getMedicoEsecutore() { return medicoEsecutore; }
    public Ricovero getRicovero() { return ricovero; }

    public String getEsito() { return esito; }

    public void compilaEsito(String nuovoEsito) {

        Validators.validaStringa(nuovoEsito, "Esito Prestazione");

        this.esito = nuovoEsito;

    }

}