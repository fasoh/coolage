package de.uniOldenburg.presenter;

import de.uniOldenburg.model.ImageProcessor;
import de.uniOldenburg.model.ResourceLoader;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@WebSocket
public class CoolageSocketListener {

    ResourceLoader resourceLoader = new ResourceLoader();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {

        JSONObject jsonMessage = new JSONObject(message);

        String text = jsonMessage.get("text").toString();
        String allImages = jsonMessage.get("images").toString();

        ArrayList<String> urlList = new ArrayList<String>();

        if (jsonMessage.getBoolean("useExamples")) {
            urlList = resourceLoader.getExampleImages();
        } else {
            String[] imagesArray = allImages.split(";");
            for (String image : imagesArray) {
                urlList.add("https://process.filestackapi.com/AhTgLagciQByzXpFGRI0Az/resize=width:620,height:414,fit:max/" + image);
            }
        }

        String fontUrl = "file://" + System.getProperty("user.dir") + "/src/main/webapp/fonts/";
        if (jsonMessage.getString("font").equals("bitter")) {
            fontUrl += "Bitter-Bold.ttf";
        } else if (jsonMessage.getString("font").equals("coveredbyyourgrace")) {
            fontUrl += "CoveredByYourGrace.ttf";
        } else {
            fontUrl += "Raleway-Heavy.ttf";
        }

        ImageProcessor imageProcessor = new ImageProcessor(fontUrl, 400f, Color.WHITE, 2f, Color.BLACK, 15, session);
        imageProcessor.processImages(resourceLoader.getFittedImagesSources(text, urlList), text);

        System.out.println("Done!");
    }
}