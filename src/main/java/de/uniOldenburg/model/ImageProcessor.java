package de.uniOldenburg.model;

import org.opencv.core.Core;

import java.awt.*;
import java.awt.image.BufferedImage;
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

    public ImageProcessor(String fontFace, float fontSize, Color backgroundColor, float borderSize, Color borderColor, int margin) {
        ResourceLoader resourceLoader = new ResourceLoader();
        this.font = resourceLoader.getFont(fontFace, fontSize);
        this.backgroundColor = backgroundColor;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin = margin;
    }

    public void processImages(ArrayList<BufferedImage> buffImageList, String text) throws ExecutionException {

        System.out.println("Creating collage...");

        text = text.toUpperCase();
        int glyphCounter = 0;
        boolean isFirstImage = true;
        Converter converter = new Converter();

        //for threadHandling with Callable
        final ExecutorService service;
        List<Future<BufferedImage>> tasks = new ArrayList<Future<BufferedImage>>();
        service = Executors.newFixedThreadPool(text.length()); //Max amount of threads working at the same time

        for (BufferedImage rawImage : buffImageList){
            if (glyphCounter == 0){
                //Start thread without glyphCounter
                tasks.add(service.submit(new LetterThread(rawImage, text, font, borderSize, borderColor, margin)));
            } else {
                //Start thread with glyphCounter
                tasks.add(service.submit(new LetterThread(rawImage, text, glyphCounter, font, borderSize, borderColor, margin)));
            }

            glyphCounter++;
        }

        for (Future<BufferedImage> task : tasks){
            try {
                BufferedImage bufferedImage = task.get();

                if (isFirstImage){
                    bufferedImage = setBackgroundColor(bufferedImage, backgroundColor);
                    saveImage(bufferedImage);
                    isFirstImage = false;
                    glyphCounter++;
                } else {
                    try {
                        finalImage = stitchImages(finalImage, bufferedImage, backgroundColor);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (final InterruptedException ex) {
                ex.printStackTrace();
            } catch (final ExecutionException ex) {
                ex.printStackTrace();
            }
        }

        converter.saveBuffImgAsPNG(finalImage);

        service.shutdownNow();
    }

    public void saveImage(BufferedImage bufferedImage){
        this.finalImage = bufferedImage;
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

    public BufferedImage stitchImages(BufferedImage firstImage, BufferedImage secondImage, Color backgroundColor) throws IOException {
        //Stitches images firstImage and secondImage together (which becomes the new firstImage for the next iteration)
        BufferedImage resultImage;

        int newHeight;
        if (secondImage.getHeight() > firstImage.getHeight()) {
            newHeight = secondImage.getHeight();
        } else {
            newHeight = firstImage.getHeight();
        }

        resultImage = new BufferedImage(firstImage.getWidth() +
                secondImage.getWidth(), newHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = resultImage.getGraphics();
        g.drawImage(firstImage, 0, 0, null);
        g.drawImage(secondImage, firstImage.getWidth(), newHeight - secondImage.getHeight(), null);

        resultImage = setBackgroundColor(resultImage, backgroundColor);
        return resultImage;

    }

}