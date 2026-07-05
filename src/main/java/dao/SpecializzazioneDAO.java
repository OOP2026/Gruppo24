package dao;

import model.Specializzazione;

import java.util.List;

public interface SpecializzazioneDAO {
    List<Specializzazione> findAll();
    List<Specializzazione> findByMedico(int idMedico);
    void insert(Specializzazione s);
    void delete(String nome);
    void aggiungiSpecializzazioneMedico(int idMedico, String nome);
    void rimuoviSpecializzazioneMedico(int idMedico, String nome);
}
