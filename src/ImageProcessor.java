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

// Detects faces in an image, draws boxes around them, and writes the results
// to "faceDetection.png".
public class ImageProcessor {

    Converter convert = new Converter();
    ResourceLoader loadResource = new ResourceLoader();
    ArrayList<BufferedImage> listOfRawImages = new ArrayList<BufferedImage>();
    ArrayList<BufferedImage> listOfBufferedImages = new ArrayList<BufferedImage>();

    public ImageProcessor(ArrayList<BufferedImage> listOfRawImages){
        this.listOfRawImages = listOfRawImages;
    }

    public void detectFaces() {

        for (BufferedImage rawImage : listOfRawImages){

            // Create a face detector from the cascade file in the resources directory.
            CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.dir") + "/src/resources/lbpcascade_frontalface.xml");
            Mat image = convert.BufferedToMat(rawImage);

            // Detect faces in the image. MatOfRect is a special container class for Rect.
            System.out.print("Detecting faces: ");
            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(image, faceDetections);
            System.out.print(String.format("%s found - ", faceDetections.toArray().length));

            // Draw a bounding box around each face.
            for (Rect rect : faceDetections.toArray()) {
                Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
            }

            //Save to ArrayList for later use (to be changed)
            listOfBufferedImages.add(convert.MatToBuffered(image));
            System.out.println("Saved to listOfBufferedImages");
        }

    }

    public void drawLettersOnGeneratedImage(String text, String fontFace, Color backgroundColor, float fontSize, float borderSize, Color borderColor, int margin){

        text = text.toUpperCase();

        //Fill up listOfBufferedImages with pictures of itself in case there is more text than available pictures
        if (text.length() > listOfBufferedImages.size()){
            int j = 0;
            for (int i = listOfBufferedImages.size(); i < text.length(); i++){
                listOfBufferedImages.add(listOfBufferedImages.get(j));
                j++;
            }
        } else {
            for (int i = listOfBufferedImages.size(); i > text.length(); i--){
                listOfBufferedImages.remove(listOfBufferedImages.size() - 1);
            }
        }

        System.out.print("Applying text on all images in listOfBufferedImages - ");

        int counter = 0;

        for (BufferedImage buffImage : listOfBufferedImages) {

            BufferedImage textImage;

            if (text.charAt(counter) != ' '){
                textImage = new BufferedImage(
                        buffImage.getWidth(),
                        buffImage.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = textImage.createGraphics();
                FontRenderContext frc = g.getFontRenderContext();
                Font font = loadResource.customFont(fontFace, fontSize);
                Character c = text.charAt(counter);
                GlyphVector gv = font.createGlyphVector(frc, c.toString());
                int xOff = textImage.getWidth()/2-100;
                int yOff = textImage.getHeight()/2+75;
                Shape shape = gv.getOutline(xOff, yOff);
                g.setClip(shape);
                    g.drawImage(buffImage, 0, 0, null);
                g.setClip(null);
                    g.setStroke(new BasicStroke(borderSize));
                    g.setColor(borderColor);
                    g.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                g.draw(shape);

                g.dispose();

                textImage = setBackgroundColor(textImage, backgroundColor);

                System.out.print(c + " ");

                textImage = cropImage(textImage, margin);
            } else {
                textImage = new BufferedImage(150, 1, BufferedImage.TYPE_INT_ARGB); //Create new image for empty space in text
            }

            listOfBufferedImages.set(counter, textImage);
            counter++;
        }

        try {
            if (text.length() > 1) {
                BufferedImage stitchedImages = stitchImages(backgroundColor);
                convert.saveBuffImgAsPNG(stitchedImages);
            } else {
                //Special case for single-letter collage
                convert.saveBuffImgAsPNG(listOfBufferedImages.get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(" - Success!");
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


    public BufferedImage stitchImages(Color backgroundColor) throws IOException {

        BufferedImage resultImage = null;

        for (BufferedImage buffImage : listOfBufferedImages){

            //Avoids the case that picture 0 gets stitched to picture 0 itself
            if (listOfBufferedImages.get(0).equals(buffImage)){
                continue;
            }

            BufferedImage firstImage = listOfBufferedImages.get(0);

            int newHeight;
            if (buffImage.getHeight() > firstImage.getHeight()){
                newHeight = buffImage.getHeight();
            } else {
                newHeight = firstImage.getHeight();
            }

            resultImage = new BufferedImage(firstImage.getWidth() +
                    buffImage.getWidth(), newHeight,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics g = resultImage.getGraphics();
            g.drawImage(firstImage, 0, 0, null);
            g.drawImage(buffImage, firstImage.getWidth(), newHeight-buffImage.getHeight(), null);
            listOfBufferedImages.set(0, resultImage);
        }

        resultImage = setBackgroundColor(resultImage, backgroundColor);

        return resultImage;

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