package model;

import java.time.LocalDate;
import utils.Validators;

public class Malattia {
    // Classe immutabile, creare nuova Malattia quando servono modifiche

    private final LocalDate dataInizio;
    private final LocalDate dataFine;

    public Malattia(LocalDate dataInizio, LocalDate dataFine) {

        Validators.validaIntervalloTempo(dataInizio, dataFine);

        this.dataInizio = dataInizio;
        this.dataFine = dataFine;

    }

    public LocalDate getDataInizio() { return dataInizio; }
    public LocalDate getDataFine() { return dataFine; }

}