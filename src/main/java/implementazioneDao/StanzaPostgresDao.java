package implementazioneDao;

import dao.DAOException;
import dao.StanzaDAO;
import database_connection.ConnessioneDatabase;
import model.Stanza;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StanzaPostgresDao implements StanzaDAO {

    private Stanza mapRow(ResultSet rs) throws SQLException {
        return new Stanza(
            rs.getInt("IdReparto"),
            rs.getString("NumeroStanza"),
            rs.getInt("CapienzaMax")
        );
    }

    @Override
    public Optional<Stanza> findById(int idReparto, String numeroStanza) {
        String sql = "SELECT * FROM Stanza WHERE IdReparto = ? AND NumeroStanza = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReparto);
            ps.setString(2, numeroStanza);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findById Stanza: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Stanza> findAll() {
        String sql = "SELECT * FROM Stanza ORDER BY IdReparto, NumeroStanza";
        List<Stanza> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll Stanza: " + e.getMessage(), e);
        }
        return result;
    }


    public List<Stanza> findByReparto(int idReparto) {
        String sql = "SELECT * FROM Stanza WHERE IdReparto = ? ORDER BY NumeroStanza";
        List<Stanza> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReparto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findByReparto Stanza: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(Stanza s) {
        String sql = "INSERT INTO Stanza (IdReparto, NumeroStanza, CapienzaMax) VALUES (?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getIdReparto());
            ps.setString(2, s.getNumeroStanza());
            ps.setInt(3, s.getCapienzaMax());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore insert Stanza: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Stanza s) {
        String sql = "UPDATE Stanza SET CapienzaMax = ? WHERE IdReparto = ? AND NumeroStanza = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getCapienzaMax());
            ps.setInt(2, s.getIdReparto());
            ps.setString(3, s.getNumeroStanza());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore update Stanza: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int idReparto, String numeroStanza) {
        String sql = "DELETE FROM Stanza WHERE IdReparto = ? AND NumeroStanza = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReparto);
            ps.setString(2, numeroStanza);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete Stanza: " + e.getMessage(), e);
        }
    }
}
