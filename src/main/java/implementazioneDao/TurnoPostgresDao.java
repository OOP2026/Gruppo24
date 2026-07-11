package implementazioneDao;

import dao.DAOException;
import dao.TurnoDAO;
import database_connection.ConnessioneDatabase;
import model.FasciaOraria;
import model.Turno;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TurnoPostgresDao implements TurnoDAO {

    private Turno mapRow(ResultSet rs) throws SQLException {
        return new Turno(
            rs.getDate("Data").toLocalDate(),
            FasciaOraria.valueOf(rs.getString("FasciaOraria")),
            rs.getTime("OraInizio").toLocalTime(),
            rs.getTime("OraFine").toLocalTime()
        );
    }

    @Override
    public Optional<Turno> findById(LocalDate data, FasciaOraria fasciaOraria) {
        String sql = "SELECT * FROM Turno WHERE Data = ? AND FasciaOraria = ?::fascia_oraria";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, fasciaOraria.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findById Turno: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Turno> findAll() {
        String sql = "SELECT * FROM Turno ORDER BY Data, OraInizio";
        List<Turno> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DAOException("Errore findAll Turno: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public List<Turno> trovaAgendaMedico(int idMedico, LocalDate inizio, LocalDate fine) {
        String sql = "SELECT Data, FasciaOraria, OraInizio, OraFine FROM agenda_medico(?, ?, ?)";
        List<Turno> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setDate(2, Date.valueOf(inizio));
            ps.setDate(3, Date.valueOf(fine));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore trovaAgendaMedico: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void insert(Turno t) {
        String sql = "INSERT INTO Turno (Data, FasciaOraria, OraInizio, OraFine) VALUES (?, ?::fascia_oraria, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(t.getData()));
            ps.setString(2, t.getFasciaOraria().name());
            ps.setTime(3, Time.valueOf(t.getOraInizio()));
            ps.setTime(4, Time.valueOf(t.getOraFine()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore insert Turno: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Turno t) {
        String sql = "UPDATE Turno SET OraInizio = ?, OraFine = ? WHERE Data = ? AND FasciaOraria = ?::fascia_oraria";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTime(1, Time.valueOf(t.getOraInizio()));
            ps.setTime(2, Time.valueOf(t.getOraFine()));
            ps.setDate(3, Date.valueOf(t.getData()));
            ps.setString(4, t.getFasciaOraria().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore update Turno: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(LocalDate data, FasciaOraria fasciaOraria) {
        String sql = "DELETE FROM Turno WHERE Data = ? AND FasciaOraria = ?::fascia_oraria";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(data));
            ps.setString(2, fasciaOraria.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore delete Turno: " + e.getMessage(), e);
        }
    }


    public void assegnaTurno(int idMedico, LocalDate data, FasciaOraria fasciaOraria) {
        String sql = "INSERT INTO Svolge_Turno (IdMedico, Data, FasciaOraria) VALUES (?, ?, ?::fascia_oraria)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setDate(2, Date.valueOf(data));
            ps.setString(3, fasciaOraria.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore assegnaTurno: " + e.getMessage(), e);
        }
    }

}
