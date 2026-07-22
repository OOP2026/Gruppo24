package implementazione_dao;

import exceptions.DAOException;
import dao.PeriodoMalattiaDAO;
import database_connection.ConnessioneDatabase;
import model.FasciaOraria;
import model.Medico;
import model.PeriodoMalattia;
import model.PrestazioneScoperta;
import model.RiassegnazionePrestazione;
import model.RiassegnazioneTurno;
import model.TipoPrestazione;
import model.TurnoScoperto;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static utils.Messages.DB_ERROR_P0001;

public class PeriodoMalattiaPostgresDao implements PeriodoMalattiaDAO {

    private PeriodoMalattia mapRow(ResultSet rs) throws SQLException {
        return new PeriodoMalattia(
            rs.getString("CodiceCertificato"),
            rs.getDate("DataInizioMalattia").toLocalDate(),
            rs.getDate("DataFineMalattia").toLocalDate(),
            rs.getInt("IdMedico")
        );
    }

    private Medico mapMedico(ResultSet rs) throws SQLException {
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
    public Optional<PeriodoMalattia> findById(String codiceCertificato) {
        String sql = "SELECT CodiceCertificato, DataInizioMalattia, DataFineMalattia, IdMedico FROM Periodo_Malattia WHERE CodiceCertificato = ?";
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
        String sql = "SELECT CodiceCertificato, DataInizioMalattia, DataFineMalattia, IdMedico FROM Periodo_Malattia ORDER BY DataInizioMalattia DESC";
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
    public List<TurnoScoperto> trovaTurniScoperti(int idMedico, LocalDate inizio, LocalDate fine) {
        String turniSql =
            "SELECT ST.Data, ST.FasciaOraria, T.OraInizio, T.OraFine " +
            "FROM Svolge_Turno ST " +
            "JOIN Turno T ON T.Data = ST.Data AND T.FasciaOraria = ST.FasciaOraria " +
            "WHERE ST.IdMedico = ? AND ST.Data BETWEEN ? AND ? " +
            "ORDER BY ST.Data, ST.FasciaOraria";

        List<TurnoScoperto> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(turniSql)) {
            ps.setInt(1, idMedico);
            ps.setDate(2, Date.valueOf(inizio));
            ps.setDate(3, Date.valueOf(fine));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate    data   = rs.getDate("Data").toLocalDate();
                    FasciaOraria fascia = FasciaOraria.valueOf(rs.getString("FasciaOraria"));
                    LocalTime    oraIn  = rs.getTime("OraInizio").toLocalTime();
                    LocalTime    oraFin = rs.getTime("OraFine").toLocalTime();
                    List<Medico> candidati = candidatiPerTurno(conn, idMedico, data, fascia);
                    result.add(new TurnoScoperto(data, fascia, oraIn, oraFin, candidati));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore trovaTurniScoperti: " + e.getMessage(), e);
        }
        return result;
    }

    private List<Medico> candidatiPerTurno(Connection conn, int idMedico,
                                           LocalDate data, FasciaOraria fascia) throws SQLException {
        String sql =
            "SELECT M.IdMedico, M.Login, M.Password, M.Matricola, M.Nome, M.Cognome, M.IdReparto " +
            "FROM Medico M " +
            "WHERE M.IdReparto = (SELECT IdReparto FROM Medico WHERE IdMedico = ?) " +
            "  AND M.IdMedico <> ? " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM Svolge_Turno ST2 " +
            "      WHERE ST2.IdMedico = M.IdMedico AND ST2.Data = ? AND ST2.FasciaOraria = ?::fascia_oraria) " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM Periodo_Malattia PM " +
            "      WHERE PM.IdMedico = M.IdMedico AND PM.DataInizioMalattia <= ? AND PM.DataFineMalattia >= ?) " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM Prestazione P " +
            "      JOIN Turno T ON T.Data = ?::date AND T.FasciaOraria = ?::fascia_oraria " +
            "      WHERE P.IdMedico = M.IdMedico " +
            "        AND P.DataInizioPrestazione::date = ?::date " +
            "        AND P.DataInizioPrestazione::time < T.OraFine " +
            "        AND T.OraInizio < P.DataFinePrestazione::time) " +
            "ORDER BY M.Cognome, M.Nome";

        List<Medico> candidati = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setInt(2, idMedico);
            ps.setDate(3, Date.valueOf(data));
            ps.setString(4, fascia.name());
            ps.setDate(5, Date.valueOf(data));
            ps.setDate(6, Date.valueOf(data));
            ps.setDate(7, Date.valueOf(data));
            ps.setString(8, fascia.name());
            ps.setDate(9, Date.valueOf(data));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) candidati.add(mapMedico(rs));
            }
        }
        return candidati;
    }

    @Override
    public List<PrestazioneScoperta> trovaPrestazioniScoperte(int idMedico, LocalDate inizio, LocalDate fine) {
        String prestSql =
            "SELECT NumeroPratica, NumeroPrestazione, DataInizioPrestazione, DataFinePrestazione, TipologiaPrestazione " +
            "FROM Prestazione " +
            "WHERE IdMedico = ? AND DataInizioPrestazione::date BETWEEN ? AND ? " +
            "ORDER BY DataInizioPrestazione";

        List<PrestazioneScoperta> result = new ArrayList<>();
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(prestSql)) {
            ps.setInt(1, idMedico);
            ps.setDate(2, Date.valueOf(inizio));
            ps.setDate(3, Date.valueOf(fine));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String        np   = rs.getString("NumeroPratica");
                    int           n    = rs.getInt("NumeroPrestazione");
                    LocalDateTime di   = rs.getTimestamp("DataInizioPrestazione").toLocalDateTime();
                    LocalDateTime df   = rs.getTimestamp("DataFinePrestazione").toLocalDateTime();
                    TipoPrestazione tipo = TipoPrestazione.valueOf(rs.getString("TipologiaPrestazione"));
                    List<Medico> candidati = candidatiPerPrestazione(conn, idMedico, di, df);
                    result.add(new PrestazioneScoperta(np, n, di, df, tipo, candidati));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore trovaPrestazioniScoperte: " + e.getMessage(), e);
        }
        return result;
    }

    private List<Medico> candidatiPerPrestazione(Connection conn, int idMedico,
                                                 LocalDateTime inizioPrest, LocalDateTime finePrest) throws SQLException {
        String sql =
            "SELECT M.IdMedico, M.Login, M.Password, M.Matricola, M.Nome, M.Cognome, M.IdReparto " +
            "FROM Medico M " +
            "WHERE M.IdReparto = (SELECT IdReparto FROM Medico WHERE IdMedico = ?) " +
            "  AND M.IdMedico <> ? " +
            "  AND EXISTS ( " +
            "      SELECT 1 FROM Svolge_Turno ST " +
            "      JOIN Turno T ON T.Data = ST.Data AND T.FasciaOraria = ST.FasciaOraria " +
            "      WHERE ST.IdMedico = M.IdMedico " +
            "        AND T.Data = ?::date AND T.OraInizio <= ?::time AND T.OraFine >= ?::time) " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM Periodo_Malattia PM " +
            "      WHERE PM.IdMedico = M.IdMedico AND PM.DataInizioMalattia <= ?::date AND PM.DataFineMalattia >= ?::date) " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM Prestazione P " +
            "      WHERE P.IdMedico = M.IdMedico AND P.DataInizioPrestazione < ? AND P.DataFinePrestazione > ?) " +
            "ORDER BY M.Cognome, M.Nome";

        List<Medico> candidati = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setInt(2, idMedico);
            ps.setDate(3, Date.valueOf(inizioPrest.toLocalDate()));
            ps.setTime(4, Time.valueOf(inizioPrest.toLocalTime()));
            ps.setTime(5, Time.valueOf(finePrest.toLocalTime()));
            ps.setDate(6, Date.valueOf(inizioPrest.toLocalDate()));
            ps.setDate(7, Date.valueOf(inizioPrest.toLocalDate()));
            ps.setTimestamp(8, Timestamp.valueOf(finePrest));
            ps.setTimestamp(9, Timestamp.valueOf(inizioPrest));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) candidati.add(mapMedico(rs));
            }
        }
        return candidati;
    }

    @Override
    public void applicaRiassegnazioni(List<RiassegnazioneTurno> turni,
                                      List<RiassegnazionePrestazione> prestazioni) {
        Connection conn = null;
        try {
            conn = ConnessioneDatabase.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psDel = conn.prepareStatement(
                    "DELETE FROM Svolge_Turno WHERE IdMedico=? AND Data=? AND FasciaOraria=?::fascia_oraria");
                 PreparedStatement psIns = conn.prepareStatement(
                         "INSERT INTO Svolge_Turno (IdMedico, Data, FasciaOraria) VALUES (?, ?, ?::fascia_oraria)")) {

                for (RiassegnazioneTurno t : turni) {
                    psDel.setInt(1, t.idMedicoAssente());
                    psDel.setDate(2, Date.valueOf(t.data()));
                    psDel.setString(3, t.fasciaOraria().name());
                    psDel.addBatch();

                    psIns.setInt(1, t.idSostituto());
                    psIns.setDate(2, Date.valueOf(t.data()));
                    psIns.setString(3, t.fasciaOraria().name());
                    psIns.addBatch();
                }

                if (!turni.isEmpty()) {
                    psDel.executeBatch();
                    psIns.executeBatch();
                }
            }

            try (PreparedStatement psUp = conn.prepareStatement(
                    "UPDATE Prestazione SET IdMedico=? WHERE NumeroPratica=? AND NumeroPrestazione=?")) {

                for (RiassegnazionePrestazione p : prestazioni) {
                    psUp.setInt(1, p.idSostituto());
                    psUp.setString(2, p.numeroPratica());
                    psUp.setInt(3, p.numeroPrestazione());
                    psUp.addBatch();
                }

                if (!prestazioni.isEmpty()) {
                    psUp.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new DAOException("Errore durante l'applicazione delle riassegnazioni in batch: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                /* L'eccezione viene ignorata perché la connessione sta comunque per essere chiusa
                   e l'eventuale fallimento del ripristino dell'auto-commit non è critico in questa fase. */
                }
                try {
                    conn.close();
                } catch (SQLException ignored) {
                /* Il fallimento del close viene ignorato in quanto si tratta di un'operazione
                   di pulizia best-effort nel blocco finally. */
                }
            }
        }
    }

    @Override
    public boolean isMedicoInMalattia(int idMedico, LocalDate data) {
        String sql = "SELECT 1 FROM Periodo_Malattia " +
                "WHERE IdMedico = ? AND DataInizioMalattia <= ? AND DataFineMalattia >= ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setDate(2, Date.valueOf(data));
            ps.setDate(3, Date.valueOf(data));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DAOException("Errore isMedicoInMalattia: " + e.getMessage(), e);
        }
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
            String state = e.getSQLState();
            if (DB_ERROR_P0001.equals(state)) {
                throw new DAOException("Il medico ha già un periodo di malattia sovrapposto a quello inserito. Modificare le date.", e);
            } else if ("23505".equals(state)) {
                throw new DAOException("Esiste già un certificato con questo codice.", e);
            }
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
