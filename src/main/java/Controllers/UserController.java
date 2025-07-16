/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controllers;

import Domain.Card;
import Utils.MisMetodos;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sovi8
 */
public class UserController {
    
    public static String se = File.separator;
    public static String path = System.getProperty("user.dir");
    public static File userJson = new File(path + se + "userCards.json");
    public static Map<Integer, Card> cardMap = new HashMap<>();
    public static boolean flag = false; // Utilizo esta variable para saber si tengo que actualizar el userJson (se actualizará si flag = true)
    
    // Método para añadir una carta al userMap
    public static void addCardToMap(Card card){
        
        Card userCard = UserController.cardMap.get(card.getCardId());

        try{
            if(userCard != null){
                userCard.setCardCount(userCard.getCardCount() +1);
            } else{
                userCard = new Card (card.getCardId(), 1, card.getName(),
                    card.getPrinted_name(), card.getSet_name(), card.getLang(),
                    card.isFoil(), card.getRarity(), card.getCollector_number(), card.getEurPrice(),
                    card.getEurPriceFoil(), card.getNewUrl(), card.getImageUrl());

                    cardMap.put(userCard.getCardId(), userCard);
            }        
            // UpdateUserJson(cardMap);
            flag = true;
            
        } catch (Exception e){
             e.printStackTrace();
             System.err.println("Error al intentar añadir la carta a la lista: " + e.getMessage());
        }
    }
    
    public static void removeCardFromMap(Card card){
               
        Card userCard = UserController.cardMap.get(card.getCardId());
        
        try{
            // Compruebo que la carta está en userJson
            if(userCard != null){
                if(userCard.getCardCount() > 1){
                    userCard.setCardCount(userCard.getCardCount() -1);
                }else{
                    userCard.setCardCount(0);
                    cardMap.remove(userCard.getCardId());
                }
                flag = true;
            } 
            // UpdateUserJson(cardMap); 
           
            
        } catch(Exception e){
            System.err.println("Error al intentar eliminar la carta de la lista: " + e.getMessage());
        }
    }
    
    public static void UpdateUserJson(Map<Integer, Card> cardMap){
        ObjectMapper mapper = new ObjectMapper();    
        try{
            if(!userJson.exists()){
                MisMetodos.CreateFile(userJson);
            }
                // Actualizo el userJson
                mapper.writeValue(userJson, cardMap);
            } catch(Exception e){
                e.printStackTrace();
        }
    }   

}
