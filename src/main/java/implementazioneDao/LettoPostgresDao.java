package implementazioneDao;

import dao.DAOException;
import dao.LettoDAO;
import database_connection.ConnessioneDatabase;
import model.Letto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LettoPostgresDao implements LettoDAO {

    private Letto mapRow(ResultSet rs) throws SQLException {
        return new Letto(
            rs.getString("CodiceUnivoco"),
            rs.getInt("IdReparto"),
            rs.getString("NumeroStanza")
        );
    }

    @Override
    public Optional<Letto> findById(String codiceUnivoco) {
        String sql = "SELECT CodiceUnivoco, IdReparto, NumeroStanza FROM Letto WHERE CodiceUnivoco = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codiceUnivoco);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findById Letto: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Letto> findAll() {
        String sql = "SELECT CodiceUnivoco, IdReparto, NumeroStanza FROM Letto ORDER BY CodiceUnivoco";
        List<Letto> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll Letto: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Letto> findByReparto(int idReparto) {
        String sql = "SELECT CodiceUnivoco, IdReparto, NumeroStanza FROM Letto WHERE IdReparto = ? ORDER BY CodiceUnivoco";
        List<Letto> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReparto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findByReparto Letto: " + e.getMessage(), e);
        }
        return result;
    }


    @Override
    public List<Letto> trovaLettiOccupati() {
        String sql = "SELECT CodiceUnivoco, IdReparto, NumeroStanza FROM LettiOccupati ORDER BY CodiceUnivoco";
        List<Letto> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore trovaLettiOccupati: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(Letto l) {
        String sql = "INSERT INTO Letto (CodiceUnivoco, IdReparto, NumeroStanza) VALUES (?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getCodiceUnivoco());
            ps.setInt(2, l.getIdReparto());
            ps.setString(3, l.getNumeroStanza());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore insert Letto: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Letto l) {
        String sql = "UPDATE Letto SET IdReparto = ?, NumeroStanza = ? WHERE CodiceUnivoco = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, l.getIdReparto());
            ps.setString(2, l.getNumeroStanza());
            ps.setString(3, l.getCodiceUnivoco());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore update Letto: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String codiceUnivoco) {
        String sql = "DELETE FROM Letto WHERE CodiceUnivoco = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codiceUnivoco);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete Letto: " + e.getMessage(), e);
        }
    }
}
