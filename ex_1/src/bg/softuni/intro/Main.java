package bg.softuni.intro;

import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    static String CONNECTION_STRING = "jdbc:mysql://localhost:3306/soft_uni";

    public static void main(String[] args) throws SQLException {
	Scanner sc = new Scanner(System.in);

	String username = sc.next();
	username = username.equals("") ? "root" : username;

	String password = sc.next().trim();

	Properties props = new Properties();
	props.setProperty("user", username);
	props.setProperty("password", password);

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(CONNECTION_STRING, props);
            statement = connection
                    .prepareStatement("SELECT * FROM employees WHERE salary > ?");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }



        String salary = sc.next();
        try {
            statement.setDouble(1, Double.parseDouble(salary));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        ResultSet resultSet = null;
        try {
            resultSet = statement.executeQuery();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        while(resultSet.next()) {
            System.out.printf("|%-15.15s | %-15.15s | %15.2f| \n",
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getDouble("salary"));
        }

        connection.close();
    }
}
