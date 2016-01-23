package de.uniOldenburg.presenter;

import de.uniOldenburg.model.ImageProcessor;
import de.uniOldenburg.model.ResourceLoader;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@WebSocket
public class CoolageSocketListener {
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {

        JSONObject jsonMessage = new JSONObject(message);

        if (jsonMessage.has("text") && jsonMessage.has("images") &&
                !jsonMessage.getString("text").isEmpty() &&
                !jsonMessage.getString("images").isEmpty()) {

            String text = jsonMessage.get("text").toString();
            String allImages = jsonMessage.get("images").toString();

            ResourceLoader resourceLoader = new ResourceLoader();
            ArrayList<String> urlList = new ArrayList<String>();
            String[] imagesArray = allImages.split(";");
            for (String image : imagesArray) {
                urlList.add("https://process.filestackapi.com/AhTgLagciQByzXpFGRI0Az/resize=width:620,height:414,fit:max/" + image);
            }
            String fontUrl = "https://fonts.gstatic.com/s/raleway/v9/PKCRbVvRfd5n7BTjtGiFZMDdSZkkecOE1hvV7ZHvhyU.ttf";
            //TODO Für leerzeichen muss das bild nicht geladen werden (im converter) (bsp "a b c" lädt 5 bilder runter, nicht 3)
            ImageProcessor imageProcessor = new ImageProcessor(fontUrl, 400f, Color.WHITE, 2f, Color.BLACK, 15, session);
            imageProcessor.processImages(resourceLoader.getImages(text, urlList), text);

            System.out.println("Done!");
        }
    }
}