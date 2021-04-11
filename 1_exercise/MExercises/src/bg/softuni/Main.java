package bg.softuni;

import bg.softuni.factory.ConnectionFactory;

import java.sql.*;

public class Main {
    final static String CONNECTION_STRING = "jdbc:mysql://localhost:3306/minions_db";

    public static void main(String[] args) {
        //Scanner sc = new Scanner(System.in);
            ConnectionFactory connectionFactory = new ConnectionFactory();
            Connection connection = connectionFactory.getConnection();

            DbManager manager = new DbManager(connection);

            manager.addMinion("Minion: Carry 20 Eindhoven", "Villain: Jimmy");


    }
}
