import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Jannik on 27.10.15.
 */
public class ResourceLoader {

    Converter convert = new Converter();

    public ArrayList<Mat> downloadImages(ArrayList<String> urlList){
        System.out.print("Downloading picture - ");
        int outputCounter = 1;
        ArrayList<Mat> matImageList = new ArrayList<Mat>();
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

    public Font customFontFromFile(String fontName, float fontSize) {
        Font customFont = null;

        try {
            //create the font to use (.ttf)
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "/src/resources/" + fontName)).deriveFont(fontSize);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //register the font
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "/src/resources/" + fontName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch(FontFormatException e)
        {
            e.printStackTrace();
        }

        return customFont;
    }

    public Font customFontFromUrl(String urlString, float fontSize){
        Font customFont = null;

        try {
            URL fontUrl = new URL(urlString);
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontUrl.openStream());
            customFont = customFont.deriveFont(Font.PLAIN, fontSize);
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
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

    public void matchImageCountWithWordCount(String text, ArrayList<Mat> buffImageList) {
        //Fill up rawImageList with pictures of itself in case there is more text than available pictures
        if (text.length() > buffImageList.size()){
            System.out.println("Filling up imageList (text > imageList.size())");
            int j = 0;
            for (int i = buffImageList.size(); i < text.length(); i++){
                buffImageList.add(buffImageList.get(j));
                j++;
            }
        } else { //Trims rawImageList to size of text if there are more images than text
            System.out.println("Trimming imageList (text < imageList.size())");
            for (int i = buffImageList.size(); i > text.length(); i--){
                buffImageList.remove(buffImageList.size() - 1);
            }
        }

    }
}
