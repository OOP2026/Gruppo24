package dao;

import model.Prestazione;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrestazioneDAO {
    Optional<Prestazione> findById(String numeroPratica, int numeroPrestazione);
    List<Prestazione> findAll();
    List<Prestazione> findByRicovero(String numeroPratica);
    List<Prestazione> findByMedico(int idMedico);
    List<Prestazione> findByMedicoAndDate(int idMedico, LocalDate data);
    void insert(Prestazione p);
    void update(Prestazione p);
    void delete(String numeroPratica, int numeroPrestazione);
    void aggiornaEsito(String numeroPratica, int numeroPrestazione, String nuovoEsito);
}
