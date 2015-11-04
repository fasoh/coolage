import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
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
    Converter convert = new Converter();
    ResourceLoader loadResource = new ResourceLoader();
    private int xOffset;
    private int yOffset;

    public ImageProcessor(ArrayList<Mat> matImageList){
        this.matImageList = matImageList;
    }

    public void processImages(String text, String fontFace, Color backgroundColor, float fontSize, float borderSize, Color borderColor, int margin) {

        text = text.toUpperCase();
        BufferedImage finalImage = null;
        int counter = 0;

        for (Mat rawImage : matImageList){
            BufferedImage newSingleLetterImage = this.detectFaces(rawImage);
            newSingleLetterImage = this.drawLettersOnGeneratedImage(newSingleLetterImage, text.charAt(counter), fontFace, backgroundColor, fontSize, borderSize, borderColor, margin);

            if (counter == 0) {
                System.out.println("- Set as result image");
                finalImage = newSingleLetterImage; //Avoids the case that picture 0 gets stitched to a copy of picture 0
            } else {
                try {
                    finalImage = stitchImages(finalImage, newSingleLetterImage, backgroundColor, text.length());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            counter++;
        }

        convert.saveBuffImgAsPNG(finalImage);

    }

    public BufferedImage detectFaces(Mat rawImage) { // Detects faces in an image, draws boxes around them, and writes the results to "faceDetection.png".

        // Create a face detector from the cascade file in the resources directory.
        CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.dir") + "/src/resources/lbpcascade_frontalface.xml");

        // Detect faces in the image. MatOfRect is a special container class for Rect.
        System.out.print("Detecting faces: ");
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(rawImage, faceDetections);
        System.out.print(String.format("%s found - ", faceDetections.toArray().length));

        // Draw a bounding box around each face.
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(rawImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
            xOffset = rect.x;
            yOffset = rect.y;
        }

        return convert.MatToBuffered(rawImage);
    }

    public BufferedImage drawLettersOnGeneratedImage(BufferedImage buffImage, Character letter, String fontFace, Color backgroundColor, float fontSize, float borderSize, Color borderColor, int margin){

        System.out.print("Applying text: '");

        BufferedImage textImage;
        if (letter == '\u00c4' || letter == '\u00d6' || letter == '\u00dc'){ //ä,ö,ü
            letter = letter.toString().toLowerCase().charAt(0);
        }

        if (letter != ' '){
            textImage = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = textImage.createGraphics();
            FontRenderContext frc = g.getFontRenderContext();
            Font font;
            if(fontFace.startsWith("http")){
                font = loadResource.customFontFromUrl(fontFace, fontSize);
            } else {
                font = loadResource.customFontFromFile(fontFace, fontSize);
            }
            GlyphVector gv = font.createGlyphVector(frc, letter.toString());
            Rectangle2D box = gv.getVisualBounds();
            int xOff = xOffset+(int)box.getX();
            int yOff = yOffset+(int)-box.getY();
            Shape shape = gv.getOutline(xOff, yOff);
            g.setClip(shape);
            g.drawImage(buffImage, 0, 0, null);
            g.setClip(null);
            g.setStroke(new BasicStroke(borderSize));
            g.setColor(borderColor);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.draw(shape);

            g.dispose();

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
        BufferedImage croppedImage = new BufferedImage( (bottomX - topX + 1 + margin),
                (bottomY - topY + 1 + margin), BufferedImage.TYPE_INT_ARGB);

        //Fill newly created image
        croppedImage.getGraphics().drawImage(source, 0, 0,
                croppedImage.getWidth(), croppedImage.getHeight(),
                topX - margin, topY - margin, bottomX + margin, bottomY + margin, null);

        return croppedImage;
    }


    public BufferedImage stitchImages(BufferedImage firstImage, BufferedImage newSingleLetterImage, Color backgroundColor, int textLength) throws IOException {

        //Stitches images firstImage and newSingleLetterImage together (which becomes the new firstImage for the next iteration)

        if (textLength == 1) { //Special case for single-letter collage
            return convert.MatToBuffered(matImageList.get(0));
        } else {
            System.out.println("- Stitching to previous image");

            BufferedImage resultImage;

            int newHeight;
            if (newSingleLetterImage.getHeight() > firstImage.getHeight()){
                newHeight = newSingleLetterImage.getHeight();
            } else {
                newHeight = firstImage.getHeight();
            }

            resultImage = new BufferedImage(firstImage.getWidth() +
                    newSingleLetterImage.getWidth(), newHeight,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics g = resultImage.getGraphics();
            g.drawImage(firstImage, 0, 0, null);
            g.drawImage(newSingleLetterImage, firstImage.getWidth(), newHeight - newSingleLetterImage.getHeight(), null);

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