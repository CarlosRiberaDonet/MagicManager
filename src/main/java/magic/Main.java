/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package magic;

import GUI.MainMenu;

/**
 *
 * @author Carlos Ribera
 */
public class Main {
    public static void main(String[] args){    
        MainMenu mainFrame = new MainMenu();

        // Centrar el frame en la pantalla
        mainFrame.setLocationRelativeTo(null);
        // Hacer visible el frame
        mainFrame.setVisible(true);
    }
}
