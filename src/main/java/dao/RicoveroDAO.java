package dao;

import model.PazienteInDimissione;
import model.Ricovero;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RicoveroDAO {
    Optional<Ricovero> findById(String numeroPratica);
    List<Ricovero> findAll();
    List<Ricovero> findInCorso();
    List<PazienteInDimissione> trovaRicoveriInDimissione(LocalDate data);
    void insert(Ricovero r);
    void update(Ricovero r);
    void delete(String numeroPratica);
}
