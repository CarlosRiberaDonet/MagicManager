/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package API;

import Domain.Card;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    
private static final File jsonFile = new File("D:/Proyectos/MagicManager/newCards.json");
private static final Random random = new Random();
  
    public static void updateCardPrices(Card card) {
    try {
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("[INFO] Obteniedno precio de la carta: " + card.getName());

            String eurPrice = card.getEurPrice();
            try {
                String fetchedPrice = fetchPriceFromWeb(card);
                if (!fetchedPrice.isEmpty()) {
                    card.setEurPrice(fetchedPrice);
                    // mapper.writerWithDefaultPrettyPrinter().writeValue(updatedCardsFile, updatedCards);
                    System.out.println("Precio Actualizado: " + card.getName() + " " + card.getEurPrice());
                }
            } catch (Exception e) {
                System.out.println("[EXCEPTION] " + card.getName() + ": " + e.getMessage());
            }
                Thread.sleep(3000 + random.nextInt(10000));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String fetchPriceFromWeb(Card card) {
        try {
            // Obtener la URL de la carta
            String url = card.getNewUrl();

            // Conectar y descargar el HTML de la página
            Document doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .header("Accept-Language", "en-US,en;q=0.9")
            .header("Referer", "https://www.google.com")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .timeout(10000)
            .get();

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
                return price;
            }
        }
    } catch (Exception e) {
        // Si hay error (problemas de red, parseo, etc.), lo muestra en consola
        System.out.println("[ERROR] " + card.getName() + ": " + e.getMessage());
    }

        // Si no se encuentra el precio o hay error, devuelve cadena vacía
        return "";
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
