package de.uniOldenburg.model;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class Converter {

    public String saveFinalImgAsPNG(BufferedImage buffImage) {

        try {
            ImageIO.write(buffImage, "png", new File(System.getProperty("user.dir") + "/src/main/webapp/collage.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "/collage.png";
    }

    public BufferedImage toBufferedImageOfType(BufferedImage original, int type) {

        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't converter if it already has correct type
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

    public Mat BufferedToMat(BufferedImage buffImage) {
        byte[] pixels = ((DataBufferByte) buffImage.getRaster().getDataBuffer()).getData();
        // Create a Matrix the same size of image
        Mat imageMat = new Mat(buffImage.getHeight(), buffImage.getWidth(), CvType.CV_8UC3);
        // Fill Matrix with image values
        imageMat.put(0, 0, pixels);

        return imageMat;
    }

    public Mat BufferedToMat8UC1(BufferedImage buffImage) {
        byte[] pixels = ((DataBufferByte) buffImage.getRaster().getDataBuffer()).getData();
        // Create a Matrix the same size of image
        Mat imageMat = new Mat(buffImage.getHeight(), buffImage.getWidth(), CvType.CV_8UC1);
        // Fill Matrix with image values
        imageMat.put(0, 0, pixels);

        return imageMat;
    }

    public BufferedImage MatToBuffered(Mat in) {

        byte[] data = new byte[in.rows() * in.cols() * (int) (in.elemSize())];
        in.get(0, 0, data);
        if (in.channels() == 3) {
            for (int i = 0; i < data.length; i += 3) {
                byte temp = data[i];
                data[i] = data[i + 2];
                data[i + 2] = temp;
            }
        }
        BufferedImage image = new BufferedImage(in.cols(), in.rows(), BufferedImage.TYPE_3BYTE_BGR);
        image.getRaster().setDataElements(0, 0, in.cols(), in.rows(), data);
        return image;
    }

    public BufferedImage getTresholdImage(BufferedImage buffImage) {
        BufferedImage tresholdedImage = null;
        buffImage = toBufferedImageOfType(buffImage, BufferedImage.TYPE_3BYTE_BGR);
        Mat sourceMat = BufferedToMat(buffImage);
        Mat destinationMat = sourceMat;

        Imgproc.cvtColor(sourceMat, destinationMat, Imgproc.COLOR_RGB2GRAY);

        Imgproc.adaptiveThreshold(sourceMat, destinationMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 5);
        //Dokumentation zu adaptiveTreshold -  http://docs.opencv.org/2.4/modules/imgproc/doc/miscellaneous_transformations.html#adaptivethreshold

        Imgproc.cvtColor(sourceMat, destinationMat, Imgproc.COLOR_GRAY2BGR);

        tresholdedImage = MatToBuffered(destinationMat);

        try {
            ImageIO.write(tresholdedImage, "jpg", new File(System.getProperty("user.dir") + "/tresholdTestOutput.jpg"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tresholdedImage;
    }

    public Rect[] detectFaces(Mat rawImage) {
        MatOfRect faceDetections = new MatOfRect();
        CascadeClassifier faceDetector = new CascadeClassifier(System.getProperty("user.dir") + "/src/main/resources/lbpcascade_frontalface.xml");

        faceDetector.detectMultiScale(rawImage, faceDetections);
        Rect[] faces = faceDetections.toArray();

        return faces;
    }

    public BufferedImage swapBlackToRed(BufferedImage inputImage) {
        int photoWidth = inputImage.getWidth();
        int photoHeight = inputImage.getHeight();

        //red 0xFFFF0000
        for (int x = 0; x < photoWidth; x++) {
            for (int y = 0; y < photoHeight; y++) {
                if (inputImage.getRGB(x, y) != -1) { //-1 ist weiss
                    inputImage.setRGB(x, y, 0xFFFF0000);
                }
            }
        }

        return inputImage;
    }

    public BufferedImage stitchImages(BufferedImage firstImage, BufferedImage secondImage, Color backgroundColor) throws IOException {
        BufferedImage resultImage;

        if (firstImage.getHeight() != secondImage.getHeight()) {
            int newWidth = (int)(((double)firstImage.getHeight()/(double)secondImage.getHeight())*(double)secondImage.getWidth());
            secondImage = getScaledImage(secondImage, newWidth, firstImage.getHeight());
        }

        resultImage = new BufferedImage(firstImage.getWidth() +
                secondImage.getWidth(), firstImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = resultImage.getGraphics();
        g.drawImage(firstImage, 0, 0, null);
        g.drawImage(secondImage, firstImage.getWidth(), 0, null);

        resultImage = setBackgroundColor(resultImage, backgroundColor);
        return resultImage;
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

    /**
     * Resizes an image using a Graphics2D object backed by a BufferedImage.
     * Source: http://stackoverflow.com/questions/16497853/scale-a-bufferedimage-the-fastest-and-easiest-way
     * @param src - source image to scale
     * @param w - desired width
     * @param h - desired height
     * @return - the new resized image
     */
    public BufferedImage getScaledImage(BufferedImage src, int w, int h){
        int finalw = w;
        int finalh = h;
        double factor = 1.0d;
        if(src.getWidth() > src.getHeight()){
            factor = ((double)src.getHeight()/(double)src.getWidth());
            finalh = (int)(finalw * factor);
        }else{
            factor = ((double)src.getWidth()/(double)src.getHeight());
            finalw = (int)(finalh * factor);
        }

        BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, finalw, finalh, null);
        g2.dispose();
        return resizedImg;
    }
}
