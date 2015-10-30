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

    public ArrayList<BufferedImage> downloadImages(ArrayList<String> urlList){
        System.out.print("Downloading picture - ");
        int outputCounter = 1;
        ArrayList<BufferedImage> buffImageList = new ArrayList<BufferedImage>();
        for (String url : urlList){
            System.out.print(outputCounter + " ");
            BufferedImage buffImage = this.imageFromURL(url);
            buffImageList.add(buffImage);
            outputCounter++;
        }
        System.out.println();
        return buffImageList;
    }

    public Font customFont(String fontName, float fontSize) {
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
}
