package bg.softuni.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private final String CONNECTION_STRING =
            "jdbc:mysql://localhost:3306/minions?useUnicode" +
                    "=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private final String username = "root";
    private final String password = "mysql123";

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(CONNECTION_STRING, username, password);
        } catch (SQLException ex) {
            throw new RuntimeException("Error connecting to the database", ex);
        }
    }
}
