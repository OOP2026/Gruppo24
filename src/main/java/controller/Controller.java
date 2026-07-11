package controller;

import dao.*;
import implementazioneDao.*;
import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    private final SpecializzazioneDAO specializzazioneDao;

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
        this.specializzazioneDao = new SpecializzazionePostgresDao();
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

    public Optional<Reparto> getReparto(int idReparto) {
        return repartoDao.findById(idReparto);
    }

    // Pazienti

    public List<Paziente> getPazienti() {
        return pazienteDao.findAll();
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
        if (dataNascita.isAfter(LocalDate.now()))
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
        if (dataNascita == null || dataNascita.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Data di nascita non valida.");
        Paziente p = pazienteDao.findById(idPaziente)
                .orElseThrow(() -> new IllegalArgumentException("Paziente non trovato."));
        p.setNome(nome.trim());
        p.setCognome(cognome.trim());
        p.setDataNascita(dataNascita);
        pazienteDao.update(p);
    }

    public Optional<Paziente> trovaPazientePerCF(String cf) {
        return pazienteDao.findByCodiceFiscale(cf);
    }

    // Letti

    public List<Letto> getLettiPerReparto(int idReparto) {
        return lettoDao.findByReparto(idReparto);
    }

    public List<Letto> getLettiDisponibiliPerReparto(int idReparto) {
        return lettoDao.trovaLettiDisponibiliInReparto(idReparto);
    }

    public List<Letto> getLettiOccupati() {
        return lettoDao.trovaLettiOccupati();
    }

    // Ricoveri

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

    public List<Sostituto> trovaSostituti(int idMedico, LocalDate inizio, LocalDate fine) {
        return periodoMalattiaDao.trovaSostituti(idMedico, inizio, fine);
    }

    // Agenda medico

    public List<Turno> getAgendaMedico(int idMedico, LocalDate inizio, LocalDate fine) {
        return turnoDao.trovaAgendaMedico(idMedico, inizio, fine);
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
