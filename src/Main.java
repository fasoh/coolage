import org.opencv.core.*;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        ImageProcessor imageProcessor = new ImageProcessor();

        imageProcessor.detectFace("http://i.imgur.com/nNAM4vu.jpg");
        imageProcessor.detectFace("http://i.imgur.com/yAY2OnG.jpg");
        imageProcessor.detectFace("http://i.imgur.com/IeC86Kx.jpg");
        imageProcessor.detectFace("http://i.imgur.com/guEE2hm.jpg");
        imageProcessor.detectFace("http://i.imgur.com/BoPi68u.jpg");

        imageProcessor.drawLettersOnGeneratedImage("TEAM1", Color.WHITE, 340f, 2f, 15); //(text, backgroundColor, fontSize, borderSize, margin)

    }
}