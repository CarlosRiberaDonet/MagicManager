/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import Controllers.CardController;
import Controllers.UserController;
import Domain.Card;
import Utils.MisMetodos;
import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author sovi8
 */
public class CardDetailBuilder {

    private Card card;
    // Configuración de botones
    JButton linkButton;
    JButton addCardButton;
    JButton removeCardButton;

    public CardDetailBuilder(Card card) {
        this.card = card;
    }

    // Método para crear el panel de imagen
    public JPanel createImagePanel() {
        JPanel imagePanel = new JPanel(new BorderLayout());
        JLabel imageLabel = new JLabel();
        
        // Cargar la imagen desde la URL proporcionada por la carta
        String imageUrl = card.getImageUrl();
        if (imageUrl != null) {
            try {
                ImageIcon imageIcon = new ImageIcon(new URL(imageUrl));
                Image scaledImage = imageIcon.getImage().getScaledInstance(250, 350, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            imageLabel.setText("Imagen no disponible");
        }

        imagePanel.add(imageLabel, BorderLayout.CENTER);
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        return imagePanel;
    }

    // Método para crear el panel con los detalles de la carta
    public JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        addCardButton = new JButton("Añadir a mi lista");
        removeCardButton = new JButton("Quitar carta de mi lista");
        linkButton = new JButton("Link");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Font boldFont = new Font("Arial", Font.BOLD, 14);

        // Añadir etiquetas de la carta al panel de detalles
        addLabelPair(rightPanel, gbc, "Nombre:", card.getName(), 0, boldFont);
        addLabelPair(rightPanel, gbc, "Rareza:", card.getRarity(), 1, boldFont);
        addLabelPair(rightPanel, gbc, "Núm. Coleccionista:", card.getCollector_number(), 2, boldFont);
        addLabelPair(rightPanel, gbc, "Edición:", card.getSet_name(), 3, boldFont);
        addLabelPair(rightPanel, gbc, "Idioma:", card.getLang(), 4, boldFont);
        addLabelPair(rightPanel, gbc, "Valor:", CardController.getRegularPrice(card), 5, boldFont);
        addLabelPair(rightPanel, gbc, "Valor FOIL:", CardController.getFoilPrice(card), 6, boldFont);
        addLabelPair(rightPanel, gbc, "Disponible FOIL:", card.isFoil() ? "Sí" : "No", 7, boldFont);

        

        // Botón "Abrir enlace" alineado a la derecha
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.EAST;
        rightPanel.add(linkButton, gbc);

        // Botón "Añadir a mi lista" alineado a la izquierda
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        rightPanel.add(addCardButton, gbc);

        // Botón "eliminar de mi lista" alineado a la izquierda
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        rightPanel.add(removeCardButton, gbc);

        // Lógica de visibilidad de los botones
        updateButtons();
        
        addCardButton.addActionListener(e ->{
           UserController.addCardToMap(card);
           updateButtons();
        });
        
        removeCardButton.addActionListener (e -> {
            UserController.removeCardFromMap(card);
            updateButtons();
        
        });
        
        linkButton.addActionListener(e -> MisMetodos.abrirURL(card.getNewUrl()));

        return rightPanel;
    }
    
    // Actualiza los botones de añadir y eliminar carta
    private void updateButtons(){
        if(isCardInUserList(card)){ 
            removeCardButton.setVisible(true);
            addCardButton.setVisible(false);            
        } else{
            removeCardButton.setVisible(false);
            addCardButton.setVisible(true);          
        }
    }

    // Método auxiliar para agregar pares de etiquetas y valores
    private void addLabelPair(JPanel panel, GridBagConstraints gbc, String labelText, String valueText, int gridY, Font boldFont) {
        JLabel label = new JLabel(labelText);
        label.setFont(boldFont);
        JLabel value = new JLabel(valueText);

        gbc.gridx = 0;
        gbc.gridy = gridY;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(value, gbc);
    }

    // Comprueba si la carta ya está añadida a userJson
    private boolean isCardInUserList(Card card) {
        return UserController.cardMap.containsKey(card.getCardId());
    }
}
