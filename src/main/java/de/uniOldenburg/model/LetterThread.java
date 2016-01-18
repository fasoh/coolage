package de.uniOldenburg.model;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.*;
import org.opencv.core.Point;

import javax.imageio.ImageIO;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;


public class LetterThread implements Callable<LetterResult> {

    private BufferedImage photo;
    private char letter;
    private int index;
    private Font font;
    private float borderSize;
    private Color borderColor;
    private int margin;
    private ProgressListener progress;
    Converter converter = new Converter();
    public final static Object syncObject = new Object();

    /**
     * Konstruktor für alle Bilder nach dem ersten (die zum vorherigen gestitched werden sollen)
     */
    public LetterThread(BufferedImage photo, char letter, int index, Font font, float borderSize, Color borderColor, int margin, ProgressListener progress) {

        this.photo = photo;
        this.letter = letter;
        this.index = index;
        this.font = font;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin = margin;
        this.progress = progress;
    }

    /**
     * Methode, die für jeden Thread vom Callable aufgerufen wird (analog zu run() bei Threads)
     */
    public LetterResult call() {
        synchronized (syncObject) {

            LetterResult result;
            double scale = 1;
            int accuracy = 5; //Je höher, desto genauer
            BufferedImage photoGlyph;

            if (letter == ' ') {
                photoGlyph = new BufferedImage(150, 1, BufferedImage.TYPE_INT_ARGB); // Create new image for empty space in text (width, height)
                result = new LetterResult(photoGlyph, ' ', (index + 1), 0, 0);

            } else {
                Mat tempMat = converter.BufferedToMat(converter.toBufferedImageOfType(photo, BufferedImage.TYPE_3BYTE_BGR));
                Rect[] faces = detectFaces(tempMat);

                BufferedImage qualityAreas;

                if (faces.length == 0) {
                    qualityAreas = getTresholdImage(photo);
                } else {
                    qualityAreas = drawFaces(faces, photo.getWidth(), photo.getHeight());
                }

                BestPositionResult bestPositionResult = getBestCoordinates(qualityAreas, accuracy, letter);
                photoGlyph = getPhotoGlyph(photo, letter, scale, bestPositionResult.bestX, bestPositionResult.bestY);
                photoGlyph = cropImage(photoGlyph, margin);
                result = new LetterResult(photoGlyph, letter, (index + 1), faces.length, bestPositionResult.bestQuality*100);
            }

            progress.letterFinished(result);
            return result;
        }
    }

    public BestPositionResult getBestCoordinates(BufferedImage buffImage, int accuracyTiles, char character){
        synchronized (syncObject) {
            double bestQuality = 0;
            int bestX = 0;
            int bestY = 0;
            int[] dimensions = getFontDimensions(font, String.valueOf(character)); //TODO mit diesen Werten von Anfang an feststellen ob ein Bild zu klein ist oder nicht

            int accuracyX = (buffImage.getWidth()-dimensions[0]) / accuracyTiles;
            int accuracyY = (buffImage.getHeight()-dimensions[1]) / accuracyTiles;

            outerloop:
            for (int x = 0; x <= (buffImage.getWidth()-dimensions[0]); x += accuracyX) {
                for (int y = 0; y <= (buffImage.getHeight()-dimensions[1]); y += accuracyY) {
                    double newQuality = getQualityOfPosition(buffImage, letter, 1, x, y);
                    if (newQuality > bestQuality) {
                        bestQuality = newQuality;
                        bestX = x;
                        bestY = y;
                    }
                    if (newQuality == 1.0){
                        break outerloop;
                    }
                }
            }

            return new BestPositionResult(bestX, bestY, bestQuality);
        }
    }

    public int[] getFontDimensions(Font font, String letter){
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);

        Rectangle2D stringBounds = font.getStringBounds(letter, frc);

        int textWidth = (int)stringBounds.getWidth();
        int textHeight = (int)stringBounds.getWidth();
        int[] dimensions = {textWidth, textHeight};

