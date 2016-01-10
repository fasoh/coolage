package de.uniOldenburg.presenter;

import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import de.uniOldenburg.model.*;
import org.opencv.core.*;


@WebServlet("/api/getCoolage")
public class CoolageServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        JSONObject requestData = getJSONData(request.getInputStream());

        response.getWriter().write(requestData.getJSONArray("images").toString());

        /*
        ResourceLoader resourceLoader = new ResourceLoader();
        Dimension boundary = new Dimension(620, 414);
        ArrayList<String> urlList = new ArrayList<String>();

        urlList.add("http://lh5.ggpht.com/fkOpCsPe9PNSEjcCzmWpbHbtOJ3MuMCujcj9jWmAwrE3KwoXTjQb5Eq4IGOSgWO2fEf9HP2rceZgh1d7=s620");
        urlList.add("http://knowingtechnologies.com/wp-content/uploads/2015/06/iStock_000046819354_Medium-600x400.jpg");
        urlList.add("http://lh4.ggpht.com/8MCTX5uAr5bJxXzVdkk-vETHKTHQQZVp9dl9xqaaBNcTfvjkl8P05elW7f1aSf5Gd_ewxs4Wpgcjqb-Xfsk=s620");
        urlList.add("http://blogs.reuters.com/great-debate/files/2013/07/obama-best.jpg");
        urlList.add("http://stockfresh.com/files/g/goce/m/98/5167439_stock-photo-green-barley-field-nature-background.jpg");
        String fontUrl = "https://fonts.gstatic.com/s/raleway/v9/PKCRbVvRfd5n7BTjtGiFZMDdSZkkecOE1hvV7ZHvhyU.ttf";
        String text = "tes";
        //TODO Für leerzeichen muss das bild nicht geladen werden (im converter) (bsp "a b c" lädt 5 bilder runter, nicht 3)

        ImageProcessor imageProcessor = new ImageProcessor(fontUrl, 400f, Color.WHITE, 2f, Color.BLACK, 15);
        try {
            imageProcessor.processImages(resourceLoader.getImages(text, urlList, boundary), text);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Done!");

        response.getWriter().write("<html><body>GET response</body></html>");

        */
    }

    private JSONObject getJSONData(ServletInputStream stream) throws IOException {
        String input = getStringFromInputStream(stream);

        if (input.equals("")) {
            return new JSONObject();
        } else {
            return new JSONObject(new JSONTokener(input));
        }
    }

    private String getStringFromInputStream(ServletInputStream stream) throws IOException {
        String inputString = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        for (String buffer; (buffer = in.readLine()) != null; inputString += buffer + "\n") ;

        return inputString;
    }
}