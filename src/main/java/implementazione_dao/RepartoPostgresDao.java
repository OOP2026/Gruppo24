package implementazione_dao;

import exceptions.DAOException;
import dao.RepartoDAO;
import database_connection.ConnessioneDatabase;
import model.Reparto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepartoPostgresDao implements RepartoDAO {

    private Reparto mapRow(ResultSet rs) throws SQLException {
        return new Reparto(
            rs.getInt("IdReparto"),
            rs.getString("NomeReparto"),
            rs.getShort("Piano"),
            rs.getString("Ala")
        );
    }

    @Override
    public Optional<Reparto> findById(int id) {
        String sql = "SELECT IdReparto, NomeReparto, Piano, Ala FROM Reparto WHERE IdReparto = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findById Reparto: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reparto> findAll() {
        String sql = "SELECT IdReparto, NomeReparto, Piano, Ala FROM Reparto ORDER BY NomeReparto";
        List<Reparto> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll Reparto: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(Reparto r) {
        String sql = "INSERT INTO Reparto (NomeReparto, Piano, Ala) VALUES (?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getNomeReparto());
            ps.setShort(2, (short) r.getPiano());
            ps.setString(3, r.getAla());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setIdReparto(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore insert Reparto: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Reparto r) {
        String sql = "UPDATE Reparto SET NomeReparto = ?, Piano = ?, Ala = ? WHERE IdReparto = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getNomeReparto());
            ps.setShort(2, (short) r.getPiano());
            ps.setString(3, r.getAla());
            ps.setInt(4, r.getIdReparto());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore update Reparto: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Reparto WHERE IdReparto = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete Reparto: " + e.getMessage(), e);
        }
    }
}
