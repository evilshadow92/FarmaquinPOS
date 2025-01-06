/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entidades;

import java.net.URL;
import javax.swing.ImageIcon;

/**
 *
 * @author Arturo_Maciel
 */
public class ResourceLoader {
        
    public static ImageIcon loadImageIcon(String path){
        URL resource = ResourceLoader.class.getResource(path);
        if (resource != null){
            return new ImageIcon(resource);
        }else{
            System.out.println("Imagen logo no encontrada ");
            return null;
            
        }
    }
    
    
        ImageIcon icon = new ImageIcon("C:\\Farmaquin _logo.png");
    
    
}
