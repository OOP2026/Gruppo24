package implementazioneDao;

import dao.DAOException;
import dao.SpecializzazioneDAO;
import database_connection.ConnessioneDatabase;
import model.Specializzazione;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpecializzazionePostgresDao implements SpecializzazioneDAO {

    private Specializzazione mapRow(ResultSet rs) throws SQLException {
        return new Specializzazione(rs.getString("NomeSpecializzazione"));
    }

    @Override
    public List<Specializzazione> findAll() {
        String sql = "SELECT * FROM Specializzazione ORDER BY NomeSpecializzazione";
        List<Specializzazione> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll Specializzazione: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Specializzazione> findByMedico(int idMedico) {
        String sql = "SELECT NomeSpecializzazione FROM Specializzazione_Medico WHERE IdMedico = ? ORDER BY NomeSpecializzazione";
        List<Specializzazione> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new Specializzazione(rs.getString("NomeSpecializzazione")));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findByMedico Specializzazione: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(Specializzazione s) {
        String sql = "INSERT INTO Specializzazione (NomeSpecializzazione) VALUES (?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getNomeSpecializzazione());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore insert Specializzazione: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String nome) {
        String sql = "DELETE FROM Specializzazione WHERE NomeSpecializzazione = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete Specializzazione: " + e.getMessage(), e);
        }
    }

    @Override
    public void aggiungiSpecializzazioneMedico(int idMedico, String nome) {
        String sql = "INSERT INTO Specializzazione_Medico (IdMedico, NomeSpecializzazione) VALUES (?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setString(2, nome);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore aggiungiSpecializzazioneMedico: " + e.getMessage(), e);
        }
    }

    @Override
    public void rimuoviSpecializzazioneMedico(int idMedico, String nome) {
        String sql = "DELETE FROM Specializzazione_Medico WHERE IdMedico = ? AND NomeSpecializzazione = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setString(2, nome);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore rimuoviSpecializzazioneMedico: " + e.getMessage(), e);
        }
    }
}
