import entities.User;
import orm.Connector;
import orm.EntityManager;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String username = "root";
        String password = "mysql123";
        String dbName = "user_db";

        Connector.createConnection(username, password, dbName);
        EntityManager<User> em = new EntityManager<User>(Connector.getConnection());

        User testUser = new User
                ("asd", "hazarta123", 63, LocalDate.of(1994, 12, 15));
        testUser.setId(30);
        em.persist(testUser);
        List<User> found = em.find(User.class, "age > 12");
        System.out.println(found.toString());
    }
}