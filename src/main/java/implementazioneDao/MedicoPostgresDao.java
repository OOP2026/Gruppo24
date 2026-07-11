package implementazioneDao;

import dao.DAOException;
import dao.MedicoDAO;
import database_connection.ConnessioneDatabase;
import model.Medico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicoPostgresDao implements MedicoDAO {

    private Medico mapRow(ResultSet rs) throws SQLException {
        return new Medico(
            rs.getInt("IdMedico"),
            rs.getString("Login"),
            rs.getString("Password"),
            rs.getString("Matricola"),
            rs.getString("Nome"),
            rs.getString("Cognome"),
            rs.getInt("IdReparto")
        );
    }

    @Override
    public Optional<Medico> findById(int id) {
        String sql = "SELECT idmedico, login, password, matricola, nome, cognome, idreparto FROM Medico WHERE IdMedico = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findById Medico: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Medico> findAll() {
        String sql = "SELECT idmedico, login, password, matricola, nome, cognome, idreparto FROM Medico ORDER BY Cognome, Nome";
        List<Medico> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll Medico: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(Medico m) {
        String sql = "INSERT INTO Medico (Login, Password, Matricola, Nome, Cognome, IdReparto) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getLogin());
            ps.setString(2, m.getPassword());
            ps.setString(3, m.getMatricola());
            ps.setString(4, m.getNome());
            ps.setString(5, m.getCognome());
            ps.setInt(6, m.getIdReparto());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setIdMedico(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore insert Medico: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Medico m) {
        String sql = "UPDATE Medico SET Login = ?, Password = ?, Matricola = ?, Nome = ?, Cognome = ?, IdReparto = ? WHERE IdMedico = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getLogin());
            ps.setString(2, m.getPassword());
            ps.setString(3, m.getMatricola());
            ps.setString(4, m.getNome());
            ps.setString(5, m.getCognome());
            ps.setInt(6, m.getIdReparto());
            ps.setInt(7, m.getIdMedico());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore update Medico: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Medico WHERE IdMedico = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete Medico: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Medico> autentica(String login, String password) {
        String sql = "SELECT idmedico, login, password, matricola, nome, cognome, idreparto FROM Medico WHERE Login = ? AND Password = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore autenticazione Medico: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Medico> findByReparto(int idReparto) {
        String sql = "SELECT idmedico, login, password, matricola, nome, cognome, idreparto FROM Medico WHERE IdReparto = ? ORDER BY Cognome, Nome";
        List<Medico> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReparto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findByReparto Medico: " + e.getMessage(), e);
        }
        return result;
    }
}
