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
    }
}