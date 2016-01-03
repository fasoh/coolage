import org.opencv.core.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) {

        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ResourceLoader resourceLoader = new ResourceLoader();

        Dimension boundary = new Dimension(620, 414);

        ArrayList<String> urlList = new ArrayList<String>();
        urlList.add("https://i.imgur.com/FEPDZ8M.jpg");
        urlList.add("http://imgur.com/g6zYCMW.jpg");
        urlList.add("http://i.imgur.com/a2F7iyp.jpg");
        urlList.add("http://blogs.reuters.com/great-debate/files/2013/07/obama-best.jpg");
        urlList.add("https://i.imgur.com/OQuArU4.jpg");
        String fontUrl = "https://fonts.gstatic.com/s/raleway/v9/PKCRbVvRfd5n7BTjtGiFZMDdSZkkecOE1hvV7ZHvhyU.ttf";
        String text = "abcde";
        //TODO Für leerzeichen muss das bild nicht geladen werden (im converter) (bsp "a b c" lädt 5 bilder runter, nicht 3)

        ImageProcessor imageProcessor = new ImageProcessor(fontUrl, 400f, Color.WHITE, 2f, Color.BLACK, 15);
        try {
            imageProcessor.processImages(resourceLoader.getImages(text, urlList, boundary), text);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Done!");
    }
}