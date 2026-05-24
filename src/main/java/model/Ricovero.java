package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import utils.Validators;

public class Ricovero {

    private LocalDateTime dataInizio;
    private LocalDateTime dataDimissione;

    private Paziente paziente;
    private Letto letto;
    private List<Prestazione> prestazioniRicevute;

    public Ricovero(LocalDateTime dataInizio, Paziente paziente, Letto letto) {

        Validators.validaOggetto(dataInizio, "Data Inizio");
        Validators.validaOggetto(paziente, "Paziente");
        Validators.validaOggetto(letto, "Letto");

        this.dataInizio = dataInizio;
        this.paziente = paziente;
        this.letto = letto;
        this.prestazioniRicevute = new ArrayList<>();

    }

    public void registraDimissione(LocalDateTime dataDimissione) {

        Validators.validaIntervalloTempo(this.dataInizio, dataDimissione);
        this.dataDimissione = dataDimissione;

    }

    public void aggiungiPrestazione(Prestazione nuovaPrestazione) {

        Validators.validaOggetto(nuovaPrestazione, "Prestazione da aggiungere");

        if (nuovaPrestazione.getInizio().isBefore(this.dataInizio)) {
            throw new IllegalArgumentException("Errore: La prestazione non può avvenire prima dell'inizio del ricovero.");
        }

        if (this.dataDimissione != null && nuovaPrestazione.getInizio().isAfter(this.dataDimissione)) {
            throw new IllegalArgumentException("Errore: La prestazione non può avvenire dopo la dimissione del paziente.");
        }

        this.prestazioniRicevute.add(nuovaPrestazione);

    }

    public List<Prestazione> getPrestazioniRicevute() {
        return Collections.unmodifiableList(prestazioniRicevute);
    }


    public void correggiDataInizio(LocalDateTime nuovaDataInizio) {

        if (this.dataDimissione != null) {
            Validators.validaIntervalloTempo(nuovaDataInizio, this.dataDimissione);
        }

        for (Prestazione p : this.prestazioniRicevute) {
            if (nuovaDataInizio.isAfter(p.getInizio())) {
                throw new IllegalStateException("Impossibile posticipare l'inizio: esistono prestazioni pregresse.");
            }
        }

        this.dataInizio = nuovaDataInizio;
    }

    public boolean isInCorso() {
        return this.dataDimissione == null;
    }

    public LocalDateTime getDataInizio() { return dataInizio; }
    public LocalDateTime getDataDimissione() { return dataDimissione; }
    public Paziente getPaziente() { return paziente; }
    public Letto getLetto() { return letto; }

}