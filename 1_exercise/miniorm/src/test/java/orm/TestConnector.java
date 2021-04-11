package orm;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.sql.Connection;

import static org.junit.jupiter.api.Assumptions.assumingThat;

public class TestConnector {
    private final String username = "root";
    private final String password = "mysql123";
    private final String dbName = "user_db";

    @Mock
    ;

    @Test
    public void testConnectionCreation() {
        assumingThat(connector);
    }
}
