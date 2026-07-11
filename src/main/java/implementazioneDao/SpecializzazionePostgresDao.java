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
}
