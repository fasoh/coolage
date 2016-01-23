package de.uniOldenburg.model;

import org.eclipse.jetty.websocket.api.Session;
import org.opencv.core.Core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class ImageProcessor {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Font font;
    private Color backgroundColor;
    private float borderSize;
    private Color borderColor;
    private int margin;
    private BufferedImage finalImage = null;
    private Session session;
    private Converter converter = new Converter();

    public ImageProcessor(String fontFace, float fontSize, Color backgroundColor, float borderSize, Color borderColor, int margin, Session session) {
        ResourceLoader resourceLoader = new ResourceLoader();
        this.font = resourceLoader.getFont(fontFace, fontSize);
        this.backgroundColor = backgroundColor;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin = margin;
        this.session = session;
    }

    public void processImages(ArrayList<String> urlList, String text) {

        text = text.toUpperCase(new Locale("de_DE"));
        ProgressListener progress = new ProgressListener(text, session);
        LetterImageCombinationCache cache = new LetterImageCombinationCache();

        for (int glyphCounter = 0; glyphCounter < text.length(); glyphCounter++) {

            Letter letter = new Letter(urlList, text.charAt(glyphCounter), glyphCounter, font, borderSize, borderColor, margin, cache);
            LetterResult letterResult = letter.getOptimalLetter();
            BufferedImage bufferedImage = letterResult.letterImage;
            progress.letterFinished(letterResult, glyphCounter);

            urlList.remove(letterResult.bestIndex);

            if (glyphCounter == 0){

                bufferedImage = converter.setBackgroundColor(bufferedImage, backgroundColor);
                finalImage = bufferedImage;
            } else {
                try {
                    finalImage = converter.stitchImages(finalImage, bufferedImage, backgroundColor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String url = converter.saveFinalImgAsPNG(finalImage);
        progress.sendFinalImage(url);
    }
}