        return dimensions;
    }

    public BufferedImage getPhotoGlyph(BufferedImage buffImage, Character letter, double imageScale, int offsetX, int offsetY) {
        synchronized (syncObject) {
            BufferedImage textImage;
            /*if (letter == '\u00c4' || letter == '\u00d6' || letter == '\u00dc') { //ä,ö,ü
                letter = letter.toString().toLowerCase().charAt(0);
            }*/

            int scaleX = (int) (buffImage.getWidth() * imageScale);
            int scaleY = (int) (buffImage.getHeight() * imageScale);

            textImage = new BufferedImage(scaleX, scaleY, BufferedImage.TYPE_INT_ARGB);
            Graphics2D letterImage = textImage.createGraphics();
            FontRenderContext frc = letterImage.getFontRenderContext();

            GlyphVector glyphVector = font.createGlyphVector(frc, letter.toString());
            Rectangle2D glyphBox = glyphVector.getLogicalBounds();
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

    private BufferedImage drawFaces(Rect[] faces, int canvasWidth, int canvasHeight) {
        BufferedImage canvas;
        Mat tempMat = new Mat(canvasWidth, canvasHeight, CvType.CV_8UC3, new Scalar(255, 255, 255));

        for (Rect face : faces) {
            Point center = new Point(face.x + face.width * 0.5, face.y + face.height * 0.5);
            Imgproc.ellipse(tempMat, center, new Size(face.width * 0.5, face.height * 0.5), 0, 0, 360, new Scalar(0, 0, 255), -1);
        }

        canvas = converter.MatToBuffered(tempMat);

        try {
            ImageIO.write(canvas, "jpg", new File(System.getProperty("user.dir") + "/faceDetectionTestOutput.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return canvas;
    }

    private double getQualityOfPosition(BufferedImage qualityImage, char letter, double imageScale, int offsetX, int offsetY) {
        synchronized (syncObject) {

            BufferedImage croppedQualityAreas = getPhotoGlyph(qualityImage, letter, imageScale, offsetX, offsetY);
            return (countQualityPixels(croppedQualityAreas) / countQualityPixels(qualityImage));
        }
    }

    /**
     * Counts the number of red pixels in a given image
     * @param qualityAreas Quality Image
     * @return number of black pixels
     */
    private double countQualityPixels (BufferedImage qualityAreas) {
        synchronized (syncObject) {
            int photoWidth = qualityAreas.getWidth();
            int photoHeight = qualityAreas.getHeight();
            double qualityPixels = 0;

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

    private BufferedImage swapBlackToRed(BufferedImage inputImage){
        synchronized (syncObject){
            int photoWidth = inputImage.getWidth();
            int photoHeight = inputImage.getHeight();
            //rot 0xFFFF0000

            for (int x = 0; x < photoWidth; x++) {
                for (int y = 0; y < photoHeight; y++) {
                    if (inputImage.getRGB(x, y) != -1) { //-1 ist weiss
                        inputImage.setRGB(x, y, 0xFFFF0000);
                    }
                }
            }

            return inputImage;
        }
    }

    public BufferedImage getTresholdImage(BufferedImage buffImage){
        synchronized (syncObject) {
            BufferedImage tresholdedImage = null;
            buffImage = converter.toBufferedImageOfType(buffImage, BufferedImage.TYPE_3BYTE_BGR);
            Mat sourceMat = converter.BufferedToMat(buffImage);
            Mat destinationMat = sourceMat;

            Imgproc.cvtColor(sourceMat, destinationMat, Imgproc.COLOR_RGB2GRAY);

            Imgproc.adaptiveThreshold(sourceMat, destinationMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 5);
            //Dokumentation zu adaptiveTreshold -  http://docs.opencv.org/2.4/modules/imgproc/doc/miscellaneous_transformations.html#adaptivethreshold

            Imgproc.cvtColor(sourceMat, destinationMat, Imgproc.COLOR_GRAY2BGR);

            tresholdedImage = converter.MatToBuffered(destinationMat);
            tresholdedImage = swapBlackToRed(tresholdedImage);

            try {
                ImageIO.write(tresholdedImage, "jpg", new File(System.getProperty("user.dir") + "/tresholdTestOutput.jpg"));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return tresholdedImage;
        }
    }

    public Rect[] detectFaces(Mat rawImage) {
        synchronized (syncObject) {
            MatOfRect faceDetections = new MatOfRect();
            CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.dir") + "/src/main/resources/lbpcascade_frontalface.xml");

            faceDetector.detectMultiScale(rawImage, faceDetections);
            Rect[] faces = faceDetections.toArray();

            return faces;
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
