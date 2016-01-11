package de.uniOldenburg.presenter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import de.uniOldenburg.model.*;

@WebServlet("/api/getCoolage")
public class CoolageServlet extends HttpServlet {


    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String text =  request.getParameter("text");
        String allImages =  request.getParameter("images");

        ResourceLoader resourceLoader = new ResourceLoader();

        Dimension boundary = new Dimension(620, 414);

        ArrayList<String> urlList = new ArrayList<String>();

        String[] imagesArray = allImages.split(";");

        for (String image : imagesArray) {
            urlList.add(image);
        }

        String fontUrl = "https://fonts.gstatic.com/s/raleway/v9/PKCRbVvRfd5n7BTjtGiFZMDdSZkkecOE1hvV7ZHvhyU.ttf";
        //TODO Für leerzeichen muss das bild nicht geladen werden (im converter) (bsp "a b c" lädt 5 bilder runter, nicht 3)

        ImageProcessor imageProcessor = new ImageProcessor(fontUrl, 400f, Color.WHITE, 2f, Color.BLACK, 15);
        try {
            imageProcessor.processImages(resourceLoader.getImages(text, urlList, boundary), text);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Done!");

        response.getWriter().write(allImages);
    }
}