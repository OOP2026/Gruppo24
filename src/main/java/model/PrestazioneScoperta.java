package model;

import java.time.LocalDateTime;
import java.util.List;

public record PrestazioneScoperta(
        String numeroPratica,
        int numeroPrestazione,
        LocalDateTime dataInizioPrestazione,
        LocalDateTime dataFinePrestazione,
        TipoPrestazione tipologia,
        List<Medico> sostitutiCandidati
) {}
