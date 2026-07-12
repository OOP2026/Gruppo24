package implementazione_dao;

import dao.DAOException;
import dao.PazienteDAO;
import database_connection.ConnessioneDatabase;
import model.Paziente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PazientePostgresDao implements PazienteDAO {

    private Paziente mapRow(ResultSet rs) throws SQLException {
        return new Paziente(
            rs.getInt("IdPaziente"),
            rs.getString("CodiceFiscale").trim(),
            rs.getString("Nome"),
            rs.getString("Cognome"),
            rs.getDate("DataNascita").toLocalDate()
        );
    }

    @Override
    public Optional<Paziente> findById(int id) {
        String sql = "SELECT IdPaziente, CodiceFiscale, Nome, Cognome, DataNascita FROM Paziente WHERE IdPaziente = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findById Paziente: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Paziente> findByCodiceFiscale(String codiceFiscale) {
        String sql = "SELECT IdPaziente, CodiceFiscale, Nome, Cognome, DataNascita FROM Paziente WHERE CodiceFiscale = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codiceFiscale.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findByCodiceFiscale Paziente: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Paziente> findAll() {
        String sql = "SELECT IdPaziente, CodiceFiscale, Nome, Cognome, DataNascita FROM Paziente ORDER BY Cognome, Nome";
        List<Paziente> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll Paziente: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(Paziente p) {
        String sql = "INSERT INTO Paziente (CodiceFiscale, Nome, Cognome, DataNascita) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodiceFiscale().toUpperCase().trim());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getCognome());
            ps.setDate(4, Date.valueOf(p.getDataNascita()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setIdPaziente(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore insert Paziente: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Paziente p) {
        String sql = "UPDATE Paziente SET Nome = ?, Cognome = ?, DataNascita = ? WHERE IdPaziente = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getCognome());
            ps.setDate(3, Date.valueOf(p.getDataNascita()));
            ps.setInt(4, p.getIdPaziente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore update Paziente: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Paziente WHERE IdPaziente = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete Paziente: " + e.getMessage(), e);
        }
    }
}
