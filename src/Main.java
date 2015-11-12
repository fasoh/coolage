import org.opencv.core.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        // Load the native library.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ResourceLoader resourceLoader = new ResourceLoader();

        ArrayList<String> urlList = new ArrayList<String>();
        urlList.add("http://lh3.ggpht.com/7pMIhD30CAwdtcrm3UGZVI45xRi8ynxNFe3tTUAEM76p35G62YklCZdR4JcHS9MR72_txTAGUBB8ozn1=s620");
        urlList.add("http://lh4.ggpht.com/8MCTX5uAr5bJxXzVdkk-vETHKTHQQZVp9dl9xqaaBNcTfvjkl8P05elW7f1aSf5Gd_ewxs4Wpgcjqb-Xfsk=s620");
        urlList.add("http://lh5.ggpht.com/fkOpCsPe9PNSEjcCzmWpbHbtOJ3MuMCujcj9jWmAwrE3KwoXTjQb5Eq4IGOSgWO2fEf9HP2rceZgh1d7=s620");
        urlList.add("http://lh4.ggpht.com/2fNMXZyMEAVkvHJmRXmp6bERkgvXmrbGCY8G9M_ajyAN1M_L4RSO1sXRepVrHMQckvbUFNlgZLE69Hyy=s620");
        urlList.add("http://lh4.ggpht.com/5X5I-rr9ZTPXp3r6twHgkhB2M3zobatltwrxr7HBwPSZpx_iFDieadQRlEKoXtPgy5pOlcRU0LAXSBhs=s620");

        String fontUrl = "https://fonts.gstatic.com/s/raleway/v9/PKCRbVvRfd5n7BTjtGiFZMDdSZkkecOE1hvV7ZHvhyU.ttf";
        String text = "abcdefgh";

        ImageProcessor imageProcessor = new ImageProcessor(fontUrl, 400f, Color.WHITE, 2f, Color.BLACK, 15);
        BufferedImage collage = imageProcessor.processImages(resourceLoader.getImages(text, urlList), text);

        Converter converter = new Converter();
        converter.saveBuffImgAsPNG(collage);

        System.out.println("Done!");
    }
}