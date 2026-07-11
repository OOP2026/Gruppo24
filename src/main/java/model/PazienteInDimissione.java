package model;

import java.time.LocalDateTime;

public record PazienteInDimissione(
        int idPaziente,
        String codiceFiscale,
        String nome,
        String cognome,
        String numeroPratica,
        String codiceUnivocoLetto,
        LocalDateTime dataDimissione
) {}
