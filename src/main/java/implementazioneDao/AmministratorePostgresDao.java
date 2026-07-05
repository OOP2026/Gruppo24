package implementazioneDao;

import dao.AmministratoreDAO;
import dao.DAOException;
import database_connection.ConnessioneDatabase;
import model.Amministratore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AmministratorePostgresDao implements AmministratoreDAO {

    private Amministratore mapRow(ResultSet rs) throws SQLException {
        return new Amministratore(
            rs.getInt("IdAdmin"),
            rs.getString("Login"),
            rs.getString("Password")
        );
    }

    @Override
    public Optional<Amministratore> findById(int id) {
        String sql = "SELECT IdAdmin, Login, Password FROM Admin WHERE IdAdmin = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findById Amministratore: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Amministratore> findAll() {
        String sql = "SELECT IdAdmin, Login, Password FROM Admin ORDER BY IdAdmin";
        List<Amministratore> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll Amministratore: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(Amministratore a) {
        String sql = "INSERT INTO Admin (Login, Password) VALUES (?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getLogin());
            ps.setString(2, a.getPassword());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.setIdAdmin(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore insert Amministratore: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Amministratore a) {
        String sql = "UPDATE Admin SET Login = ?, Password = ? WHERE IdAdmin = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getLogin());
            ps.setString(2, a.getPassword());
            ps.setInt(3, a.getIdAdmin());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore update Amministratore: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Admin WHERE IdAdmin = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete Amministratore: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Amministratore> autentica(String login, String password) {
        String sql = "SELECT IdAdmin, Login, Password FROM Admin WHERE Login = ? AND Password = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore autenticazione Amministratore: " + e.getMessage(), e);
        }
    }
}
