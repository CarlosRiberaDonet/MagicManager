/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package API;

import Domain.Card;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Random;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author sovi8
 */
public class PriceUpdater {

private static final Random random = new Random();
private static Document doc;
  
    public static void updateCardPrice(Card card) {
        
    try {
        ObjectMapper mapper = new ObjectMapper();
        String urlCard = card.getNewUrl();
        String cardPrice;
        
            try {
                // Descargo el html
                doc = fetchPriceFromWeb(card);
                // Intento obtener el precio directamente de la url de la carta
                cardPrice = updateRegularPrice(doc, card);
                // Si el campo precio != "empty"
                if(!cardPrice.isEmpty()){
                    // Si la url termina en &isFoil=Y
                    if(urlCard.contains("&isFoil=Y")){
                        // Obtengo el precio Foil
                        System.out.println("obteniendo precio foil");
                        card.setEurPriceFoil(cardPrice);
                        System.out.println("Precio Actualizado: " + card.getName() + " " + card.getEurPriceFoil());
                    } else{
                        // Obtengo el precio regular
                        System.out.println("obteniendo precio regular");
                        card.setEurPrice(cardPrice);
                    }
                } else{
                    // Intento obtener el precio buscando la carta mediante número coleccista
                    cardPrice = updatePriceFromList(doc,card);
                    if (!cardPrice.isEmpty()) {
                        card.setEurPrice(cardPrice);                   
                    }
                }     
            } catch (Exception e) {
                System.out.println("[EXCEPTION] " + card.getName() + ": " + e.getMessage());
            }
                Thread.sleep(3000 + random.nextInt(10000));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Precio Actualizado: " + card.getName() + " " + card.getEurPrice());
    }
    
    public static Document fetchPriceFromWeb(Card card) {
        
        try {
            // Obtener la URL de la carta
            String url = card.getNewUrl();

            // Conectar y descargar el HTML de la página
            doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .header("Accept-Language", "en-US,en;q=0.9")
            .header("Referer", "https://www.google.com")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .timeout(10000)
            .get();   
        } catch (IOException e) {
            // Si hay error (problemas de red, parseo, etc.), lo muestra en consola
            System.out.println("[ERROR] " + card.getName() + ": " + e.getMessage());
        }

        return doc;
    }
    
    public static String updateRegularPrice(Document doc, Card card){
        Elements dts = doc.select("dt.col-6.col-xl-5");
        for (Element dt : dts) {
            if (dt.text().trim().equals("From")) {
                Element dd = dt.nextElementSibling();
                if (dd != null && dd.hasClass("col-6") && dd.hasClass("col-xl-7")) {
                    String price = dd.text().trim();
                    return price;
                }
            }
        }

        return "N/D";
    }
    
    public static String updatePriceFromList(Document doc, Card card){ 
        // Selecciona todos los divs cuyo id comienza con "productRow", cada uno representa una edición de carta
        Elements rows = doc.select("div[id^=productRow]");

        // Recorre todas las filas encontradas
        for (Element row : rows) {

            // Extrae el número de coleccionista visible dentro de esta fila
            // Selector: entra a div.col-number > div > segundo <span>
            String collectorNumber = row
                .select("div.col-number > div > span:nth-of-type(2)")
                .text()
                .trim();

            // Compara con el número de coleccionista de la carta que estamos procesando
            if (collectorNumber.equals(card.getCollector_number())) {

                // Si coincide, extrae el precio de esta edición
                String price = row
                    .select("div.col-price")
                    .text()
                    .trim();
            // Devuelve el precio encontrado
            System.out.println(row.html());
            return price;
            }
        }
        
        return "N/D";
    }
    
//    private static int readLastIndex() {
//       if (!lastIndexFile.exists()) return 0;
//       try (BufferedReader br = new BufferedReader(new FileReader(lastIndexFile))) {
//           String line = br.readLine();
//           return line != null ? Integer.parseInt(line) : 0;
//       } catch (Exception e) {
//           return 0;
//       }
//    }
//    
//    private static void saveLastIndex(int index) {
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(lastIndexFile, false))) {
//            bw.write(String.valueOf(index));
//        } catch (IOException ignored) {}
//    } 
}
