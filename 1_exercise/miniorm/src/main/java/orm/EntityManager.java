package orm;

import orm.annotation.Column;
import orm.annotation.Id;
import orm.annotation.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class EntityManager<E> implements DbContext<E>{
    private final String INSERT_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    private final String UPDATE_QUERY = "UPDATE %s SET %s WHERE %s";
    private final String SELECT_WITH_STAR = "SELECT * FROM %s";
    private final String UPDATE_SET = "%s = %s";
    private final String SUCCESS_QUERY = "Successfully executed query";

    private final Connection connection;

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    private Field getId(Class<?> entity) {
        return Arrays.stream(entity.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Entity does not have primary key"));
    }

    @Override
    public boolean persist(E entity) throws IllegalAccessException {
        Field primary = this.getId(entity.getClass());
        primary.setAccessible(true);
        Object value = primary.get(entity);

        try{
        if(value == null || (int)value <= 0) {
                return this.doInsert(entity);
        }
            return this.doUpdate(entity, primary);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.out.println(throwables.getMessage());

        }

        return false;
    }

    @Override
    public List<E> find(Class<E> table) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Statement statement = connection.createStatement();
        String query = String.format(
                SELECT_WITH_STAR,
                table.getAnnotation(Entity.class).name());

        ResultSet rs = statement.executeQuery(query);
        E entity;
        List<E> entities = new ArrayList<>();
        while(rs.next()) {
            entity = table.getDeclaredConstructor().newInstance();
            this.fillEntity(table, rs, entity);
            entities.add(entity);
        }

        return entities;
    }

    @Override
    public List<E> find(Class<E> table, String where) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Statement statement = connection.createStatement();
        String query = String.format(
                SELECT_WITH_STAR,
                table.getAnnotation(Entity.class).name(),
                where != null ? " AND " + where : "");

        ResultSet rs = statement.executeQuery(query);
        E entity;
        List<E> entities = new ArrayList<>();
        while(rs.next()) {
            entity = table.getDeclaredConstructor().newInstance();
            this.fillEntity(table, rs, entity);
            entities.add(entity);
        }

        return entities;
    }

    @Override
    public E findFirst(Class<E> table) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Statement statement = connection.createStatement();
        String query = String.format(
                SELECT_WITH_STAR + " LIMIT 1",
                table.getAnnotation(Entity.class).name());

        ResultSet rs = statement.executeQuery(query);
        E entity = table.getDeclaredConstructor().newInstance();
        rs.next();
        this.fillEntity(table, rs, entity);
        return entity;
    }

    @Override
    public E findFirst(Class<E> table, String where) throws SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Statement statement = connection.createStatement();
        String query = String.format(
                SELECT_WITH_STAR + " WHERE 1 %s LIMIT 1",
                table.getAnnotation(Entity.class).name(),
                where != null ? " AND " + where : "");

        ResultSet rs = statement.executeQuery(query);
        E entity = table.getDeclaredConstructor().newInstance();
        rs.next();
        this.fillEntity(table, rs, entity);
        return entity;
    }
    private void fillEntity(Class<E> table, ResultSet rs, E entity) throws SQLException, IllegalAccessException {

        Field[] declaredFields = table.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            fillField(field, entity, rs,
                    field.isAnnotationPresent(Id.class)
                            ? "id" : field.getAnnotation(Column.class).name());
        }
    }

    private void fillField(Field field, E entity, ResultSet rs, String name) throws SQLException, IllegalAccessException {
        field.setAccessible(true);
        switch (name) {
            case "id" -> field.set(entity, rs.getInt("id"));
            case "username", "password" -> field.set(entity, rs.getString(name));
            case "age" -> field.set(entity, rs.getInt("age"));
            case "registrationDate" -> field.set(entity, rs.getString("registration_date"));
        }
    }


    private boolean doUpdate(E entity, Field primary) throws IllegalAccessException, SQLException {
        String table = getTableName(entity);
        String setValues = getFieldsValues(entity);
        String setCondition = primary.getName() + " = " + primary.get(entity).toString();
        String query = String.format(this.UPDATE_QUERY , table, setValues, setCondition);

        PreparedStatement statement
                = connection.prepareStatement(query);

        System.out.println(this.SUCCESS_QUERY + " " + query);
        return statement.execute();
    }

    private boolean doInsert(E entity) throws SQLException {
        String table = getTableName(entity);
        String columns = getFields(entity);
        String values = getValues(entity);
        String query = String.format(this.INSERT_QUERY, table, columns, values);

        PreparedStatement statement =
                connection.prepareStatement(query);

        System.out.println(this.SUCCESS_QUERY + " " + query);
        return statement.execute();
    }

    private String getTableName(E entity) {

        Entity table = entity.getClass().getAnnotation(Entity.class);

        if(table == null) {
            throw new UnsupportedOperationException("Entity does not have primary key");
        }
        return table.name();
    }

    private String getFields(E entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .map(x -> {
                    x.setAccessible(true);
                    return x.getAnnotation(Column.class).name();
                }).collect(joining(", "));
    }

    private String getValues(E entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .map(x -> {
                    try {
                        x.setAccessible(true);
                        return x.getType() == String.class || x.getType() == LocalDate.class
                                ? "'" + x.get(entity) + "'" : x.get(entity).toString();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new IllegalArgumentException("Cannot resolve " + entity.toString());
                    }
                }).collect(joining(", "));
    }

    private String getFieldsValues(E entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(x -> x.isAnnotationPresent(Column.class))
                .map(x -> {
                    try {
                        x.setAccessible(true);
                        return String.format(this.UPDATE_SET,
                                x.getAnnotation(Column.class).name(),
                                x.getType() == String.class || x.getType() == LocalDate.class
                                        ? "'" + x.get(entity) + "'" : x.get(entity).toString());
                    }
                    catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new IllegalArgumentException("Cannot resolve " + entity.toString());
                    }
                }).collect(joining(", "));
    }
}
