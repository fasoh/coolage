import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
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

    public ImageProcessor(ArrayList<BufferedImage> rawImageList){
        System.out.print("Converting Images to Mat - ");
        int counter = 1;
        for (BufferedImage bufferedImage : rawImageList){
            matImageList.add(convert.BufferedToMat(bufferedImage));
            System.out.print(counter + " ");
            counter++;
        }
        System.out.println();
    }

    public void processImages(String text, String fontFace, Color backgroundColor, float fontSize, float borderSize, Color borderColor, int margin) {

        this.matchImageCountWithWordCount(text);
        text = text.toUpperCase();
        BufferedImage finalImage = null;
        int counter = 0;

        for (Mat rawImage : matImageList){
            BufferedImage newSingleLetterImage = this.detectFaces(rawImage);
            newSingleLetterImage = this.drawLettersOnGeneratedImage(newSingleLetterImage, text.charAt(counter), fontFace, backgroundColor, fontSize, borderSize, borderColor, margin);

            if (counter == 0) {
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

    private void matchImageCountWithWordCount(String text) {
        //Fill up rawImageList with pictures of itself in case there is more text than available pictures
        if (text.length() > matImageList.size()){
            int j = 0;
            for (int i = matImageList.size(); i < text.length(); i++){
                matImageList.add(matImageList.get(j));
                j++;
            }
        } else { //Trims rawImageList to size of text if there are more images than text
            for (int i = matImageList.size(); i > text.length(); i--){
                matImageList.remove(matImageList.size() - 1);
            }
        }

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
        }

        return convert.MatToBuffered(rawImage);
    }

    public BufferedImage drawLettersOnGeneratedImage(BufferedImage buffImage, Character letter, String fontFace, Color backgroundColor, float fontSize, float borderSize, Color borderColor, int margin){

        System.out.print("Applying text: ");

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
            int xOff = textImage.getWidth()/2-100;
            int yOff = textImage.getHeight()/2+75;
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

            System.out.print(letter + " ");

            textImage = cropImage(textImage, margin);
        } else {
            textImage = new BufferedImage(150, 1, BufferedImage.TYPE_INT_ARGB); //Create new image for empty space in text (width, height)
            System.out.print("  ");
        }

        System.out.println(" - Success!");

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
        BufferedImage newBuffImage = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < buffImage.getWidth(); x++){
            for (int y = 0; y < buffImage.getHeight(); y++){
                int rgba = buffImage.getRGB(x,y);
                boolean isTrans = (rgba & 0xff000000) == 0;
                if (isTrans){
                    newBuffImage.setRGB(x, y, (backgroundColor.getRGB()));
                } else {
                    newBuffImage.setRGB(x, y, rgba);
                }
            }
        }

        return newBuffImage;
    }

}