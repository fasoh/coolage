import org.opencv.core.*;

public class Main {
    public static void main(String[] args) {
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        ImageProcessor imageProcessor = new ImageProcessor();

        imageProcessor.detectFace("http://i.imgur.com/nNAM4vu.jpg");
        imageProcessor.drawLettersOnGeneratedImage("ABC");

    }
}