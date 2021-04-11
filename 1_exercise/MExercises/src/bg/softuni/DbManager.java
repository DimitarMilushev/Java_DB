package bg.softuni;

import bg.softuni.dto.Minion;
import bg.softuni.dto.Person;
import bg.softuni.dto.Town;
import bg.softuni.dto.Villain;
import bg.softuni.factory.PersonFactory;

import java.sql.*;
import java.util.Locale;

public class DbManager {

    private PersonFactory personFactory;

    private final Connection connection;
    //last used id
    private int lastMinionIndex;
    private int lastVillainIndex;
    private int lastTownIndex;

    public DbManager(Connection connection) {
        this.connection = connection;
        this.personFactory = new PersonFactory();
        this.lastMinionIndex = initIndex("minions");
        this.lastVillainIndex = initIndex("villains");
        this.lastTownIndex = initIndex("towns");
    }

    private int initIndex(String type) {
        final String getLastIdByType = "SELECT 1 FROM ? ORDER BY 1 DESC";

        try(PreparedStatement statement = connection.prepareStatement(getLastIdByType)) {
            statement.setString(1, type);

            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getInt("id");
            }

        } catch (SQLException throwables) {
            System.out.println("index initalizaton problem");
            throwables.printStackTrace();
        }

        return 0;
    }

    public void printVillainsNames() {
        final String getVillainsNamesQuery =
                """
                SELECT v.name, COUNT(*) AS 'count'
                FROM minions_villains AS mv
                JOIN villains AS v ON mv.villain_id = v.id
                GROUP BY villain_id
                HAVING count > 15
                ORDER BY count DESC
                """;

        try(PreparedStatement statement = connection.prepareStatement(getVillainsNamesQuery)) {

            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                System.out.printf("%s %d \n",
                        rs.getString("name"),
                        rs.getInt("count"));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void printMinionsNames(int villainId) {
        final String getMinionsQuery =
                """
                SELECT m.name, m.age
                FROM minions AS m
                JOIN minions_villains AS mv ON m.id = mv.minion_id
                WHERE mv.villain_id = ?
                """;

        try(PreparedStatement minionsStatement = connection.prepareStatement(getMinionsQuery);
            PreparedStatement villainStatement =
                    connection.prepareStatement("SELECT name FROM villains WHERE id = ?")) {

            minionsStatement.setInt(1, villainId);
            villainStatement.setInt(1, villainId);
            ResultSet rs = minionsStatement.executeQuery();
            ResultSet villainResult = villainStatement.executeQuery();

            if(!villainResult.next()) {
                System.out.println("No villain with id " + villainId);
            }
            else {
                System.out.println("Villain: " + villainResult.getString("name"));
                int count = 0;
                while(rs.next()) {
                    ++count;
                    System.out.printf("%d. %s %d \n",
                            count,
                            rs.getString("name"),
                            rs.getInt("age"));
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void addMinion(String minionInfo, String villainInfo) {

        try {
            Minion minion = (Minion) getPersonInfo(minionInfo);
            Villain villain = (Villain) getPersonInfo(villainInfo);



        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    private Person getPersonInfo(String info) throws SQLException {
        String[] tokens = info.split(" ");

        String type = tokens[0].substring(0, tokens[0].length() - 1).toLowerCase(Locale.ROOT);
        String name = tokens[1];
        int id = getPersonId(type, name);

        if(id < 0) {
            if(type.equals("minion")) {
                id = ++this.lastMinionIndex;
            }
            else if(type.equals("villain")) {
                id = ++this.lastVillainIndex;
            }
        }

        if(type.equals("minion")) {
            tokens[tokens.length - 1] = String.valueOf(getTown(tokens[tokens.length - 1]).getId());
        }

        tokens[0] = String.valueOf(id);

        return this.personFactory.createPerson(type, tokens);
    }


    private int getPersonId(String type, String name) throws SQLException {
        String selectPersonQuery = type.equals("minion") ? "SELECT id FROM minions WHERE name = ?" :
                "SELECT id FROM villain WHERE name = ?";
        PreparedStatement statement = connection.prepareStatement(selectPersonQuery);
        statement.setString(1, name);

        ResultSet rs = statement.executeQuery();

        if(rs.next()) {
            return rs.getInt("id");
        }

        return -1;
    }

    public Town getTown(String name) throws SQLException {
        String selectTownQuery = "SELECT id FROM towns WHERE name = ?";
        PreparedStatement statement = connection.prepareStatement(selectTownQuery);

        statement.setString(1, name);
        ResultSet rs = statement.executeQuery();

       if(rs.next()) {
           return new Town(rs.getInt("id"), name);
       }

       Town town = new Town(++this.lastTownIndex, name);
       addTown(town);
       System.out.println("town added!");

       return town;
    }

    private void addTown(Town town) throws SQLException {
        String insertQuery = "INSERT INTO towns VALUES(?, ?)";
        PreparedStatement statement = connection.prepareStatement(insertQuery);
        statement.setInt(1, town.getId());
        statement.setString(2, town.getName());

        statement.executeUpdate();
    }
}
