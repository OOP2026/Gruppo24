package dao;

import model.FasciaOraria;
import model.Turno;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TurnoDAO {
    Optional<Turno> findById(LocalDate data, FasciaOraria fasciaOraria);
    List<Turno> findAll();

    /**
     * Restituisce i turni pianificati nel periodo indicato (estremi inclusi),
     * ciascuno arricchito con l'elenco dei medici assegnati (eventualmente vuoto).
     */
    List<Turno> findByPeriodoConAssegnazioni(LocalDate dal, LocalDate al);

    List<Turno> trovaAgendaMedico(int idMedico, LocalDate inizio, LocalDate fine);
    void insert(Turno t);
    void update(Turno t);
    void delete(LocalDate data, FasciaOraria fasciaOraria);
    void assegnaTurno(int idMedico, LocalDate data, FasciaOraria fascia);
}
