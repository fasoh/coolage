package de.uniOldenburg.model;

import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by adrian-jagusch on 17.01.16.
 */
public class ProgressListener {

    private String text;
    private int countFinished = 0;
    private Session session;

    public ProgressListener(String text, Session session) {
        this.text = text;
        this.session = session;

        JSONObject status = new JSONObject();
        status.put("percentage", 0);
        status.put("task", "Coolage wird erstellt...");
        sendStatusToClient(status);
    }

    public void letterFinished(LetterResult letterResult) {

        this.countFinished++;
        double percentage = (double)countFinished/(double)text.length()*100;

        JSONObject status = new JSONObject();
        status.put("percentage", percentage);
        status.put("task", "Buchstabe " + letterResult.letter +
                " an Position " + letterResult.position +
                " enthält " + letterResult.numberOfFaces + " Gesicht(er)" +
                " und hat eine Qualität von " + (int)letterResult.quality + ".");

        sendStatusToClient(status);
    }

    private void sendStatusToClient(JSONObject status) {
        try {
            this.session.getRemote().sendString(status.toString());
        } catch (IOException e) {
            System.out.println("Verbindung zum Client verloren");
        }
    }
}
