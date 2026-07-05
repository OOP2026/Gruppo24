package dao;

import model.Stanza;

import java.util.List;
import java.util.Optional;

public interface StanzaDAO {
    Optional<Stanza> findById(int idReparto, String numeroStanza);
    List<Stanza> findAll();
    List<Stanza> findByReparto(int idReparto);
    void insert(Stanza s);
    void update(Stanza s);
    void delete(int idReparto, String numeroStanza);
}
