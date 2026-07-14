package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Turno {

    private LocalDate data;
    private FasciaOraria fasciaOraria;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private final List<Medico> mediciAssegnati = new ArrayList<>();

    public Turno(LocalDate data, FasciaOraria fasciaOraria,
                 LocalTime oraInizio, LocalTime oraFine) {
        this.data = data;
        this.fasciaOraria = fasciaOraria;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
    }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public FasciaOraria getFasciaOraria() { return fasciaOraria; }

    public LocalTime getOraInizio() { return oraInizio; }

    public LocalTime getOraFine() { return oraFine; }

    public List<Medico> getMediciAssegnati() {
        return Collections.unmodifiableList(mediciAssegnati);
    }

    public void addMedicoAssegnato(Medico medico) {
        if (medico != null) mediciAssegnati.add(medico);
    }

    public boolean isAssegnato() {
        return !mediciAssegnati.isEmpty();
    }

    /**
     * Restituisce una rappresentazione leggibile dei medici assegnati
     * (es. "Dr. Mario Rossi, Dr. Luigi Verdi") oppure stringa vuota se nessuno.
     */
    public String getDescrizioneAssegnazione() {
        return mediciAssegnati.stream()
                .map(m -> "Dr. " + m.getNome() + " " + m.getCognome())
                .collect(Collectors.joining(", "));
    }
}
