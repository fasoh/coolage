import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

/**
 * Created by Jannik on 27.10.15.
 */
public class Converter {

    public void saveBuffImgAsPNG(BufferedImage buffImage) {
        try {
            ImageIO.write(buffImage, "png", new File(System.getProperty("user.dir") + "/collage.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage toBufferedImageOfType(BufferedImage original, int type) {

        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() == type) {
            return original;
        }

        System.out.print("Converting to BGR - ");

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
        System.out.print("Converting Image to Mat - ");
        byte[] pixels = ((DataBufferByte) buffImage.getRaster().getDataBuffer()).getData();
        // Create a Matrix the same size of image
        Mat imageMat = new Mat(buffImage.getHeight(), buffImage.getWidth(), CvType.CV_8UC3);
        // Fill Matrix with image values
        imageMat.put(0, 0, pixels);

        return imageMat;
    }

    public BufferedImage MatToBuffered(Mat in) {

        System.out.print("Converting Mat to Image - ");

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

}
