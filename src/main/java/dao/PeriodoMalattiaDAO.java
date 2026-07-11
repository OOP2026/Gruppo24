package dao;

import model.PeriodoMalattia;
import model.Sostituto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PeriodoMalattiaDAO {
    Optional<PeriodoMalattia> findById(String codiceCertificato);
    List<PeriodoMalattia> findAll();
    List<Sostituto> trovaSostituti(int idMedico, LocalDate inizio, LocalDate fine);
    void insert(PeriodoMalattia pm);
    void update(PeriodoMalattia pm);
    void delete(String codiceCertificato);
}
