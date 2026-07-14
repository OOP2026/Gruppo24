package model;

import java.time.LocalDate;

public record RiassegnazioneTurno(
        int idMedicoAssente,
        int idSostituto,
        LocalDate data,
        FasciaOraria fasciaOraria
) {}
