package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TurnoScoperto(
        LocalDate data,
        FasciaOraria fasciaOraria,
        LocalTime oraInizio,
        LocalTime oraFine,
        List<Medico> sostitutiCandidati
) {}
