package de.uniOldenburg.model;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class Converter {

    public String saveBuffImgAsPNG(BufferedImage buffImage) {

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
