/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Domain;

import java.util.Map;

/**
 *
 * @author sovi8
 */
public class Deck {
    
    private String nameDeck;
    private Map<Integer, Deck> cardMapDeck;     
    
    // CONSTRUCTOR
    public Deck(String nameDeck, Map<Integer, Deck> cardMapDeck) {    
        this.nameDeck = nameDeck;
        this.cardMapDeck = cardMapDeck;
    }

    // GETTERS Y SETTERS

    public String getNameDeck() {
        return nameDeck;
    }

    public void setNameDeck(String nameDeck) {
        this.nameDeck = nameDeck;
    }

    public Map<Integer, Deck> getCardMapDeck() {
        return cardMapDeck;
    }

    public void setCardMapDeck(Map<Integer, Deck> cardMapDeck) {
        this.cardMapDeck = cardMapDeck;
    }
   
}
