package dao;

import model.Reparto;

import java.util.List;
import java.util.Optional;

public interface RepartoDAO {
    Optional<Reparto> findById(int id);
    List<Reparto> findAll();
    void insert(Reparto r);
    void update(Reparto r);
    void delete(int id);
}
