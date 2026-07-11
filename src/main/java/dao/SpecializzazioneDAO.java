package dao;

import model.Specializzazione;

import java.util.List;

public interface SpecializzazioneDAO {
    List<Specializzazione> findAll();
    void insert(Specializzazione s);
    void delete(String nome);
}
