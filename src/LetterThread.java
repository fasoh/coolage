import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.*;
import org.opencv.core.Point;

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
    Converter converter = new Converter();
    public final static Object syncObject = new Object();

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
            //tresholdTest(rawImage);

            double scale = 1;
            int accuracyTiles = 10;
            double quality = 0;

            if (text.charAt(glyphCounter) != ' ') {
                int[] bestCoordinates = getBestCoordinates(accuracyTiles);
                photoGlyph = this.getPhotoGlyph(rawImage, text.charAt(glyphCounter), scale, bestCoordinates[0], bestCoordinates[1]);
                photoGlyph = cropImage(photoGlyph, margin);
                quality = getQualityOfPosition(rawImage, text.charAt(glyphCounter), scale, bestCoordinates[0], bestCoordinates[1]);
                System.out.println("Letter " + text.charAt(glyphCounter) + " at position " + (glyphCounter+1) + " contains " + amountOfFaces + " face/s and has a quality of " + quality);
            } else {
                photoGlyph = new BufferedImage(150, 1, BufferedImage.TYPE_INT_ARGB); //Create new image for empty space in text (width, height)
                System.out.println("Empty space at position " + (glyphCounter+1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return photoGlyph;
    }

    public int[] getBestCoordinates(int accuracyTiles){
        double bestQuality = 0;
        int bestX = 0;
        int bestY = 0;

        int stepsX = rawImage.getWidth()/accuracyTiles;
        int stepsY = rawImage.getHeight()/accuracyTiles;

        for (int x = 0; x < rawImage.getWidth(); x += stepsX){
            for (int y = 0; y < rawImage.getHeight(); y += stepsY){
                double newQuality = getQualityOfPosition(rawImage, text.charAt(glyphCounter), 1, x, y);
                if (newQuality != 0 && newQuality > bestQuality){
                    bestQuality = newQuality;
                    bestX = x;
                    bestY = y;
                }
            }
        }

        int[] bestCoordinates = new int[] {bestX, bestY};
        return bestCoordinates;
    }

    public BufferedImage getPhotoGlyph(BufferedImage buffImage, Character letter, double imageScale, int offsetX, int offsetY) {
        synchronized (syncObject) {
            BufferedImage textImage;
            if (letter == '\u00c4' || letter == '\u00d6' || letter == '\u00dc') { //ä,ö,ü
                letter = letter.toString().toLowerCase().charAt(0);
            }

            int scaleX = (int) (buffImage.getWidth() * imageScale);
            int scaleY = (int) (buffImage.getHeight() * imageScale);

            textImage = new BufferedImage(scaleX, scaleY, BufferedImage.TYPE_INT_ARGB);
            Graphics2D letterImage = textImage.createGraphics();
            FontRenderContext frc = letterImage.getFontRenderContext();

            GlyphVector glyphVector = font.createGlyphVector(frc, letter.toString());
            Rectangle2D glyphBox = glyphVector.getVisualBounds();
            int xOff = (int) glyphBox.getX();
            int yOff = (int) -glyphBox.getY();
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

            return textImage;
        }
    }

    private double getQualityOfPosition(BufferedImage buffImage, Character letter, double imageScale, int offsetX, int offsetY) {
        synchronized (syncObject) {
            BufferedImage qualityAreas = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Mat tempMat = new Mat(buffImage.getHeight(), buffImage.getWidth(), CvType.CV_8UC3);
            Rect[] faces = this.detectFaces(converter.BufferedToMat(converter.toBufferedImageOfType(buffImage, BufferedImage.TYPE_3BYTE_BGR)));

            Graphics2D canvas = qualityAreas.createGraphics();
            canvas.setColor(Color.RED);

            if (faces.length == 0) {
                return 0;
            } else {
                for (Rect face : faces) {
                    //canvas.fill(new Rectangle(face.x, face.y, face.width, face.height));
                    Point center= new Point(face.x + face.width*0.5, face.y + face.height*0.5 );
                    Imgproc.ellipse(tempMat, center, new Size(face.width * 0.5, face.height * 0.5), 0, 0, 360, new Scalar(0, 0, 255), -1);
                    //ellipse(Mat img, Point center, Size axes, double angle, double startAngle, double endAngle, Scalar color, int thickness)
                }

                qualityAreas = converter.MatToBuffered(tempMat);

                canvas.dispose();

                BufferedImage croppedQualityAres = this.getPhotoGlyph(qualityAreas, letter, imageScale, offsetX, offsetY);

                // Save image for visualisation
                /*try {
                    ImageIO.write(croppedQualityAres, "jpg", new File(System.getProperty("user.dir") + "/quality.jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                return ((double) this.countQualityPixels(croppedQualityAres) / (double) this.countQualityPixels(qualityAreas));
            }
        }
    }

    /**
     * Counts the number of red pixels in a given image
     * @param qualityAreas Quality Image
     * @return number of black pixels
     */
    private int countQualityPixels (BufferedImage qualityAreas) {
        synchronized (syncObject) {
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
    }

    public void tresholdTest(BufferedImage buffImage){
        synchronized (syncObject) {
            try {
                buffImage = converter.toBufferedImageOfType(buffImage, BufferedImage.TYPE_3BYTE_BGR);
                Mat sourceMat = converter.BufferedToMat(buffImage);
                Mat destinationMat = new Mat(sourceMat.rows(), sourceMat.cols(), sourceMat.type());
                destinationMat = sourceMat;

                Imgproc.cvtColor(sourceMat, destinationMat, Imgproc.COLOR_RGB2GRAY);

                Imgproc.threshold(sourceMat, destinationMat, 180, 255, Imgproc.THRESH_BINARY);
                //Imgproc.adaptiveThreshold(sourceMat, destinationMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 40);

                Imgproc.cvtColor(sourceMat, destinationMat, Imgproc.COLOR_GRAY2BGR);

                BufferedImage treshTest = converter.MatToBuffered(destinationMat);

                //treshTest = this.getPhotoGlyph(treshTest, text.charAt(glyphCounter), 0.7, 0, 0);

                ImageIO.write(treshTest, "jpg", new File(System.getProperty("user.dir") + "/treshold.jpg"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Rect[] detectFaces(Mat rawImage) {
        synchronized (syncObject) {
            MatOfRect faceDetections = new MatOfRect();
            CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.dir") + "/src/resources/lbpcascade_frontalface.xml");

            faceDetector.detectMultiScale(rawImage, faceDetections);

            amountOfFaces = faceDetections.toArray().length; //für system out

            return faceDetections.toArray();
        }
    }

    /** Crops the parts of an image that have the same color as the top left pixel
     * The algorithm checks the image pixel by pixel. It stops when the current pixel does NOT equal the top left pixel
     * Therefore it draws a rectangle over the letter
     * */
    public BufferedImage cropImage(BufferedImage source, int margin) {
        synchronized (syncObject) {
            int width = source.getWidth();
            int height = source.getHeight();

            int topY = Integer.MAX_VALUE, topX = Integer.MAX_VALUE;
            int bottomY = -1, bottomX = -1;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (source.getRGB(x, y) != 0) {
                        if (x < topX) topX = x;
                        if (y < topY) topY = y;
                        if (x > bottomX) bottomX = x;
                        if (y > bottomY) bottomY = y;
                    }
                }
            }

            //Create new BufferedImage to paste the cropped content on
            BufferedImage croppedImage = new BufferedImage((bottomX - topX + margin * 2),
                    (bottomY - topY + margin * 2), BufferedImage.TYPE_INT_ARGB);

            //Fill newly created image
            croppedImage.getGraphics().drawImage(source, 0, 0,
                    (croppedImage.getWidth()), (croppedImage.getHeight()),
                    topX - margin, topY - margin, bottomX + margin, bottomY + margin, null);

            return croppedImage;
        }
    }

}
