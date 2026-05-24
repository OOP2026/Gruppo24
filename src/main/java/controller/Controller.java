package controller;

import model.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe Controller: unico intermediario tra il package gui e il package model.
 * Gestisce lo stato in memoria (no DB in questa fase).
 */
public class Controller {

    // ─── Data stores in-memory ──────────────────────────────────────────────
    private final List<Utente>   utenti    = new ArrayList<>();
    private final List<Reparto>  reparti   = new ArrayList<>();
    private final List<Paziente> pazienti  = new ArrayList<>();
    private final List<Ricovero> ricoveri  = new ArrayList<>();

    private Utente utenteCorrente;

    public Controller() {
        inizializzaDatiDemo();
    }

    // ════════════════════════════════════════════════════════════════════════
    // AUTH
    // ════════════════════════════════════════════════════════════════════════

    /** Tenta il login. Restituisce l'utente se le credenziali sono valide, null altrimenti. */
    public Utente login(String login, String password) {
        for (Utente u : utenti) {
            if (u.getLogin().equals(login) && u.getPassword().equals(password)) {
                this.utenteCorrente = u;
                return u;
            }
        }
        return null;
    }

    public void logout() { this.utenteCorrente = null; }

    public Utente  getUtenteCorrente()  { return utenteCorrente; }
    public boolean isAmministratore()   { return utenteCorrente instanceof Amministratore; }
    public boolean isMedico()           { return utenteCorrente instanceof Medico; }

    /** Restituisce il medico corrente (null se l'utente non è un medico). */
    public Medico getMedicoCorrente() {
        return (utenteCorrente instanceof Medico) ? (Medico) utenteCorrente : null;
    }

    // ════════════════════════════════════════════════════════════════════════
    // STRUTTURA OSPEDALIERA (Reparti / Stanze / Letti)
    // ════════════════════════════════════════════════════════════════════════

    public List<Reparto> getReparti() { return Collections.unmodifiableList(reparti); }

    public Reparto aggiungiReparto(String nome) {
        Reparto r = new Reparto(nome);
        reparti.add(r);
        return r;
    }

    public Stanza aggiungiStanza(Reparto reparto, int numero) {
        Stanza s = new Stanza(numero);
        reparto.aggiungiStanza(s);
        return s;
    }

    public Letto aggiungiLetto(Stanza stanza, String codice) {
        Letto l = new Letto(codice);
        stanza.getLetti().add(l);   // getLetti() restituisce la lista interna (non defensive copy)
        return l;
    }

    /** Restituisce tutti i letti di un reparto (tutte le stanze). */
    public List<Letto> getTuttiLetti(Reparto reparto) {
        return reparto.getStanze().stream()
                .flatMap(s -> s.getLetti().stream())
                .collect(Collectors.toList());
    }

    /** Restituisce i letti LIBERI in questo momento in un reparto. */
    public List<Letto> getLettidiDisponibili(Reparto reparto) {
        return getTuttiLetti(reparto).stream()
                .filter(l -> !isLettoOccupatoOra(l))
                .collect(Collectors.toList());
    }

    /** true se il letto risulta occupato adesso (ricovero in corso). */
    public boolean isLettoOccupatoOra(Letto letto) {
        LocalDateTime ora = LocalDateTime.now();
        return isLettoOccupato(letto, ora, ora.plusSeconds(1));
    }

    // ════════════════════════════════════════════════════════════════════════
    // PAZIENTI
    // ════════════════════════════════════════════════════════════════════════

    public List<Paziente> getPazienti() { return Collections.unmodifiableList(pazienti); }

    public Paziente aggiungiPaziente(String cf, String nome, String cognome, LocalDate dataNascita) {
        for (Paziente p : pazienti) {
            if (p.getCodiceFiscale().equalsIgnoreCase(cf))
                throw new IllegalArgumentException("Paziente con CF " + cf + " già registrato.");
        }
        Paziente p = new Paziente(cf, nome, cognome, dataNascita);
        pazienti.add(p);
        return p;
    }

    public void modificaPaziente(Paziente paziente, String nome, String cognome, LocalDate dataNascita) {
        paziente.setNome(nome);
        paziente.setCognome(cognome);
        paziente.setDataNascita(dataNascita);
    }

