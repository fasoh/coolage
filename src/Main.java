import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.*;
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

        new DetectFaceDemo().run();
    }
}


//
// Detects faces in an image, draws boxes around them, and writes the results
// to "faceDetection.png".
//
class DetectFaceDemo {
    public void run() {
        System.out.println("\nRunning DetectFaceDemo");

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

        try {
            drawLetterTest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawLetterTest() throws IOException {
        System.out.print("Applying demo letter on image ... ");

        File imageFile = new File(System.getProperty("user.dir") + "/faceDetection.png");
        BufferedImage img = ImageIO.read(imageFile);

        Graphics2D graph = img.createGraphics();
        graph.setColor(Color.BLACK);
        graph.fill(new Rectangle(0, 0, (img.getWidth() / 2) - 150, (img.getHeight())));
        graph.fill(new Rectangle((img.getWidth()/2)+150, 0, (img.getWidth() / 2), (img.getHeight())));
        graph.fill(new Rectangle((img.getWidth()/2)-50, 0, 100, (img.getHeight()/2)-50));
        graph.fill(new Rectangle((img.getWidth()/2)-50, (img.getHeight()/2)+50, 100, (img.getHeight()/2)));

        graph.dispose();

        ImageIO.write(img, "jpg", new File(System.getProperty("user.dir") + "/faceDetection.png"));

        System.out.println("Success!");
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
