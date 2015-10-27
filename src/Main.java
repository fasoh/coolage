import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class Main {
    public static void main(String[] args) {
        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        new DetectFace().run();
    }
}

// Detects faces in an image, draws boxes around them, and writes the results
// to "faceDetection.png".

class DetectFace {
    public void run() {
        System.out.println("\nRunning DetectFace");

        // Create a face detector from the cascade file in the resources
        // directory.
        CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.dir") + "/src/lbpcascade_frontalface.xml");
        BufferedImage buffImage = loadImageFromURL("http://i.imgur.com/nNAM4vu.jpg");
        Mat image = convertBufferedToMat(buffImage);


        // Detect faces in the image.
        // MatOfRect is a special container class for Rect.
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);

        System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

        // Draw a bounding box around each face.
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
        }

        // Save the visualized detection.
        String filename = "faceDetection.png";
        System.out.println(String.format("Writing %s", filename));
        Imgcodecs.imwrite(filename, image);
        System.out.println("Image saved under " + System.getProperty("user.dir") + "/" + filename);


        BufferedImage buffFaceDetectionImage = null;
        try {
            buffFaceDetectionImage = ImageIO.read(new File(System.getProperty("user.dir") + "/faceDetection.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        drawLetterTest(buffFaceDetectionImage, "ABC");

    }

    private void drawLetterTest(BufferedImage originalImage, String text){
        System.out.print("Applying text on image ... ");
        final BufferedImage textImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = textImage.createGraphics();
        FontRenderContext frc = g.getFontRenderContext();
        Font font = new Font(Font.MONOSPACED, Font.BOLD, 300);
        GlyphVector gv = font.createGlyphVector(frc, text);
        Rectangle2D box = gv.getVisualBounds();
        int xOff = 25+(int)-box.getX();
        int yOff = 80+ (int) -box.getY();
        Shape shape = gv.getOutline(xOff, yOff);
        g.setClip(shape);
        g.drawImage(originalImage, 0, 0, null);
        g.setClip(null);
        g.setStroke(new BasicStroke(2f));
        g.setColor(Color.BLACK);
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.draw(shape);

        g.dispose();

        try {
            ImageIO.write(textImage, "jpg", new File(System.getProperty("user.dir") + "/faceDetection.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print("Success!");
    }

    private BufferedImage loadImageFromURL(String urlString) {
        URL imageURL = null;
        BufferedImage img = null;
        System.out.print("Downloading picture from URL " + urlString + " ... ");

        try {
            imageURL = new URL(urlString);
            //Convert to BufferedImage of type BGR so openCV can read it
            img = toBufferedImageOfType(ImageIO.read(imageURL), BufferedImage.TYPE_3BYTE_BGR);
            System.out.println("Success!");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    public static BufferedImage toBufferedImageOfType(BufferedImage original, int type) {

        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() == type) {
            return original;
        }


        // Create a buffered image
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), type);

        // Draw the image onto the new buffer
        Graphics2D g = image.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(original, 0, 0, null);
        } finally {
            g.dispose();
        }

        return image;
    }

    public Mat convertBufferedToMat(BufferedImage buffImage) {
        System.out.print("Converting Image to Mat ... ");
        //Convert BufferedImage from URL to Mat for OpenCV
        byte[] pixels = ((DataBufferByte) buffImage.getRaster().getDataBuffer()).getData();
        // Create a Matrix the same size of image
        Mat imageMat = new Mat(buffImage.getHeight(), buffImage.getWidth(), CvType.CV_8UC3);
        // Fill Matrix with image values
        imageMat.put(0, 0, pixels);

        System.out.println("Success!");
        return imageMat;
    }




}
