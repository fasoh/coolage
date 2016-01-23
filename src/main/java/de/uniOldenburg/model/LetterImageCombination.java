package de.uniOldenburg.model;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import org.opencv.core.Point;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LetterImageCombination {

    private BufferedImage photo;
    private char letter;
    private Font font;
    private float borderSize;
    private Color borderColor;
    private int margin;
    Converter converter = new Converter();

    /**
     * Konstruktor für alle Bilder nach dem ersten (die zum vorherigen gestitched werden sollen)
     */
    public LetterImageCombination(BufferedImage photo, char letter, Font font, float borderSize, Color borderColor, int margin) {

        this.photo = photo;
        this.letter = letter;
        this.font = font;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin = margin;
    }

    /**
     * Methode, die für jeden Thread vom Callable aufgerufen wird (analog zu run() bei Threads)
     */
    public LetterResult getOptimalPosition() {

        LetterResult result;
        int accuracy = 10; //Je höher, desto genauer
        BufferedImage photoGlyph;

        Mat tempMat = converter.BufferedToMat(converter.toBufferedImageOfType(photo, BufferedImage.TYPE_3BYTE_BGR));
        Rect[] faces = converter.detectFaces(tempMat);

        BufferedImage qualityAreas;

        if (faces.length == 0) {
            qualityAreas = converter.swapBlackToRed(converter.getTresholdImage(photo));
        } else {
            qualityAreas = drawFaces(faces, photo.getWidth(), photo.getHeight());
        }

        BestPositionResult bestPositionResult = getBestCoordinates(qualityAreas, accuracy, letter);
        photoGlyph = getPhotoGlyph(photo, letter, bestPositionResult.scale, bestPositionResult.bestX, bestPositionResult.bestY);
        photoGlyph = cropImage(photoGlyph, margin);
        result = new LetterResult(photoGlyph, letter, faces.length, bestPositionResult.bestQuality * 100);

        return result;
    }

    private BestPositionResult getBestCoordinates(BufferedImage qualityImage, int accuracyTiles, char character) {
        double bestQuality = 0;
        int bestX = 0;
        int bestY = 0;
        double scale;

        int[] dimensions = getFontDimensions(font, String.valueOf(character));

        int accuracyX = (qualityImage.getWidth() - dimensions[0]) / accuracyTiles;
        int accuracyY = (qualityImage.getHeight() - dimensions[1]) / accuracyTiles;

        double buffRatio = (double)qualityImage.getHeight() / (double)qualityImage.getWidth();
        double letterRatio = (double)dimensions[1] / (double)dimensions[0];

        if (buffRatio > letterRatio) {

            scale = (double)qualityImage.getWidth() / (double)dimensions[0];
            int fontHeight = (int)((double)dimensions[1]*scale);

            for (int y = 0; y <= (qualityImage.getHeight() - fontHeight); y += accuracyY) {
                double newQuality = getQualityOfPosition(qualityImage, letter, scale, 0, y);
                if (newQuality > bestQuality) {
                    bestQuality = newQuality;
                    bestY = y;
                }
                if (newQuality == 1.0) break;
            }
        } else {

            scale = (double) qualityImage.getHeight() / (double) dimensions[1];
            int fontWidth = (int)((double)dimensions[0]*scale);

            for (int x = 0; x <= (qualityImage.getWidth() - fontWidth); x += accuracyX) {

                double newQuality = getQualityOfPosition(qualityImage, letter, scale, x, 0);
                if (newQuality > bestQuality) {
                    bestQuality = newQuality;
                    bestX = x;
                }
                if (newQuality == 1.0) break;
            }
        }

        return new BestPositionResult(bestX, bestY, scale, bestQuality);
    }

    private int[] getFontDimensions(Font font, String letter) {

        Graphics2D canvas = photo.createGraphics();
        FontRenderContext frc = canvas.getFontRenderContext();

        GlyphVector glyphVector = font.createGlyphVector(frc, letter.toString());
        Rectangle2D glyphBox = glyphVector.getVisualBounds();
        int[] dimensions = {(int)glyphBox.getWidth(), (int)glyphBox.getHeight()};

        return dimensions;
    }

    private BufferedImage getPhotoGlyph(BufferedImage buffImage, Character letter, double imageScale, int offsetX, int offsetY) {

        // Makes sure border is visible
        imageScale = imageScale*0.99;

        BufferedImage finalImage = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D canvas = finalImage.createGraphics();
        FontRenderContext frc = canvas.getFontRenderContext();

        GlyphVector glyphVector = font.createGlyphVector(frc, letter.toString());
        Rectangle2D glyphBox = glyphVector.getVisualBounds();

        AffineTransform scaling = new AffineTransform();
        scaling.scale(imageScale, imageScale);
        glyphVector.setGlyphTransform(0, scaling);

        int xOff = (int) (-glyphBox.getX()*imageScale);
        int yOff = (int) (-glyphBox.getY()*imageScale);
        Shape shape = glyphVector.getOutline(xOff + offsetX, yOff + offsetY);

        canvas.setClip(shape); // Deactivate to see letter position in image
        canvas.drawImage(buffImage, 0, 0, null);

        canvas.setClip(null);
        canvas.setStroke(new BasicStroke(borderSize));
        canvas.setColor(borderColor);
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.draw(shape);
        canvas.dispose();

        return finalImage;
    }

    private BufferedImage drawFaces(Rect[] faces, int canvasWidth, int canvasHeight) {
        BufferedImage canvas;
        Mat tempMat = new Mat(canvasHeight, canvasWidth, CvType.CV_8UC3, new Scalar(255, 255, 255));

        for (Rect face : faces) {
            Point center = new Point(face.x + face.width * 0.5, face.y + face.height * 0.5);
            Imgproc.ellipse(tempMat, center, new Size(face.width * 0.5, face.height * 0.5), 0, 0, 360, new Scalar(0, 0, 255), -1);
        }

        canvas = converter.MatToBuffered(tempMat);

        try {
            ImageIO.write(canvas, "png", new File(System.getProperty("user.dir") + "/faceDetectionTestOutput.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return canvas;
    }

    private double getQualityOfPosition(BufferedImage qualityImage, char letter, double imageScale, int offsetX, int offsetY) {
        BufferedImage croppedQualityAreas = getPhotoGlyph(qualityImage, letter, imageScale, offsetX, offsetY);
        return (countQualityPixels(croppedQualityAreas) / countQualityPixels(qualityImage));
    }

    /**
     * Counts the number of red pixels in a given image
     *
     * @param qualityAreas Quality Image
     * @return number of black pixels
     */
    private double countQualityPixels(BufferedImage qualityAreas) {
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

    /**
     * Crops the parts of an image that have the same color as the top left pixel
     * The algorithm checks the image pixel by pixel. It stops when the current pixel does NOT equal the top left pixel
     * Therefore it draws a rectangle over the letter
     */
    public BufferedImage cropImage(BufferedImage source, int margin) {
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