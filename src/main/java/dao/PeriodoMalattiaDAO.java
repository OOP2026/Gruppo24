package dao;

import model.PeriodoMalattia;
import model.PrestazioneScoperta;
import model.RiassegnazionePrestazione;
import model.RiassegnazioneTurno;
import model.TurnoScoperto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PeriodoMalattiaDAO {
    Optional<PeriodoMalattia> findById(String codiceCertificato);
    List<PeriodoMalattia> findAll();

    List<TurnoScoperto> trovaTurniScoperti(int idMedico, LocalDate inizio, LocalDate fine);
    List<PrestazioneScoperta> trovaPrestazioniScoperte(int idMedico, LocalDate inizio, LocalDate fine);
    void applicaRiassegnazioni(List<RiassegnazioneTurno> turni,
                               List<RiassegnazionePrestazione> prestazioni);

    void insert(PeriodoMalattia pm);
    void update(PeriodoMalattia pm);
    void delete(String codiceCertificato);
}
