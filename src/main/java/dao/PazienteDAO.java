package dao;

import model.Paziente;

import java.util.List;
import java.util.Optional;

public interface PazienteDAO {
    Optional<Paziente> findById(int id);
    Optional<Paziente> findByCodiceFiscale(String codiceFiscale);
    List<Paziente> findAll();
    void insert(Paziente p);
    void update(Paziente p);
    void delete(int id);
}
