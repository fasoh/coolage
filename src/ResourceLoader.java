import org.opencv.core.Mat;

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

/**
 * Created by Jannik on 27.10.15.
 */
public class ResourceLoader {

    Converter convert = new Converter();

    public ArrayList<Mat> getImages(String text, ArrayList<String> urlList){
        System.out.print("Downloading picture - ");
        ArrayList<Mat> matImageList = new ArrayList<Mat>();

        if (text.length() > urlList.size()){
            System.out.println("Filling up imageList (text > imageList.size())");
            downloadImagesFromList(matImageList, urlList);
            int j = 0;
            for (int i = urlList.size(); i < text.length(); i++){
                matImageList.add(matImageList.get(j));
                j++;
            }
        } else { //Trims rawImageList to size of text if there are more images than text
            System.out.println("Trimming imageList (text < imageList.size())");
            for (int i = urlList.size(); i > text.length(); i--){
                urlList.remove(urlList.size() - 1);
            }
            downloadImagesFromList(matImageList, urlList);
        }

        return matImageList;
    }

    public ArrayList<Mat> downloadImagesFromList(ArrayList<Mat> matImageList, ArrayList<String> urlList){
        int outputCounter = 1;
        for (String url : urlList){
            System.out.print(outputCounter + " ");
            BufferedImage buffImage = this.imageFromURL(url);
            Mat matImage = convert.BufferedToMat(buffImage);
            matImageList.add(matImage);
            outputCounter++;
        }
        System.out.println("- Converted to Mat");
        return matImageList;
    }

    public Font customFontFromFile(String fontName, float fontSize) throws FileNotFoundException {
        Font customFont = null;

        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "/src/resources/" + fontName)).deriveFont(Font.PLAIN, fontSize);
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
            FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/src/resources/" + urlString.split("/")[urlString.split("/").length-1]);
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

    public BufferedImage imageFromURL(String urlString) {
        URL imageURL;
        BufferedImage img = null;

        try {
            imageURL = new URL(urlString);
            //Convert to BufferedImage of type BGR so openCV can read it
            img = convert.toBufferedImageOfType(ImageIO.read(imageURL), BufferedImage.TYPE_3BYTE_BGR);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

            // Save font for later use
            File file = new File(System.getProperty("user.dir") + "/src/resources/" + font.getFontName());
        }

        return font;
    }
}
