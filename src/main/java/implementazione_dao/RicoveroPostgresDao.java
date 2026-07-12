package implementazione_dao;

import exceptions.DAOException;
import dao.RicoveroDAO;
import database_connection.ConnessioneDatabase;
import model.PazienteInDimissione;
import model.Ricovero;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RicoveroPostgresDao implements RicoveroDAO {

    private Ricovero mapRow(ResultSet rs) throws SQLException {
        Timestamp dimissione = rs.getTimestamp("DataDimissioneRicovero");
        return new Ricovero(
            rs.getString("NumeroPratica"),
            rs.getTimestamp("DataInizioRicovero").toLocalDateTime(),
            dimissione != null ? dimissione.toLocalDateTime() : null,
            rs.getString("MotivoRicovero"),
            rs.getInt("IdPaziente"),
            rs.getString("CodiceUnivocoLetto")
        );
    }

    @Override
    public Optional<Ricovero> findById(String numeroPratica) {
        String sql = "SELECT NumeroPratica, DataInizioRicovero, DataDimissioneRicovero, MotivoRicovero, IdPaziente, CodiceUnivocoLetto FROM Ricovero WHERE NumeroPratica = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroPratica);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findById Ricovero: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Ricovero> findAll() {
        String sql = "SELECT NumeroPratica, DataInizioRicovero, DataDimissioneRicovero, MotivoRicovero, IdPaziente, CodiceUnivocoLetto  FROM Ricovero ORDER BY DataInizioRicovero DESC";
        List<Ricovero> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll Ricovero: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Ricovero> findInCorso() {
        String sql = "SELECT NumeroPratica, DataInizioRicovero, DataDimissioneRicovero, MotivoRicovero, IdPaziente, CodiceUnivocoLetto  FROM Ricovero WHERE DataDimissioneRicovero IS NULL OR DataDimissioneRicovero > NOW() ORDER BY DataInizioRicovero DESC";
        List<Ricovero> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findInCorso Ricovero: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<PazienteInDimissione> trovaRicoveriInDimissione(LocalDate data) {
        String sql = """
            SELECT P.IdPaziente, P.CodiceFiscale, P.Nome, P.Cognome,
                   R.NumeroPratica, R.CodiceUnivocoLetto, R.DataDimissioneRicovero
            FROM Ricovero R
            JOIN Paziente P ON P.IdPaziente = R.IdPaziente
            WHERE R.DataDimissioneRicovero::date = ?
            ORDER BY R.DataDimissioneRicovero
            """;
        List<PazienteInDimissione> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(data));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new PazienteInDimissione(
                        rs.getInt("IdPaziente"),
                        rs.getString("CodiceFiscale").trim(),
                        rs.getString("Nome"),
                        rs.getString("Cognome"),
                        rs.getString("NumeroPratica"),
                        rs.getString("CodiceUnivocoLetto"),
                        rs.getTimestamp("DataDimissioneRicovero").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore trovaRicoveriInDimissione: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(Ricovero r) {
        String sql = "INSERT INTO Ricovero (NumeroPratica, DataInizioRicovero, DataDimissioneRicovero, MotivoRicovero, IdPaziente, CodiceUnivocoLetto) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getNumeroPratica());
            ps.setTimestamp(2, Timestamp.valueOf(r.getDataInizioRicovero()));
            if (r.getDataDimissioneRicovero() != null)
                ps.setTimestamp(3, Timestamp.valueOf(r.getDataDimissioneRicovero()));
            else
                ps.setNull(3, Types.TIMESTAMP);
            ps.setString(4, r.getMotivoRicovero());
            ps.setInt(5, r.getIdPaziente());
            ps.setString(6, r.getCodiceUnivocoLetto());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore insert Ricovero: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Ricovero r) {
        String sql = "UPDATE Ricovero SET DataInizioRicovero = ?, DataDimissioneRicovero = ?, MotivoRicovero = ?, IdPaziente = ?, CodiceUnivocoLetto = ? WHERE NumeroPratica = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(r.getDataInizioRicovero()));
            if (r.getDataDimissioneRicovero() != null)
                ps.setTimestamp(2, Timestamp.valueOf(r.getDataDimissioneRicovero()));
            else
                ps.setNull(2, Types.TIMESTAMP);
            ps.setString(3, r.getMotivoRicovero());
            ps.setInt(4, r.getIdPaziente());
            ps.setString(5, r.getCodiceUnivocoLetto());
            ps.setString(6, r.getNumeroPratica());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore update Ricovero: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String numeroPratica) {
        String sql = "DELETE FROM Ricovero WHERE NumeroPratica = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroPratica);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete Ricovero: " + e.getMessage(), e);
        }
    }
}