    public Paziente trovaPazientePerCF(String cf) {
        return pazienti.stream()
                .filter(p -> p.getCodiceFiscale().equalsIgnoreCase(cf))
                .findFirst().orElse(null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // RICOVERI
    // ════════════════════════════════════════════════════════════════════════

    public List<Ricovero> getRicoveri() { return Collections.unmodifiableList(ricoveri); }

    /**
     * Crea e registra un nuovo ricovero.
     * Lancia IllegalStateException se il letto è già occupato nell'intervallo.
     * dataDimissione può essere null (ricovero a tempo indeterminato).
     */
    public Ricovero aggiungiRicovero(Paziente paziente, Letto letto,
                                     LocalDateTime dataInizio, LocalDateTime dataDimissione) {
        LocalDateTime fine = (dataDimissione != null) ? dataDimissione : LocalDateTime.MAX;
        if (isLettoOccupato(letto, dataInizio, fine))
            throw new IllegalStateException(
                "Il letto " + letto.getCodiceUnivoco() + " è già occupato nell'intervallo indicato.");

        Ricovero r = new Ricovero(dataInizio, paziente, letto);
        if (dataDimissione != null) r.registraDimissione(dataDimissione);
        ricoveri.add(r);
        return r;
    }

    public void registraDimissione(Ricovero ricovero, LocalDateTime dataDimissione) {
        ricovero.registraDimissione(dataDimissione);
    }

    /** Ricoveri con dimissione prevista oggi. */
    public List<Ricovero> getPazientiInScadenzaOggi() {
        return getPazientiInScadenza(LocalDate.now());
    }

    /** Ricoveri con dimissione prevista nella data indicata. */
    public List<Ricovero> getPazientiInScadenza(LocalDate data) {
        return ricoveri.stream()
                .filter(r -> r.getDataDimissione() != null
                          && r.getDataDimissione().toLocalDate().equals(data))
                .collect(Collectors.toList());
    }

    /** Ricoveri attualmente in corso (nessuna dimissione registrata o dimissione futura). */
    public List<Ricovero> getRicoveriInCorso() {
        LocalDateTime ora = LocalDateTime.now();
        return ricoveri.stream()
                .filter(r -> r.isInCorso() || (r.getDataDimissione() != null && r.getDataDimissione().isAfter(ora)))
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════════════
    // MEDICI
    // ════════════════════════════════════════════════════════════════════════

    public List<Medico> getMedici() {
        return utenti.stream()
                .filter(u -> u instanceof Medico)
                .map(u -> (Medico) u)
                .collect(Collectors.toList());
    }

    public List<Medico> getMediciPerReparto(Reparto reparto) {
        return getMedici().stream()
                .filter(m -> m.getReparto().getNome().equals(reparto.getNome()))
                .collect(Collectors.toList());
    }

    public Medico aggiungiMedico(String login, String password,
                                  String nome, String cognome,
                                  String matricola, Reparto reparto) {
        for (Utente u : utenti) {
            if (u.getLogin().equals(login))
                throw new IllegalArgumentException("Login già in uso: " + login);
        }
        Medico m = new Medico(login, password, nome, cognome, matricola, reparto);
        utenti.add(m);
        return m;
    }

    // ════════════════════════════════════════════════════════════════════════
    // TURNI
    // ════════════════════════════════════════════════════════════════════════

    public void aggiungiTurno(Medico medico, DayOfWeek giorno,
                               LocalTime oraInizio, LocalTime oraFine) {
        medico.aggiungiTurno(new Turno(giorno, oraInizio, oraFine));
    }

    // ════════════════════════════════════════════════════════════════════════
    // PRESTAZIONI
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Registra una prestazione per il medico su un ricovero.
     * Controlla: (1) la prestazione deve ricadere in un turno del medico,
     *             (2) non deve sovrapporsi con altre sue prestazioni.
     */
    public Prestazione registraPrestazione(Medico medico, Ricovero ricovero,
                                            LocalDateTime inizio, LocalDateTime fine,
                                            TipoPrestazione tipo) {
        if (!medicoHaTurnoIn(medico, inizio, fine))
            throw new IllegalStateException(
                "La prestazione non ricade in nessun turno lavorativo del medico.");

        for (Prestazione p : medico.getPrestazioniErogate()) {
            if (inizio.isBefore(p.getFine()) && p.getInizio().isBefore(fine))
                throw new IllegalStateException(
                    "Il medico ha già un'altra prestazione in questo intervallo.");
        }

        Prestazione prestazione = new Prestazione(inizio, fine, tipo, medico, ricovero);
        medico.aggiungiPrestazione(prestazione);
        ricovero.aggiungiPrestazione(prestazione);
        return prestazione;
    }

    public void aggiornaEsito(Prestazione prestazione, String esito) {
        prestazione.compilaEsito(esito);
    }

    // ════════════════════════════════════════════════════════════════════════
    // AGENDA
    // ════════════════════════════════════════════════════════════════════════

    public List<Prestazione> getAgendaGiornaliera(Medico medico, LocalDate data) {
        return medico.getPrestazioniErogate().stream()
                .filter(p -> p.getInizio().toLocalDate().equals(data))
                .sorted(Comparator.comparing(Prestazione::getInizio))
                .collect(Collectors.toList());
    }

    /** Restituisce una mappa giorno → prestazioni per i 7 giorni a partire da inizioSettimana. */
    public Map<LocalDate, List<Prestazione>> getAgendaSettimanale(Medico medico, LocalDate inizioSettimana) {
        Map<LocalDate, List<Prestazione>> agenda = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate giorno = inizioSettimana.plusDays(i);
            agenda.put(giorno, getAgendaGiornaliera(medico, giorno));
        }
        return agenda;
    }

    // ════════════════════════════════════════════════════════════════════════
    // MALATTIA
    // ════════════════════════════════════════════════════════════════════════

    public void registraMalattia(Medico medico, LocalDate dataInizio, LocalDate dataFine) {
        medico.aggiungiMalattia(new Malattia(dataInizio, dataFine));
    }

    /** Turni del medico che cadono nel periodo di malattia. */
    public List<Turno> getTurniScoperti(Medico medico, LocalDate dataInizio, LocalDate dataFine) {
        List<Turno> scoperti = new ArrayList<>();
        for (LocalDate d = dataInizio; !d.isAfter(dataFine); d = d.plusDays(1)) {
            DayOfWeek giorno = d.getDayOfWeek();
            medico.getTurniProgrammati().stream()
                    .filter(t -> t.getGiornoDellaSettimana() == giorno)
                    .forEach(scoperti::add);
        }
        return scoperti;
    }

    /** Prestazioni del medico programmate nel periodo di malattia. */
    public List<Prestazione> getPrestazioniScoperte(Medico medico, LocalDate dataInizio, LocalDate dataFine) {
        return medico.getPrestazioniErogate().stream()
                .filter(p -> {
                    LocalDate data = p.getInizio().toLocalDate();
                    return !data.isBefore(dataInizio) && !data.isAfter(dataFine);
                })
                .collect(Collectors.toList());
    }

    /**
     * Medici sostitutivi: stesso reparto, nessun turno/prestazione sovrapposto
     * con i turni scoperti del medico assente nel periodo.
     */
    public List<Medico> getMediciSostitutivi(Medico medicoAssente,
                                              LocalDate dataInizio, LocalDate dataFine) {
        List<Turno> turniScoperti = getTurniScoperti(medicoAssente, dataInizio, dataFine);
        return getMediciPerReparto(medicoAssente.getReparto()).stream()
                .filter(m -> !m.getMatricola().equals(medicoAssente.getMatricola()))
                .filter(m -> !haSovrapposizioni(m, turniScoperti, dataInizio, dataFine))
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════════════════════════════════════
    // METODI PRIVATI DI SUPPORTO
    // ════════════════════════════════════════════════════════════════════════

    private boolean isLettoOccupato(Letto letto, LocalDateTime richInizio, LocalDateTime richFine) {
        for (Ricovero r : ricoveri) {
            if (!r.getLetto().getCodiceUnivoco().equals(letto.getCodiceUnivoco())) continue;
            LocalDateTime rInizio = r.getDataInizio();
            LocalDateTime rFine   = r.getDataDimissione() != null ? r.getDataDimissione() : LocalDateTime.MAX;
            // overlap: start1 < end2 AND start2 < end1
            if (richInizio.isBefore(rFine) && rInizio.isBefore(richFine)) return true;
        }
        return false;
    }

    private boolean medicoHaTurnoIn(Medico medico, LocalDateTime inizio, LocalDateTime fine) {
        DayOfWeek giorno   = inizio.getDayOfWeek();
        LocalTime  oraStart = inizio.toLocalTime();
        LocalTime  oraEnd   = fine.toLocalTime();
        return medico.getTurniProgrammati().stream()
                .anyMatch(t -> t.getGiornoDellaSettimana() == giorno
                            && !oraStart.isBefore(t.getOraInizio())
                            && !oraEnd.isAfter(t.getOraFine()));
    }

    private boolean haSovrapposizioni(Medico medico, List<Turno> turniScoperti,
                                       LocalDate dataInizio, LocalDate dataFine) {
        for (Turno ts : turniScoperti) {
            // Turno vs Turno
            for (Turno tm : medico.getTurniProgrammati()) {
                if (tm.getGiornoDellaSettimana() == ts.getGiornoDellaSettimana()
                        && ts.getOraInizio().isBefore(tm.getOraFine())
                        && tm.getOraInizio().isBefore(ts.getOraFine()))
                    return true;
            }
            // Turno vs Prestazioni nel periodo
            for (LocalDate d = dataInizio; !d.isAfter(dataFine); d = d.plusDays(1)) {
                if (d.getDayOfWeek() != ts.getGiornoDellaSettimana()) continue;
                final LocalDate day = d;
                for (Prestazione p : medico.getPrestazioniErogate()) {
                    if (!p.getInizio().toLocalDate().equals(day)) continue;
                    LocalTime ps = p.getInizio().toLocalTime();
                    LocalTime pe = p.getFine().toLocalTime();
                    if (ts.getOraInizio().isBefore(pe) && ps.isBefore(ts.getOraFine()))
                        return true;
                }
            }
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════════════════
    // DATI DEMO (sostituisce il DB in questa fase)
    // ════════════════════════════════════════════════════════════════════════

    private void inizializzaDatiDemo() {
        // Reparti
        Reparto cardiologia = aggiungiReparto("Cardiologia");
        Reparto chirurgia   = aggiungiReparto("Chirurgia");

        // Struttura letti
        Stanza c101 = aggiungiStanza(cardiologia, 101);
        aggiungiLetto(c101, "CARD-101-A");
        aggiungiLetto(c101, "CARD-101-B");
        Stanza c102 = aggiungiStanza(cardiologia, 102);
        aggiungiLetto(c102, "CARD-102-A");

        Stanza ch201 = aggiungiStanza(chirurgia, 201);
        aggiungiLetto(ch201, "CHIR-201-A");
        aggiungiLetto(ch201, "CHIR-201-B");

        // Utenti
        utenti.add(new Amministratore("admin", "admin123"));

        Medico m1 = aggiungiMedico("dr.rossi",   "pass123", "Mario", "Rossi",   "MED001", cardiologia);
        aggiungiTurno(m1, DayOfWeek.MONDAY,    LocalTime.of(8,0),  LocalTime.of(14,0));
        aggiungiTurno(m1, DayOfWeek.WEDNESDAY, LocalTime.of(8,0),  LocalTime.of(14,0));
        aggiungiTurno(m1, DayOfWeek.FRIDAY,    LocalTime.of(14,0), LocalTime.of(20,0));

        Medico m2 = aggiungiMedico("dr.verdi",   "pass456", "Luigi", "Verdi",   "MED002", cardiologia);
        aggiungiTurno(m2, DayOfWeek.TUESDAY,   LocalTime.of(8,0),  LocalTime.of(14,0));
        aggiungiTurno(m2, DayOfWeek.THURSDAY,  LocalTime.of(8,0),  LocalTime.of(14,0));

        Medico m3 = aggiungiMedico("dr.bianchi", "pass789", "Anna",  "Bianchi", "MED003", chirurgia);
        aggiungiTurno(m3, DayOfWeek.MONDAY,    LocalTime.of(7,0),  LocalTime.of(19,0));
        aggiungiTurno(m3, DayOfWeek.THURSDAY,  LocalTime.of(7,0),  LocalTime.of(19,0));

        // Pazienti
        Paziente p1 = aggiungiPaziente("RSSMRA80A01F839X", "Marco",  "Russo", LocalDate.of(1980, 1, 1));
        Paziente p2 = aggiungiPaziente("VRDGLI75B15H501Y", "Giulia", "Verdi", LocalDate.of(1975, 2, 15));

        // Ricovero demo (letto CARD-101-A, scade domani)
        List<Letto> lettiCard = getTuttiLetti(cardiologia);
        if (!lettiCard.isEmpty()) {
            aggiungiRicovero(p1, lettiCard.get(0),
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().plusDays(1));
        }
    }
}
