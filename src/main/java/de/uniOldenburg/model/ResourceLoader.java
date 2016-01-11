package de.uniOldenburg.model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class ResourceLoader {

    Converter convert = new Converter();

    public ArrayList<BufferedImage> getImages(String text, ArrayList<String> urlList, Dimension boundary){
        ArrayList<BufferedImage> buffImageList = new ArrayList<BufferedImage>();

        if (text.length() > urlList.size()){
            System.out.println("Filling up imageList (text > imageList.size())");
            downloadImagesFromListAsBuffered(buffImageList, urlList, boundary);
            int j = 0;
            for (int i = urlList.size(); i < text.length(); i++){
                buffImageList.add(buffImageList.get(j));
                j++;
            }
        } else { //Trims rawImageList to size of text if there are more images than text
            System.out.println("Trimming imageList (text < imageList.size())");
            for (int i = urlList.size(); i > text.length(); i--){
                urlList.remove(urlList.size() - 1);
            }
            downloadImagesFromListAsBuffered(buffImageList, urlList, boundary);
        }

        return buffImageList;
    }

    public ArrayList<BufferedImage> downloadImagesFromListAsBuffered(ArrayList<BufferedImage> buffImageList, ArrayList<String> urlList, Dimension boundary){
        int outputCounter = 1;
        for (String url : urlList){
            System.out.print("Picture #" + outputCounter + " ");
            BufferedImage buffImage = this.getImage(url, boundary);
            buffImageList.add(buffImage);
            outputCounter++;
        }
        System.out.println();
        return buffImageList;
    }

    public Font customFontFromFile(String fontName, float fontSize) throws FileNotFoundException {
        Font customFont = null;

        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "/src/main/resources/" + fontName)).deriveFont(Font.PLAIN, fontSize);
        } catch (IOException e) {
            throw new FileNotFoundException();
        } catch(FontFormatException e) {
            e.printStackTrace();
        }

        return customFont;
    }

    public Font customFontFromUrl(String urlString, float fontSize){

        System.out.println("Downloading font");
        Font customFont = null;

        try {
            URL fontUrl = new URL(urlString);
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
            customFont = customFont.deriveFont(Font.PLAIN, fontSize);
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

            ReadableByteChannel rbc = Channels.newChannel(fontUrl.openStream());
            FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/src/main/resources/" + urlString.split("/")[urlString.split("/").length-1]);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return customFont;
    }

    public BufferedImage getImage(String url, Dimension boundary) {

        BufferedImage image = null;
        String fileName = url.split("/")[url.split("/").length-1];

        // Try finding local image – else download from source
        try {
            image = this.imageFromURL("file://" + System.getProperty("user.dir") + "/src/main/cache/" + fileName);
            System.out.print("cached - ");
        } catch (IOException e) {

            try {
                image = this.imageFromURL(url);
                ImageIO.write(image, "jpg", new File(System.getProperty("user.dir") + "/src/main/cache/" + fileName));
                System.out.print("downloaded - ");
            } catch (IOException f) {
                f.printStackTrace();
            }
        }

        Dimension newDimension = getScaledDimension(new Dimension(image.getWidth(), image.getHeight()), boundary);
        BufferedImage scaledImage = resizeImage(image, newDimension.width, newDimension.height);

        System.out.println("original width: " + image.getWidth() + ", scaled to width: " + scaledImage.getWidth() + "; original height: " + image.getHeight() + ", scaled to height: " + scaledImage.getHeight());

        return scaledImage;
    }

    public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

        int original_width = imgSize.width;
        int original_height = imgSize.height;
        int bound_width = boundary.width;
        int bound_height = boundary.height;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }

        return new Dimension(new_width, new_height);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int biggerWidth, int biggerHeight) {
        int type = BufferedImage.TYPE_INT_ARGB;

        BufferedImage resizedImage = new BufferedImage(biggerWidth, biggerHeight, type);
        Graphics2D g = resizedImage.createGraphics();

        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(originalImage, 0, 0, biggerWidth, biggerHeight, null);
        g.dispose();

        return resizedImage;
    }

    public BufferedImage imageFromURL(String urlString) throws IOException {
        URL imageURL;
        BufferedImage img = null;

        try {
            imageURL = new URL(urlString);
            //Convert to BufferedImage of type BGR so openCV can read it
            img = convert.toBufferedImageOfType(ImageIO.read(imageURL), BufferedImage.TYPE_3BYTE_BGR);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return img;
    }

    public Font getFont(String url, float fontSize) {

        Font font;

        try {
            font = this.customFontFromFile(url.split("/")[url.split("/").length-1], fontSize);
        } catch (FileNotFoundException e) {
            font = this.customFontFromUrl(url, fontSize);
        }

        return font;
    }
}
