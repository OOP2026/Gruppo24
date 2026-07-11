package model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Sostituto(
        LocalDate data,
        FasciaOraria fasciaOraria,
        LocalTime oraInizio,
        LocalTime oraFine,
        int idSostituto,
        String nomeSostituto,
        String cognomeSostituto
) {}