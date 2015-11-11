import org.opencv.core.*;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jannik on 27.10.15.
 */

public class ImageProcessor {

    ArrayList<Mat> matImageList = new ArrayList<Mat>();
    Converter converter = new Converter();
    ResourceLoader loadResource = new ResourceLoader();
    private Font font;
    private Color backgroundColor;
    private float borderSize;
    private Color borderColor;
    private int margin;

    public ImageProcessor(String fontFace, float fontSize, Color backgroundColor,  float borderSize, Color borderColor, int margin) {

        this.font = loadResource.getFont(fontFace, fontSize);

        this.backgroundColor = backgroundColor;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin = margin;
    }

    public BufferedImage processImages(ArrayList<Mat> matImageList, String text) {

        text = text.toUpperCase();
        BufferedImage finalImage = null;
        int glyphCounter = 0;

        for (Mat rawImage : matImageList){

            BufferedImage photoGlyph = this.getPhotoGlyph(converter.MatToBuffered(rawImage), text.charAt(glyphCounter), 0.7, 0, 0);

            if (glyphCounter == 0) {
                finalImage = photoGlyph; //Avoids the case that picture 0 gets stitched to a copy of picture 0
                System.out.println();
            } else {
                try {
                    finalImage = stitchImages(finalImage, photoGlyph, backgroundColor, text.length());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            glyphCounter++;
        }

        return finalImage;
    }

    public Rect[] detectFaces(Mat rawImage) { // Detects faces in an image, draws boxes around them, and writes the results to "faceDetection.png".

        // Create a face detector from the cascade file in the resources directory.
        CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.dir") + "/src/resources/lbpcascade_frontalface.xml");

        // Detect faces in the image. MatOfRect is a special container class for Rect.
        System.out.print("Detecting faces: ");
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(rawImage, faceDetections);
        System.out.print(String.format("%s found - ", faceDetections.toArray().length));

        return faceDetections.toArray();

        /*
        // Draw a bounding box around each face.
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(rawImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
            xOffset = rect.x;
            yOffset = rect.y;
        }

        return converter.MatToBuffered(rawImage);
        */
    }

    public BufferedImage getPhotoGlyph(BufferedImage buffImage, Character letter, double imageScale, int offsetX, int offsetY) {

        System.out.print("Applying text: '");

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

            System.out.print(letter + "' ");

            textImage = cropImage(textImage, margin);
        } else {
            textImage = new BufferedImage(150, 1, BufferedImage.TYPE_INT_ARGB); //Create new image for empty space in text (width, height)
            System.out.print("  ");
        }

        return textImage;
    }

    public BufferedImage cropImage(BufferedImage source, int margin) {
        // Crops the parts of an image that have the same color as the top left pixel
        // The algorithm checks the image pixel by pixel. It stops when the current pixel does NOT equal the top left pixel
        // Therefore it draws a rectangle over the letter

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


    public BufferedImage stitchImages(BufferedImage firstImage, BufferedImage secondImage, Color backgroundColor, int textLength) throws IOException {

        //Stitches images firstImage and secondImage together (which becomes the new firstImage for the next iteration)

        if (textLength == 1) { //Special case for single-letter collage
            return converter.MatToBuffered(matImageList.get(0));
        } else {
            System.out.println("- Stitching to previous image");

            BufferedImage resultImage;

            int newHeight;
            if (secondImage.getHeight() > firstImage.getHeight()){
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

    private BufferedImage setBackgroundColor(BufferedImage buffImage, Color backgroundColor) {

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
}