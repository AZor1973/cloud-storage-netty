package com.geekbrains.server;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:users.db";
    private static final String GET_USERNAME_REQUEST = "SELECT user FROM users WHERE login = ? AND password = ?";
    private static final String CHANGE_USERNAME_REQUEST = "UPDATE users SET user = ? WHERE user = ?";
    private static final String ADD_NEW_USER_REQUEST = "INSERT INTO users(login, password, user) VALUES(?, ?, ?)";
    private static final String GET_LOGIN_AND_PASS_REQUEST = "SELECT login, password FROM users WHERE user = ?";
    private Connection connection;
    private PreparedStatement getUsernameStatement;
    private PreparedStatement changeUsernameStatement;
    private PreparedStatement addNewUserStatement;
    private PreparedStatement getLoginPassStatement;

    public DatabaseService() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            log.debug("database connected");
            getUsernameStatement = connection.prepareStatement(GET_USERNAME_REQUEST);
            changeUsernameStatement = connection.prepareStatement(CHANGE_USERNAME_REQUEST);
            addNewUserStatement = connection.prepareStatement(ADD_NEW_USER_REQUEST);
            getLoginPassStatement = connection.prepareStatement(GET_LOGIN_AND_PASS_REQUEST);
        } catch (SQLException e) {
            log.error("Failed to database connection");
        }
    }

    public String getUsernameByLoginAndPassword(String login, char[] password) {
        String username = null;
        try {
            getUsernameStatement.setString(1, login);
            getUsernameStatement.setString(2, new String(password));
            ResultSet resultSet = getUsernameStatement.executeQuery();
            while (resultSet.next()) {
                username = resultSet.getString("user");
            }
        } catch (SQLException e) {
            log.error("Failed to database connection");
        }
        return username;
    }

    public boolean changeUsername(String newUsername, String oldUsername) {
        try {
            changeUsernameStatement.setString(1, newUsername);
            changeUsernameStatement.setString(2, oldUsername);
            return changeUsernameStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Failed to database connection");
        }
        return false;
    }

    public boolean addNewUser(String username, String login, char[] password) {
        try {
            addNewUserStatement.setString(1, login);
            addNewUserStatement.setString(2, new String(password));
            addNewUserStatement.setString(3, username);
            return addNewUserStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Failed to database connection");
        }
        return false;
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
}
