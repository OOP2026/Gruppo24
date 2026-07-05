package database_connection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class ConnessioneDatabase {

    private static final Logger LOG = Logger.getLogger(ConnessioneDatabase.class.getName());
    private static ConnessioneDatabase instance;

    private final String url;
    private final String user;
    private final String password;

    private ConnessioneDatabase() {
        Properties props = new Properties();
        try (InputStream is = ConnessioneDatabase.class
                .getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null)
                throw new RuntimeException(
                    "File db.properties non trovato nel classpath. "
                );
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile leggere db.properties: " + e.getMessage(), e);
        }
        this.url      = props.getProperty("db.url");
        this.user     = props.getProperty("db.user");
        this.password = props.getProperty("db.password");
    }

    public static synchronized ConnessioneDatabase getInstance() {
        if (instance == null) {
            instance = new ConnessioneDatabase();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
