import org.opencv.core.*;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Jannik on 27.10.15.
 */

public class ImageProcessor {

    private Font font;
    private Color backgroundColor;
    private float borderSize;
    private Color borderColor;
    private int margin;
    private BufferedImage finalImage = null;

    public ImageProcessor(String fontFace, float fontSize, Color backgroundColor,  float borderSize, Color borderColor, int margin) {

        ResourceLoader loadResource = new ResourceLoader();
        this.font = loadResource.getFont(fontFace, fontSize);
        this.backgroundColor = backgroundColor;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin = margin;
    }

    public void processImages(ArrayList<BufferedImage> buffImageList, String text) throws ExecutionException {

        text = text.toUpperCase();
        int glyphCounter = 0;
        boolean isFirstImage = true;
        Converter converter = new Converter();

        //Für ThreadHandling mit Callable
        final ExecutorService service;
        List<Future<BufferedImage>> tasks = new ArrayList<Future<BufferedImage>>();
        service = Executors.newFixedThreadPool(text.length()); //Anzahl der zu erstellenden Threads

        for (BufferedImage rawImage : buffImageList){

            if (glyphCounter == 0){
                //Start thread without glyphCounter
                tasks.add(service.submit(new LetterThread(rawImage, text, font, backgroundColor, borderSize, borderColor, margin)));
            } else {
                //Start thread with glyphCounter
                tasks.add(service.submit(new LetterThread(rawImage, text, glyphCounter, font, backgroundColor, borderSize, borderColor, margin)));
            }

            glyphCounter++;
        }

        for (Future<BufferedImage> task : tasks){
            try {
                BufferedImage bufferedImage = task.get();

                if (isFirstImage){
                    bufferedImage = setBackgroundColor(bufferedImage, backgroundColor);
                    saveImage(bufferedImage);
                    isFirstImage = false;
                    glyphCounter++;
                } else {
                    try {
                        finalImage = stitchImages(finalImage, bufferedImage, backgroundColor);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (final InterruptedException ex) {
                ex.printStackTrace();
            } catch (final ExecutionException ex) {
                ex.printStackTrace();
            }
        }

        converter.saveBuffImgAsPNG(finalImage);

        service.shutdownNow();
    }

    public void saveImage(BufferedImage bufferedImage){
        this.finalImage = bufferedImage;
    }

    public BufferedImage setBackgroundColor(BufferedImage buffImage, Color backgroundColor) {

        BufferedImage backgroundLayer = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(),  BufferedImage.TYPE_INT_ARGB);
        BufferedImage newBuffImage = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = backgroundLayer.createGraphics();
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, backgroundLayer.getWidth(), backgroundLayer.getHeight());

        Graphics g = newBuffImage.getGraphics();
        g.drawImage(backgroundLayer, 0, 0, null);
        g.drawImage(buffImage, 0, 0, null);

        return newBuffImage;
    }

    public BufferedImage stitchImages(BufferedImage firstImage, BufferedImage secondImage, Color backgroundColor) throws IOException {
        //Stitches images firstImage and secondImage together (which becomes the new firstImage for the next iteration)
        BufferedImage resultImage;

        int newHeight;
        if (secondImage.getHeight() > firstImage.getHeight()) {
            newHeight = secondImage.getHeight();
        } else {
            newHeight = firstImage.getHeight();
        }

        resultImage = new BufferedImage(firstImage.getWidth() +
                secondImage.getWidth(), newHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = resultImage.getGraphics();
        g.drawImage(firstImage, 0, 0, null);
        g.drawImage(secondImage, firstImage.getWidth(), newHeight - secondImage.getHeight(), null);

        resultImage = setBackgroundColor(resultImage, backgroundColor);
        return resultImage;

    }

}

class LetterThread implements Callable<BufferedImage> {

    private BufferedImage rawImage;
    private String text;
    private int glyphCounter = 0;
    private Font font;
    private Color backgroundColor;
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
    public LetterThread(BufferedImage rawImage, String text, int glyphCounter, Font font, Color backgroundColor, float borderSize, Color borderColor, int margin) {
        this.rawImage = rawImage;
        this.text = text;
        this.glyphCounter = glyphCounter;
        this.font = font;
        this.backgroundColor = backgroundColor;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin= margin;
    }

    /**
     * Konstruktor für das allererste Bild (welches nicht gestitched werden soll)
     */
    public LetterThread(BufferedImage rawImage, String text, Font font, Color backgroundColor, float borderSize, Color borderColor, int margin) {
        this.rawImage = rawImage;
        this.text = text;
        this.font = font;
        this.backgroundColor = backgroundColor;
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

            setBackgroundColor(photoGlyph, backgroundColor);

            System.out.println("Letter " + text.charAt(glyphCounter) + " at position " + glyphCounter + " contains " + amountOfFaces + " face/s and has a quality of " + quality);

        } catch (Exception e) {
            e.printStackTrace();
        }

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
            Shape shape = glyphVector.getOutline(xOff+offsetX, yOff+offsetY);

            letterImage.setClip(shape); // Deactivate to see letter position in image

            Image scaledImage = buffImage.getScaledInstance(scaleX, scaleY, 1);

            letterImage.drawImage(scaledImage, 0, 0, null);
            letterImage.setClip(null);

            letterImage.setStroke(new BasicStroke(borderSize));
            letterImage.setColor(borderColor);
            letterImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            letterImage.draw(shape);

            letterImage.dispose();

            textImage = setBackgroundColor(textImage, backgroundColor);

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

    public Rect[] detectFaces(Mat rawImage) { // Detects faces in an image, draws boxes around them, and writes the results to "faceDetection.png".

        // Detect faces in the image. MatOfRect is a special container class for Rect.
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(rawImage, faceDetections);

        amountOfFaces = faceDetections.toArray().length; //für system out

        return faceDetections.toArray();
    }

    public BufferedImage setBackgroundColor(BufferedImage buffImage, Color backgroundColor) {

        BufferedImage backgroundLayer = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(),  BufferedImage.TYPE_INT_ARGB);
        BufferedImage newBuffImage = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = backgroundLayer.createGraphics();
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, backgroundLayer.getWidth(), backgroundLayer.getHeight());

        Graphics g = newBuffImage.getGraphics();
        g.drawImage(backgroundLayer, 0, 0, null);
        g.drawImage(buffImage, 0, 0, null);

        return newBuffImage;
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
