/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

//@author Arturo_Maciel

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/farmaquindb?zeroDateTimeBehavior=convertToNull";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    private DatabaseConnection() throws SQLException{
        try{
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch(SQLException e){
            throw new SQLException("Connection with database error: ",e);
        }
    }
    
    public static DatabaseConnection getInstance() throws SQLException{
        if(instance == null){
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection(){
        return connection;
    }
    
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
