package orm;

import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.SQLException;

public class TestEntityManager {
    private final String username = "root";
    private final String password = "mysql123";
    private final String dbName = "user_db";

    private Connection connection;

    @BeforeAll
    public void setUp() throws SQLException {
        Connector.createConnection(username, password, dbName);
        this.connection = Connector.getConnection();
    }



}
