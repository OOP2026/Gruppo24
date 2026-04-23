package utils;

import java.time.LocalDate;

public class Validators {

    public static <T extends Comparable<T>> void validaIntervalloTempo(T inizio, T fine) {
        if (inizio == null || fine == null) {
            throw new IllegalArgumentException("Date o orari mancanti.");
        }
        if (inizio.compareTo(fine) > 0) {
            throw new IllegalArgumentException("La data/ora di inizio non può superare quella di fine!");
        }
    }

    public static void validaDataPassata(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("Data mancante.");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Errore: La data non può essere nel futuro!");
        }
    }

    public static void validaStringa(String valore, String nomeCampo) {
        if (valore == null || valore.trim().isEmpty()) {
            throw new IllegalArgumentException("Il campo " + nomeCampo + " non può essere vuoto.");
        }
    }

    public static void validaCodiceFiscale(String cf) {
        validaStringa(cf, "Codice Fiscale");
        if (cf.length() != 16) {
            throw new IllegalArgumentException("Il Codice Fiscale deve contenere esattamente 16 caratteri.");
        }
    }

    public static void validaNumeroPositivo(int numero, String nomeCampo) {
        if (numero <= 0) {
            throw new IllegalArgumentException("Il campo " + nomeCampo + " deve essere maggiore di zero.");
        }
    }

    public static void validaOggetto(Object obj, String nomeCampo) {
        if (obj == null) {
            throw new IllegalArgumentException("L'entità " + nomeCampo + " non può essere nulla o mancante.");
        }
    }

}
