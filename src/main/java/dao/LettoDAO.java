package dao;

import model.Letto;

import java.util.List;
import java.util.Optional;

public interface LettoDAO {
    Optional<Letto> findById(String codiceUnivoco);
    List<Letto> findAll();
    List<Letto> findByReparto(int idReparto);
    List<Letto> trovaLettiOccupati();
    void insert(Letto l);
    void update(Letto l);
    void delete(String codiceUnivoco);
}
