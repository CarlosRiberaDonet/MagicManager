/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import Controllers.CardController;
import Controllers.UserController;
import Domain.Card;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 *
 * @author sovi8
 */

public class CardTableMouseListener extends MouseAdapter {
    private static final int COLUMN_NAME = 1;

    private Map<Integer, Card> cardMap; // Mapa de cartas
    private JTable cardTable;
    private JFrame mainFrame; // Variable de instancia para el JFrame

    public CardTableMouseListener(Map<Integer, Card> cardMap, JTable cardTable, JFrame mainFrame) {
        this.cardMap = cardMap;
        this.cardTable = cardTable;
        this.mainFrame = mainFrame;
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        int row = cardTable.rowAtPoint(evt.getPoint());
        int column = cardTable.columnAtPoint(evt.getPoint());

        // Convertir el índice de la fila visible al índice del modelo original
        int modelRow = cardTable.convertRowIndexToModel(row);

        // Asegurarse de que se haya hecho clic en una fila válida
        if (row != -1) {
            // Obtener el ID de la carta de la fila seleccionada como Integer (de la columna 0, que está oculta)
            Integer cardId = (Integer) cardTable.getModel().getValueAt(modelRow, 0);
            Card selectedCard = cardMap.get(cardId);

            // Si la carta seleccionada es válida
            if (selectedCard != null) {
                // Si se hace clic en el nombre de la carta (columna específica del nombre)
                if (column == COLUMN_NAME) {
                    showCardDetails(cardId);
                }

            } else {
                JOptionPane.showMessageDialog(cardTable, "No se pudo encontrar la carta seleccionada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCardDetails(Integer cardId) {
    Card cardSelect = null;

    // Si la carta proviene del cardJson 
    if (cardMap == CardController.cardMap) {
        cardSelect = CardController.getCard(cardId,false);
        // Si la carta proviene del userJson
    } else if (cardMap == UserController.cardMap) {
        cardSelect = CardController.getCard(cardId, true);
    }

    if (cardSelect != null) {
        CardDetailDialog dialog = new CardDetailDialog(mainFrame, cardSelect);
        dialog.setVisible(true); // Pausa hasta que el diálogo se cierre

        // Tras cerrarse, actualiza el campo "Cant." en la tabla (si existe)
        if (cardTable.getModel().getColumnCount() > 8) {
            for (int i = 0; i < cardTable.getRowCount(); i++) {
                int rowId = (Integer) cardTable.getModel().getValueAt(i, 0);
                if (rowId == cardId) {
                    cardTable.getModel().setValueAt(cardSelect.getCardCount(), i, 8);
                    break;
                }
            }
        }
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Carta no encontrada");
        }
    }  
}



