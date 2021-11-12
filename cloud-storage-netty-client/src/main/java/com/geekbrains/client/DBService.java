package com.geekbrains.client;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DBService {
    private static final String DB_URL = "jdbc:sqlite:user_data.db";
    private static final String ADD_USER_REQUEST = "INSERT INTO accounts(username, login, password) VALUES(?, ?, ?)";
    private static final String GET_USERNAMES_REQUEST = "SELECT username FROM accounts";
    private static final String GET_LOGIN_AND_PASS_REQUEST = "SELECT login, password FROM accounts WHERE username = ?";
    private static final String REMOVE_USER = "DELETE FROM accounts WHERE username = ?";
    private static final String CHANGE_USERNAME_REQUEST = "UPDATE accounts SET username = ? WHERE username = ?";
    private Connection connection;
    private PreparedStatement getUsernameStatement;
    private PreparedStatement getLoginPassStatement;
    private PreparedStatement addUserStatement;
    private PreparedStatement removeUserStatement;
    private PreparedStatement changeUsernameStatement;

    public DBService() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            addUserStatement = connection.prepareStatement(ADD_USER_REQUEST);
            getUsernameStatement = connection.prepareStatement(GET_USERNAMES_REQUEST);
            getLoginPassStatement = connection.prepareStatement(GET_LOGIN_AND_PASS_REQUEST);
            removeUserStatement = connection.prepareStatement(REMOVE_USER);
            changeUsernameStatement = connection.prepareStatement(CHANGE_USERNAME_REQUEST);
            log.debug("database connected");
        } catch (SQLException e) {
            log.error("Failed to database connection");
        }
    }

    public void addNewUser(String username, String login, char[] password) {
        try {
            addUserStatement.setString(1, username);
            addUserStatement.setString(2, login);
            addUserStatement.setString(3, new String(password));
            addUserStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to database connection");
        }
    }

    public void removeUser(String username){
        try {
            removeUserStatement.setString(1, username);
            removeUserStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to database connection");
        }
    }

    public List<String> getUsernames() {
        List<String> usernames = new ArrayList<>();
        try {
            ResultSet resultSet = getUsernameStatement.executeQuery();
            while (resultSet.next()) {
               usernames.add(resultSet.getString("username"));
            }
        } catch (SQLException e) {
            log.error("Failed to database connection");
        }
        return usernames;
    }

    public List<String> getLoginPass(String username){
        List<String> result  = new ArrayList<>();
        try {
            getLoginPassStatement.setString(1, username);
            ResultSet resultSet = getLoginPassStatement.executeQuery();
            while (resultSet.next()) {
               result.add(resultSet.getString( 1));
               result.add(resultSet.getString(2));
            }
            return result;
        } catch (SQLException e) {
            log.error("Failed to database connection");
        }
        return  null;
    }

    public void changeUsername(String newUsername, String oldUsername) {
        try {
            changeUsernameStatement.setString(1, newUsername);
            changeUsernameStatement.setString(2, oldUsername);
            changeUsernameStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to database connection");
        }
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                log.info("Connection with DB closed.");
            }
        } catch (SQLException e) {
            log.error("Failed to close database connection");
        }
    }
}

