import org.opencv.core.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ResourceLoader loadResource = new ResourceLoader();

        ArrayList<String> urlList = new ArrayList<String>();
        urlList.add("http://i.imgur.com/nNAM4vu.jpg");
        urlList.add("http://i.imgur.com/yAY2OnG.jpg");
        urlList.add("http://i.imgur.com/IeC86Kx.jpg");
        urlList.add("http://i.imgur.com/guEE2hm.jpg");
        urlList.add("http://i.imgur.com/WTWvYcT.jpg");
        ArrayList<BufferedImage> bufferedImageList = loadResource.downloadImages(urlList);

        ImageProcessor imageProcessor = new ImageProcessor(bufferedImageList);
        imageProcessor.detectFaces();
        imageProcessor.drawLettersOnGeneratedImage("Hallo test", "Arial_Black.ttf", Color.WHITE, 340f, 2f, Color.BLACK, 15); //(text, fontFace, backgroundColor, fontSize, borderSize, borderColor, margin)

    }
}