package dao;

import model.Amministratore;

import java.util.List;
import java.util.Optional;

public interface AmministratoreDAO {
    Optional<Amministratore> findById(int id);
    List<Amministratore> findAll();
    void insert(Amministratore a);
    void update(Amministratore a);
    void delete(int id);
    Optional<Amministratore> autentica(String login, String password);
}
