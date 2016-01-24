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

    public ArrayList<String> getFittedImagesSources(String text, ArrayList<String> urlList) {
        if (text.length() > urlList.size()){
            System.out.println("Filling up imageList (text > imageList.size())");
            int j = 0;
            for (int i = urlList.size(); i < text.length(); i++) {
                urlList.add(urlList.get(j));
                j++;
            }
        } /* else { //Trims rawImageList to size of text if there are more images than text
            System.out.println("Trimming imageList (text < imageList.size())");
            for (int i = urlList.size(); i > text.length(); i--){
                urlList.remove(urlList.size() - 1);
            }
        } */
        return urlList;
    }

    /*

    public ArrayList<BufferedImage> getImages(String text, ArrayList<String> urlList) {
        ArrayList<BufferedImage> buffImageList = new ArrayList<BufferedImage>();

        if (text.length() > urlList.size()){
            System.out.println("Filling up imageList (text > imageList.size())");
            loadImagesFromListAsBuffered(buffImageList, urlList);
            int j = 0;
            for (int i = urlList.size(); i < text.length(); i++) {
                buffImageList.add(buffImageList.get(j));
                j++;
            }
        } else { //Trims rawImageList to size of text if there are more images than text
            System.out.println("Trimming imageList (text < imageList.size())");
            for (int i = urlList.size(); i > text.length(); i--){
                urlList.remove(urlList.size() - 1);
            }
            loadImagesFromListAsBuffered(buffImageList, urlList);
        }

        return buffImageList;
    }

    public ArrayList<BufferedImage> loadImagesFromListAsBuffered(ArrayList<BufferedImage> buffImageList, ArrayList<String> urlList) {
        for (String url : urlList) {
            BufferedImage buffImage = getImage(url);
            buffImageList.add(buffImage);
        }
        return buffImageList;
    }

    */

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

    public BufferedImage getImage(String url) {

        BufferedImage image = null;
        String fileName = url.split("/")[url.split("/").length-1];

        // Try finding local image – else download from source
        try {
            image = this.imageFromURL("file://" + System.getProperty("user.dir") + "/src/main/cache/" + fileName);
        } catch (IOException e) {

            try {
                image = this.imageFromURL(url);
                ImageIO.write(image, "jpg", new File(System.getProperty("user.dir") + "/src/main/cache/" + fileName));
            } catch (IOException f) {
                f.printStackTrace();
            }
        }
        return image;
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

    public ArrayList<String> getExampleImages() {

        ArrayList<String> exampleImages = new ArrayList<String>();

        File folder = new File(System.getProperty("user.dir") + "/src/main/example images/");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                exampleImages.add("file://" + System.getProperty("user.dir") + "/src/main/example images/" + listOfFiles[i].getName());
            }
        }
        return exampleImages;
    }
}
