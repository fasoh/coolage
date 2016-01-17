package de.uniOldenburg.presenter;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import javax.servlet.annotation.WebServlet;

@WebServlet(name = "Websocket", urlPatterns = {"/api/coolageSocket"})
public class CoolageSocket extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.getPolicy().setIdleTimeout(15 * 60 * 1000); // 15 * 60 * 1000ms --> 15 Minutes
        webSocketServletFactory.register(CoolageSocketListener.class);
    }
}