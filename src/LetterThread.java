import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by Jannik on 01.12.15.
 */

public class LetterThread implements Callable<BufferedImage> {

    private BufferedImage rawImage;
    private String text;
    private int glyphCounter = 0;
    private Font font;
    private float borderSize;
    private Color borderColor;
    private int margin;
    BufferedImage photoGlyph;
    CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.dir") + "/src/resources/lbpcascade_frontalface.xml");
    Converter converter = new Converter();

    //Für system outs
    private int amountOfFaces;

    /**
     * Konstruktor für alle Bilder nach dem ersten (die zum vorherigen gestitched werden sollen)
     */
    public LetterThread(BufferedImage rawImage, String text, int glyphCounter, Font font, float borderSize, Color borderColor, int margin) {
        this.rawImage = rawImage;
        this.text = text;
        this.glyphCounter = glyphCounter;
        this.font = font;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin= margin;
    }

    /**
     * Konstruktor für das allererste Bild (welches nicht gestitched werden soll)
     */
    public LetterThread(BufferedImage rawImage, String text, Font font, float borderSize, Color borderColor, int margin) {
        this.rawImage = rawImage;
        this.text = text;
        this.font = font;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin = margin;
    }

    /**
     * Methode, die für jeden Thread vom Callable aufgerufen wird (analog zu run() bei Threads)
     */
    public BufferedImage call() {

        try {
            photoGlyph = this.getPhotoGlyph(rawImage, text.charAt(glyphCounter), 0.7, 0, 0);
            double quality = getQualityOfPosition(rawImage, text.charAt(glyphCounter), 0.7, 0, 0);

            System.out.println("Letter " + text.charAt(glyphCounter) + " at position " + glyphCounter + " contains " + amountOfFaces + " face/s and has a quality of " + quality);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //tresholdTest(photoGlyph);

        return photoGlyph;
    }

    public BufferedImage getPhotoGlyph(BufferedImage buffImage, Character letter, double imageScale, int offsetX, int offsetY) {

        BufferedImage textImage;
        if (letter == '\u00c4' || letter == '\u00d6' || letter == '\u00dc'){ //ä,ö,ü
            letter = letter.toString().toLowerCase().charAt(0);
        }

        if (letter != ' '){
            int scaleX = (int)(buffImage.getWidth() * imageScale);
            int scaleY = (int)(buffImage.getHeight() * imageScale);

            textImage = new BufferedImage(scaleX, scaleY, BufferedImage.TYPE_INT_ARGB);
            Graphics2D letterImage = textImage.createGraphics();
            FontRenderContext frc = letterImage.getFontRenderContext();

            GlyphVector glyphVector = font.createGlyphVector(frc, letter.toString());
            Rectangle2D glyphBox = glyphVector.getVisualBounds();
            int xOff = (int)glyphBox.getX();
            int yOff = (int)-glyphBox.getY();
            Shape shape = glyphVector.getOutline(xOff + offsetX, yOff + offsetY);

            letterImage.setClip(shape); // Deactivate to see letter position in image

            Image scaledImage = buffImage.getScaledInstance(scaleX, scaleY, 1);

            letterImage.drawImage(scaledImage, 0, 0, null);
            letterImage.setClip(null);

            letterImage.setStroke(new BasicStroke(borderSize));
            letterImage.setColor(borderColor);
            letterImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            letterImage.draw(shape);

            letterImage.dispose();

            textImage = cropImage(textImage, margin);
        } else {
            textImage = new BufferedImage(150, 1, BufferedImage.TYPE_INT_ARGB); //Create new image for empty space in text (width, height)
        }

        return textImage;
    }

    private double getQualityOfPosition(BufferedImage buffImage, Character letter, double imageScale, int offsetX, int offsetY) {

        BufferedImage qualityAreas = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Rect[] faces = this.detectFaces(converter.BufferedToMat(buffImage));

        Graphics2D canvas = qualityAreas.createGraphics();
        canvas.setColor(Color.RED);

        if (faces.length == 0) {
            return 0;
        } else {
            for (Rect face : faces) {
                canvas.fill(new Rectangle(face.x, face.y, face.width, face.height));
            }
            canvas.dispose();

            BufferedImage croppedQualityAres = this.getPhotoGlyph(qualityAreas, letter, imageScale, offsetX, offsetY);

            // Save image for visualisation
            try {
                ImageIO.write(croppedQualityAres, "jpg", new File(System.getProperty("user.dir") + "/src/quality.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return ((double)this.countQualityPixels(croppedQualityAres) / (double)this.countQualityPixels(qualityAreas));
        }
    }

    /**
     * Counts the number of red pixels in a given image
     * @param qualityAreas Quality Image
     * @return number of black pixels
     */
    private int countQualityPixels (BufferedImage qualityAreas) {

        int photoWidth = qualityAreas.getWidth();
        int photoHeight = qualityAreas.getHeight();
        int qualityPixels = 0;

        for (int x = 0; x < photoWidth; x++) {
            for (int y = 0; y < photoHeight; y++) {
                if (qualityAreas.getRGB(x, y) == 0xFFFF0000) {
                    qualityPixels++;
                }
            }
        }
        return qualityPixels;
    }

   /* public void tresholdTest(BufferedImage buffImage){

        try{
            buffImage = converter.toBufferedImageOfType(buffImage, BufferedImage.TYPE_3BYTE_BGR);
            Mat source = converter.BufferedToMat(buffImage);
            Mat destination = new Mat(source.rows(),source.cols(),source.type());
            destination = source;

            Imgproc.threshold(source, destination, 180, 255, Imgproc.THRESH_TOZERO);

            ImageIO.write(converter.MatToBuffered(destination), "jpg", new File(System.getProperty("user.dir") + "/src/treshold.jpg"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public Rect[] detectFaces(Mat rawImage) { // Detects faces in an image, draws boxes around them, and writes the results to "faceDetection.png".

        // Detect faces in the image. MatOfRect is a special container class for Rect.
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(rawImage, faceDetections);

        amountOfFaces = faceDetections.toArray().length; //für system out

        return faceDetections.toArray();
    }

    /** Crops the parts of an image that have the same color as the top left pixel
     * The algorithm checks the image pixel by pixel. It stops when the current pixel does NOT equal the top left pixel
     * Therefore it draws a rectangle over the letter
     * */
    public BufferedImage cropImage(BufferedImage source, int margin) {

        int width = source.getWidth();
        int height = source.getHeight();

        int topY = Integer.MAX_VALUE, topX = Integer.MAX_VALUE;
        int bottomY = -1, bottomX = -1;
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                if (source.getRGB(x, y) != source.getRGB(0,0)) { //top left pixel (0,0) used as comparator
                    if (x < topX) topX = x;
                    if (y < topY) topY = y;
                    if (x > bottomX) bottomX = x;
                    if (y > bottomY) bottomY = y;
                }
            }
        }

        //Create new BufferedImage to paste the cropped content on
        BufferedImage croppedImage = new BufferedImage( (bottomX - topX + margin*2),
                (bottomY - topY + margin*2), BufferedImage.TYPE_INT_ARGB);

        //Fill newly created image
        croppedImage.getGraphics().drawImage(source, 0, 0,
                (croppedImage.getWidth()), (croppedImage.getHeight()),
                topX - margin, topY - margin, bottomX + margin, bottomY + margin, null);

        return croppedImage;
    }

}
