package controller;

import dao.*;
import exceptions.DAOException;
import exceptions.ValidationException;
import implementazione_dao.*;
import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static utils.TimeZones.EUROPE_ROME;

public class Controller {

    private final AmministratoreDAO amministratoreDao;
    private final MedicoDAO medicoDao;
    private final PazienteDAO pazienteDao;
    private final RepartoDAO repartoDao;
    private final LettoDAO lettoDao;
    private final RicoveroDAO ricoveroDao;
    private final PrestazioneDAO prestazioneDao;
    private final TurnoDAO turnoDao;
    private final PeriodoMalattiaDAO periodoMalattiaDao;

    private Amministratore amministratoreCorrente;
    private Medico medicoCorrente;
    private Reparto repartoMedicoCorrente;

    public Controller() {
        this.amministratoreDao = new AmministratorePostgresDao();
        this.medicoDao = new MedicoPostgresDao();
        this.pazienteDao = new PazientePostgresDao();
        this.repartoDao = new RepartoPostgresDao();
        this.lettoDao = new LettoPostgresDao();
        this.ricoveroDao = new RicoveroPostgresDao();
        this.prestazioneDao = new PrestazionePostgresDao();
        this.turnoDao = new TurnoPostgresDao();
        this.periodoMalattiaDao = new PeriodoMalattiaPostgresDao();
    }

    // Autenticazione

