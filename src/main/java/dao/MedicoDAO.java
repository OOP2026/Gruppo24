package dao;

import model.Medico;

import java.util.List;
import java.util.Optional;

public interface MedicoDAO {
    Optional<Medico> findById(int id);
    List<Medico> findAll();
    void insert(Medico m);
    void update(Medico m);
    void delete(int id);
    Optional<Medico> autentica(String login, String password);
    List<Medico> findByReparto(int idReparto);
}
