package implementazione_dao;

import dao.DAOException;
import dao.PeriodoMalattiaDAO;
import database_connection.ConnessioneDatabase;
import model.FasciaOraria;
import model.PeriodoMalattia;
import model.Sostituto;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PeriodoMalattiaPostgresDao implements PeriodoMalattiaDAO {

    private PeriodoMalattia mapRow(ResultSet rs) throws SQLException {
        return new PeriodoMalattia(
            rs.getString("CodiceCertificato"),
            rs.getDate("DataInizioMalattia").toLocalDate(),
            rs.getDate("DataFineMalattia").toLocalDate(),
            rs.getInt("IdMedico")
        );
    }

    @Override
    public Optional<PeriodoMalattia> findById(String codiceCertificato) {
        String sql = "SELECT codicecertificato,datainiziomalattia,datafinemalattia,idmedico FROM Periodo_Malattia WHERE CodiceCertificato = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codiceCertificato);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findById PeriodoMalattia: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PeriodoMalattia> findAll() {
        String sql = "SELECT codicecertificato,datainiziomalattia,datafinemalattia,idmedico FROM Periodo_Malattia ORDER BY DataInizioMalattia DESC";
        List<PeriodoMalattia> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll PeriodoMalattia: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Sostituto> trovaSostituti(int idMedico, LocalDate inizio, LocalDate fine) {
        String sql = "SELECT Data, FasciaOraria, OraInizio, OraFine, IdSostituto, NomeSostituto, CognomeSostituto FROM trova_sostituti(?, ?, ?)";
        List<Sostituto> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setDate(2, Date.valueOf(inizio));
            ps.setDate(3, Date.valueOf(fine));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Sostituto(
                        rs.getDate("Data").toLocalDate(),
                        FasciaOraria.valueOf(rs.getString("FasciaOraria")),
                        rs.getTime("OraInizio").toLocalTime(),
                        rs.getTime("OraFine").toLocalTime(),
                        rs.getInt("IdSostituto"),
                        rs.getString("NomeSostituto"),
                        rs.getString("CognomeSostituto")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore trovaSostituti: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(PeriodoMalattia pm) {
        String sql = "INSERT INTO Periodo_Malattia (CodiceCertificato, DataInizioMalattia, DataFineMalattia, IdMedico) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pm.getCodiceCertificato());
            ps.setDate(2, Date.valueOf(pm.getDataInizioMalattia()));
            ps.setDate(3, Date.valueOf(pm.getDataFineMalattia()));
            ps.setInt(4, pm.getIdMedico());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore insert PeriodoMalattia: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(PeriodoMalattia pm) {
        String sql = "UPDATE Periodo_Malattia SET DataInizioMalattia = ?, DataFineMalattia = ?, IdMedico = ? WHERE CodiceCertificato = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(pm.getDataInizioMalattia()));
            ps.setDate(2, Date.valueOf(pm.getDataFineMalattia()));
            ps.setInt(3, pm.getIdMedico());
            ps.setString(4, pm.getCodiceCertificato());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore update PeriodoMalattia: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String codiceCertificato) {
        String sql = "DELETE FROM Periodo_Malattia WHERE CodiceCertificato = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codiceCertificato);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete PeriodoMalattia: " + e.getMessage(), e);
        }
    }
}
