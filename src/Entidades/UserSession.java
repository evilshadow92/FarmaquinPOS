/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

/**
 *
 * @author Arturo_Maciel
 */
public class UserSession {
    private static UserSession instance;
    private int IdEmployee;

    private UserSession() {
        // Constructor privado para evitar instanciación
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public int getUsername() {
        return IdEmployee;
    }

    public void setUsername(int IdEmployee) {
        this.IdEmployee = IdEmployee;
    }    
}
