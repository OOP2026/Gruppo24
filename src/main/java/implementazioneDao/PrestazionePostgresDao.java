package implementazioneDao;

import dao.DAOException;
import dao.PrestazioneDAO;
import database_connection.ConnessioneDatabase;
import model.Prestazione;
import model.TipoPrestazione;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrestazionePostgresDao implements PrestazioneDAO {

    private Prestazione mapRow(ResultSet rs) throws SQLException {
        String esitoRaw = rs.getString("Esito");
        return new Prestazione(
            rs.getString("NumeroPratica"),
            rs.getInt("NumeroPrestazione"),
            rs.getTimestamp("DataInizioPrestazione").toLocalDateTime(),
            rs.getTimestamp("DataFinePrestazione").toLocalDateTime(),
            esitoRaw != null ? esitoRaw : "",
            TipoPrestazione.valueOf(rs.getString("TipologiaPrestazione")),
            rs.getInt("IdMedico")
        );
    }

    @Override
    public Optional<Prestazione> findById(String numeroPratica, int numeroPrestazione) {
        String sql = "SELECT numeropratica,numeroprestazione,datainizioprestazione,datafineprestazione,esito,tipologiaprestazione,idmedico FROM Prestazione WHERE NumeroPratica = ? AND NumeroPrestazione = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroPratica);
            ps.setInt(2, numeroPrestazione);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findById Prestazione: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Prestazione> findAll() {
        String sql = "SELECT numeropratica,numeroprestazione,datainizioprestazione,datafineprestazione,esito,tipologiaprestazione,idmedico FROM Prestazione ORDER BY DataInizioPrestazione DESC";
        List<Prestazione> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll Prestazione: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Prestazione> findByMedico(int idMedico) {
        String sql = "SELECT numeropratica,numeroprestazione,datainizioprestazione,datafineprestazione,esito,tipologiaprestazione,idmedico FROM Prestazione WHERE IdMedico = ? ORDER BY DataInizioPrestazione DESC";
        List<Prestazione> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findByMedico Prestazione: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Prestazione> findByMedicoAndDate(int idMedico, LocalDate data) {
        String sql = "SELECT numeropratica,numeroprestazione,datainizioprestazione,datafineprestazione,esito,tipologiaprestazione,idmedico FROM Prestazione WHERE IdMedico = ? AND DataInizioPrestazione::date = ? ORDER BY DataInizioPrestazione";
        List<Prestazione> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setDate(2, Date.valueOf(data));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findByMedicoAndDate Prestazione: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(Prestazione p) {
        String nextNumSql = "SELECT COALESCE(MAX(NumeroPrestazione), 0) + 1 FROM Prestazione WHERE NumeroPratica = ?";
        String insertSql  = "INSERT INTO Prestazione (NumeroPratica, NumeroPrestazione, DataInizioPrestazione, DataFinePrestazione, Esito, TipologiaPrestazione, IdMedico) VALUES (?, ?, ?, ?, ?, ?::tipo_prestazione, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection()) {
            int nextNum;
            try (PreparedStatement ps = conn.prepareStatement(nextNumSql)) {
                ps.setString(1, p.getNumeroPratica());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    nextNum = rs.getInt(1);
                }
            }
            p.setNumeroPrestazione(nextNum);
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, p.getNumeroPratica());
                ps.setInt(2, p.getNumeroPrestazione());
                ps.setTimestamp(3, Timestamp.valueOf(p.getDataInizioPrestazione()));
                ps.setTimestamp(4, Timestamp.valueOf(p.getDataFinePrestazione()));
                if (p.getEsito() != null && !p.getEsito().isEmpty())
                    ps.setString(5, p.getEsito());
                else
                    ps.setNull(5, Types.VARCHAR);
                ps.setString(6, p.getTipologiaPrestazione().name());
                ps.setInt(7, p.getIdMedico());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore insert Prestazione: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Prestazione p) {
        String sql = "UPDATE Prestazione SET DataInizioPrestazione = ?, DataFinePrestazione = ?, Esito = ?, TipologiaPrestazione = ?::tipo_prestazione, IdMedico = ? WHERE NumeroPratica = ? AND NumeroPrestazione = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(p.getDataInizioPrestazione()));
            ps.setTimestamp(2, Timestamp.valueOf(p.getDataFinePrestazione()));
            if (p.getEsito() != null && !p.getEsito().isEmpty())
                ps.setString(3, p.getEsito());
            else
                ps.setNull(3, Types.VARCHAR);
            ps.setString(4, p.getTipologiaPrestazione().name());
            ps.setInt(5, p.getIdMedico());
            ps.setString(6, p.getNumeroPratica());
            ps.setInt(7, p.getNumeroPrestazione());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore update Prestazione: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String numeroPratica, int numeroPrestazione) {
        String sql = "DELETE FROM Prestazione WHERE NumeroPratica = ? AND NumeroPrestazione = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroPratica);
            ps.setInt(2, numeroPrestazione);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete Prestazione: " + e.getMessage(), e);
        }
    }

    @Override
    public void aggiornaEsito(String numeroPratica, int numeroPrestazione, String nuovoEsito) {
        String sql = "UPDATE Prestazione SET Esito = ? WHERE NumeroPratica = ? AND NumeroPrestazione = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuovoEsito);
            ps.setString(2, numeroPratica);
            ps.setInt(3, numeroPrestazione);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore aggiornaEsito Prestazione: " + e.getMessage(), e);
        }
    }
}