    public boolean login(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.isBlank())
            return false;
        Optional<Amministratore> admin = amministratoreDao.autentica(login, password);
        if (admin.isPresent()) {
            amministratoreCorrente = admin.get();
            medicoCorrente = null;
            repartoMedicoCorrente = null;
            return true;
        }
        Optional<Medico> medico = medicoDao.autentica(login, password);
        if (medico.isPresent()) {
            medicoCorrente = medico.get();
            amministratoreCorrente = null;
            repartoMedicoCorrente = repartoDao.findById(medicoCorrente.getIdReparto()).orElse(null);
            return true;
        }
        return false;
    }

    public void logout() {
        amministratoreCorrente = null;
        medicoCorrente = null;
        repartoMedicoCorrente = null;
    }

    public boolean isAmministratore() { return amministratoreCorrente != null; }
    public boolean isMedico()         { return medicoCorrente != null; }

    public Amministratore getAmministratoreCorrente() { return amministratoreCorrente; }
    public Medico         getMedicoCorrente()          { return medicoCorrente; }

    public String getNomeRepartoMedicoCorrente() {
        return repartoMedicoCorrente != null ? repartoMedicoCorrente.getNomeReparto() : "";
    }

    // Reparti

    public List<Reparto> getReparti() {
        return repartoDao.findAll();
    }

    public List<Paziente> getPazienti() {
        return pazienteDao.findAll();
    }

    public Optional<Paziente> getPazienteById(int idPaziente) {
       return pazienteDao.findById(idPaziente);
    }

    public Paziente aggiungiPaziente(String cf, String nome, String cognome, LocalDate dataNascita) {
        if (cf == null || cf.isBlank())
            throw new IllegalArgumentException("Codice Fiscale obbligatorio.");
        if (cf.trim().length() != 16)
            throw new IllegalArgumentException("Il Codice Fiscale deve avere esattamente 16 caratteri.");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome obbligatorio.");
        if (cognome == null || cognome.isBlank())
            throw new IllegalArgumentException("Cognome obbligatorio.");
        if (dataNascita == null)
            throw new IllegalArgumentException("Data di nascita obbligatoria.");
        if (dataNascita.isAfter(LocalDate.now(ZoneId.of(EUROPE_ROME))))
            throw new IllegalArgumentException("La data di nascita non può essere nel futuro.");
        if (pazienteDao.findByCodiceFiscale(cf).isPresent())
            throw new IllegalArgumentException("Paziente con CF " + cf + " già registrato.");
        Paziente p = new Paziente(0, cf.toUpperCase().trim(), nome.trim(), cognome.trim(), dataNascita);
        pazienteDao.insert(p);
        return p;
    }

    public void modificaPaziente(int idPaziente, String nome, String cognome, LocalDate dataNascita) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome obbligatorio.");
        if (cognome == null || cognome.isBlank())
            throw new IllegalArgumentException("Cognome obbligatorio.");
        if (dataNascita == null || dataNascita.isAfter(LocalDate.now(ZoneId.of(EUROPE_ROME))))
            throw new IllegalArgumentException("Data di nascita non valida.");
        Paziente p = pazienteDao.findById(idPaziente)
                .orElseThrow(() -> new IllegalArgumentException("Paziente non trovato."));
        p.setNome(nome.trim());
        p.setCognome(cognome.trim());
        p.setDataNascita(dataNascita);
        pazienteDao.update(p);
    }

    // Letti

    public List<Letto> getLettiPerReparto(int idReparto) {
        return lettoDao.findByReparto(idReparto);
    }

    public List<Letto> getLettiOccupati() {
        return lettoDao.trovaLettiOccupati();
    }

    // Ricoveri
    public Optional<Ricovero> getRicoveroByNumPratica(String numeroPratica) { return ricoveroDao.findById(numeroPratica); }

    public List<Ricovero> getRicoveri() {
        return ricoveroDao.findAll();
    }

    public List<Ricovero> getRicoveriInCorso() {
        return ricoveroDao.findInCorso();
    }

    public void registraRicovero(String numeroPratica, int idPaziente,
                                  String codiceUnivocoLetto, LocalDateTime dataInizio,
                                  LocalDateTime dataDimissione, String motivo) {
        if (numeroPratica == null || numeroPratica.isBlank())
            throw new IllegalArgumentException("Numero pratica obbligatorio.");
        if (motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("Motivo del ricovero obbligatorio.");
        if (dataInizio == null)
            throw new IllegalArgumentException("Data di inizio obbligatoria.");
        if (dataDimissione != null && !dataDimissione.isAfter(dataInizio))
            throw new IllegalArgumentException("La data di dimissione deve essere successiva all'inizio.");
        if (ricoveroDao.findById(numeroPratica).isPresent())
            throw new IllegalArgumentException("Esiste già un ricovero con numero pratica " + numeroPratica + ".");
        Ricovero r = new Ricovero(numeroPratica, dataInizio, dataDimissione, motivo, idPaziente, codiceUnivocoLetto);
        ricoveroDao.insert(r);
    }

    public void registraDimissione(String numeroPratica, LocalDateTime dataDimissione) {
        if (dataDimissione == null)
            throw new IllegalArgumentException("Data di dimissione obbligatoria.");
        Ricovero r = ricoveroDao.findById(numeroPratica)
                .orElseThrow(() -> new IllegalArgumentException("Ricovero non trovato."));
        if (!r.isInCorso())
            throw new IllegalStateException("Il ricovero è già stato concluso.");
        if (!dataDimissione.isAfter(r.getDataInizioRicovero()))
            throw new IllegalArgumentException("La data di dimissione deve essere successiva all'inizio del ricovero.");
        r.setDataDimissioneRicovero(dataDimissione);
        ricoveroDao.update(r);
    }

    public List<PazienteInDimissione> getPazientiInDimissione(LocalDate data) {
        if (data == null) throw new IllegalArgumentException("Data obbligatoria.");
        return ricoveroDao.trovaRicoveriInDimissione(data);
    }

    // Medici

    public List<Medico> getMedici() {
        return medicoDao.findAll();
    }

    // Periodi di malattia

    public void registraPeriodoMalattia(String codiceCertificato, int idMedico,
                                         LocalDate dataInizio, LocalDate dataFine) {
        if (codiceCertificato == null || codiceCertificato.isBlank())
            throw new IllegalArgumentException("Codice certificato obbligatorio.");
        if (dataInizio == null || dataFine == null)
            throw new IllegalArgumentException("Date obbligatorie.");
        if (dataFine.isBefore(dataInizio))
            throw new IllegalArgumentException("La data di fine non può precedere quella di inizio.");
        if (periodoMalattiaDao.findById(codiceCertificato).isPresent())
            throw new IllegalArgumentException("Esiste già un certificato con codice " + codiceCertificato + ".");
        PeriodoMalattia pm = new PeriodoMalattia(codiceCertificato, dataInizio, dataFine, idMedico);
        periodoMalattiaDao.insert(pm);
    }

    public PeriodoMalattia trovaPeriodoMalattia(String codiceCertificato) throws ValidationException {
        if (codiceCertificato == null || codiceCertificato.isBlank())
            throw new ValidationException("Codice certificato obbligatorio.");
        return periodoMalattiaDao.findById(codiceCertificato)
                .orElseThrow(() -> new ValidationException(
                        "Nessun periodo di malattia trovato con codice " + codiceCertificato + "."));
    }

    public void aggiornaPeriodoMalattia(String codiceCertificato,
                                         LocalDate dataInizio, LocalDate dataFine) throws ValidationException {
        if (codiceCertificato == null || codiceCertificato.isBlank())
            throw new ValidationException("Codice certificato obbligatorio.");
        if (dataInizio == null || dataFine == null)
            throw new ValidationException("Date obbligatorie.");
        if (dataFine.isBefore(dataInizio))
            throw new ValidationException("La data di fine non può precedere quella di inizio.");
        PeriodoMalattia esistente = periodoMalattiaDao.findById(codiceCertificato)
                .orElseThrow(() -> new ValidationException(
                        "Nessun periodo di malattia trovato con codice " + codiceCertificato + "."));
        try {
            periodoMalattiaDao.update(new PeriodoMalattia(codiceCertificato, dataInizio, dataFine, esistente.getIdMedico()));
        } catch (DAOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public List<TurnoScoperto> turniScoperti(int idMedico, LocalDate inizio, LocalDate fine)
            throws ValidationException {
        validaPeriodoMalattia(inizio, fine);
        try {
            return periodoMalattiaDao.trovaTurniScoperti(idMedico, inizio, fine);
        } catch (DAOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public List<PrestazioneScoperta> prestazioniScoperte(int idMedico, LocalDate inizio, LocalDate fine)
            throws ValidationException {
        validaPeriodoMalattia(inizio, fine);
        try {
            return periodoMalattiaDao.trovaPrestazioniScoperte(idMedico, inizio, fine);
        } catch (DAOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public void applicaRiassegnazioni(List<RiassegnazioneTurno> turni,
                                      List<RiassegnazionePrestazione> prestazioni) throws ValidationException {
        boolean nessunTurno = turni == null || turni.isEmpty();
        boolean nessunaPrestazione = prestazioni == null || prestazioni.isEmpty();
        if (nessunTurno && nessunaPrestazione)
            throw new ValidationException("Nessuna riassegnazione selezionata.");
        try {
            periodoMalattiaDao.applicaRiassegnazioni(
                    nessunTurno ? List.of() : turni,
                    nessunaPrestazione ? List.of() : prestazioni);
        } catch (DAOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void validaPeriodoMalattia(LocalDate inizio, LocalDate fine) throws ValidationException {
        if (inizio == null || fine == null)
            throw new ValidationException("Le date del periodo di malattia sono obbligatorie.");
        if (fine.isBefore(inizio))
            throw new ValidationException("La data di fine non può precedere quella di inizio.");
    }

    // Agenda medico

    public List<Turno> getAgendaMedico(int idMedico, LocalDate inizio, LocalDate fine) {
        return turnoDao.trovaAgendaMedico(idMedico, inizio, fine);
    }

    // Turni — pianificazione

    public Optional<Turno> trovaTurno(LocalDate data, FasciaOraria fascia) {
        return turnoDao.findById(data, fascia);
    }

    public void pianificaTurno(LocalDate data, FasciaOraria fascia,
                                LocalTime oraInizio, LocalTime oraFine) throws ValidationException {
        validaTurno(data, fascia, oraInizio, oraFine);
        try {
            turnoDao.insert(new Turno(data, fascia, oraInizio, oraFine));
        } catch (DAOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public void modificaTurnoPianificato(LocalDate data, FasciaOraria fascia,
                                          LocalTime oraInizio, LocalTime oraFine) throws ValidationException {
        validaTurno(data, fascia, oraInizio, oraFine);
        if (!turnoDao.findById(data, fascia).isPresent())
            throw new ValidationException("Il turno del "
                    + data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " fascia " + fascia + " non è pianificato.");
        try {
            turnoDao.update(new Turno(data, fascia, oraInizio, oraFine));
        } catch (DAOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public void eliminaTurnoPianificato(LocalDate data, FasciaOraria fascia) throws ValidationException {
        if (data == null)  throw new ValidationException("La data è obbligatoria.");
        if (fascia == null) throw new ValidationException("La fascia oraria è obbligatoria.");
        try {
            turnoDao.delete(data, fascia);
        } catch (DAOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public List<Turno> getTurniPianificati(LocalDate inizio, LocalDate fine) {
        if (inizio == null || fine == null)
            throw new IllegalArgumentException("Date del filtro periodo obbligatorie.");
        if (fine.isBefore(inizio))
            throw new IllegalArgumentException("La data di fine non può precedere quella di inizio.");
        List<Turno> result = turnoDao.findByPeriodoConAssegnazioni(inizio, fine);
        result.sort(java.util.Comparator.comparing(Turno::getData)
                .thenComparingInt(t -> t.getFasciaOraria().ordinal()));
        return result;
    }

    public List<Turno> getTurniPianificatiConMedici(LocalDate da, LocalDate a,
                                                    FasciaOraria fasciaFiltro,
                                                    Integer idMedicoDaEscludere) {
        return getTurniPianificati(da, a).stream()
                .filter(t -> fasciaFiltro == null || t.getFasciaOraria() == fasciaFiltro)
                .filter(t -> idMedicoDaEscludere == null || t.getMediciAssegnati().stream()
                        .noneMatch(m -> m.getIdMedico() == idMedicoDaEscludere.intValue()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Turni — assegnazione

    public void assegnaTurnoAMedico(int idMedico, LocalDate data,
                                     FasciaOraria fascia) throws ValidationException {
        if (data == null)
            throw new ValidationException("La data è obbligatoria.");
        if (data.isBefore(LocalDate.now(ZoneId.of(EUROPE_ROME))))
            throw new ValidationException("La data non può essere nel passato.");
        if (fascia == null)
            throw new ValidationException("La fascia oraria è obbligatoria.");
        if (!turnoDao.findById(data, fascia).isPresent())
            throw new ValidationException("Il turno del "
                    + data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " fascia " + fascia
                    + " non è stato pianificato. Pianificarlo prima nel tab dedicato.");
        if (periodoMalattiaDao.isMedicoInMalattia(idMedico, data))
            throw new ValidationException("Il medico è in malattia in questa data e non può essere assegnato al turno.");
        try {
            turnoDao.assegnaTurno(idMedico, data, fascia);
        } catch (DAOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void validaTurno(LocalDate data, FasciaOraria fascia,
                               LocalTime oraInizio, LocalTime oraFine) throws ValidationException {
        if (data == null)
            throw new ValidationException("La data è obbligatoria.");
        if (data.isBefore(LocalDate.now(ZoneId.of(EUROPE_ROME))))
            throw new ValidationException("La data non può essere nel passato.");
        if (fascia == null)
            throw new ValidationException("La fascia oraria è obbligatoria.");
        if (oraInizio == null || oraFine == null)
            throw new ValidationException("Ora di inizio e fine sono obbligatorie.");
        switch (fascia) {
            case MATTINA:
                if (oraInizio.isBefore(LocalTime.of(6, 0)) || oraFine.isAfter(LocalTime.of(14, 0))
                        || !oraFine.isAfter(oraInizio))
                    throw new ValidationException(
                            "Il turno MATTINA deve iniziare non prima delle 06:00 e finire non oltre le 14:00.");
                break;
            case POMERIGGIO:
                if (oraInizio.isBefore(LocalTime.of(14, 0)) || oraFine.isAfter(LocalTime.of(22, 0))
                        || !oraFine.isAfter(oraInizio))
                    throw new ValidationException(
                            "Il turno POMERIGGIO deve iniziare non prima delle 14:00 e finire non oltre le 22:00.");
                break;
            case NOTTE:
                if (oraInizio.isBefore(LocalTime.of(22, 0)) || oraFine.isAfter(LocalTime.of(6, 0))
                        || !oraFine.isBefore(oraInizio))
                    throw new ValidationException(
                            "Il turno NOTTE deve iniziare non prima delle 22:00 e finire non oltre le 06:00 del giorno successivo.");
                break;
            default:
                break;
        }
    }

    // Prestazioni

    public List<Prestazione> getPrestazioniMedico(int idMedico) {
        return prestazioneDao.findByMedico(idMedico);
    }

    public List<Prestazione> getPrestazioniPerGiorno(int idMedico, LocalDate data) {
        return prestazioneDao.findByMedicoAndDate(idMedico, data);
    }

    public void registraPrestazione(String numeroPratica, LocalDateTime dataInizio,
                                     LocalDateTime dataFine, TipoPrestazione tipo) {
        if (medicoCorrente == null)
            throw new IllegalStateException("Nessun medico autenticato.");
        if (numeroPratica == null || numeroPratica.isBlank())
            throw new IllegalArgumentException("Numero pratica obbligatorio.");
        if (dataInizio == null || dataFine == null)
            throw new IllegalArgumentException("Date obbligatorie.");
        if (!dataFine.isAfter(dataInizio))
            throw new IllegalArgumentException("La fine deve essere successiva all'inizio.");
        if (!dataInizio.toLocalDate().equals(dataFine.toLocalDate()))
            throw new IllegalArgumentException("La prestazione deve svolgersi in un'unica giornata.");
        ricoveroDao.findById(numeroPratica)
                .orElseThrow(() -> new IllegalArgumentException("Ricovero " + numeroPratica + " non trovato."));
        Prestazione p = new Prestazione(numeroPratica, 0, dataInizio, dataFine, null, tipo, medicoCorrente.getIdMedico());
        prestazioneDao.insert(p);
    }

    public void aggiornaEsito(String numeroPratica, int numeroPrestazione, String esito) {
        if (esito == null || esito.isBlank())
            throw new IllegalArgumentException("L'esito non può essere vuoto.");
        prestazioneDao.aggiornaEsito(numeroPratica, numeroPrestazione, esito.trim());
    }
}
