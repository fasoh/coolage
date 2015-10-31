import org.opencv.core.*;

import java.awt.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ResourceLoader resourceLoader = new ResourceLoader();

        ArrayList<String> urlList = new ArrayList<String>();
        urlList.add("http://i.imgur.com/nNAM4vu.jpg");
        urlList.add("http://i.imgur.com/yAY2OnG.jpg");
        urlList.add("http://i.imgur.com/IeC86Kx.jpg");
        urlList.add("http://i.imgur.com/guEE2hm.jpg");
        urlList.add("http://i.imgur.com/WTWvYcT.jpg");

        ArrayList<Mat> matImageList = resourceLoader.downloadImages(urlList);
        String fontUrl = "https://fonts.gstatic.com/s/oswald/v10/bH7276GfdCjMjApa_dkG6SZ2oysoEQEeKwjgmXLRnTc.ttf";
        String text = "medienverarbeitung";
        resourceLoader.matchImageCountWithWordCount(text, matImageList);

        ImageProcessor imageProcessor = new ImageProcessor(matImageList);
        imageProcessor.processImages(text, fontUrl, Color.WHITE, 250f, 2f, Color.BLACK, 15); //(text, fontFace, backgroundColor, fontSize, borderSize, borderColor, margin)

        System.out.println("Done!");

    }
}