package de.uniOldenburg.model;

import org.eclipse.jetty.websocket.api.Session;
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
    private Session session;

    public ImageProcessor(String fontFace, float fontSize, Color backgroundColor, float borderSize, Color borderColor, int margin, Session session) {
        ResourceLoader resourceLoader = new ResourceLoader();
        this.font = resourceLoader.getFont(fontFace, fontSize);
        this.backgroundColor = backgroundColor;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin = margin;
        this.session = session;
    }

    public void processImages(ArrayList<BufferedImage> buffImageList, String text) throws ExecutionException {

        text = text.toUpperCase(new Locale("de_DE"));
        int glyphCounter = 0;
        boolean isFirstImage = true;
        Converter converter = new Converter();

        ProgressListener progress = new ProgressListener(text, session);

        //for threadHandling with Callable
        final ExecutorService service;
        List<Future<LetterResult>> tasks = new ArrayList<Future<LetterResult>>();
        service = Executors.newFixedThreadPool(text.length()); //Max amount of threads working at the same time

        for (BufferedImage rawImage : buffImageList) {

            // Start the thread
            tasks.add(service.submit(new LetterThread(rawImage, text.charAt(glyphCounter), glyphCounter, font, borderSize, borderColor, margin, progress)));
            glyphCounter++;
        }

        for (Future<LetterResult> task : tasks){
            try {
                BufferedImage bufferedImage = task.get().letterImage;

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

        service.shutdownNow();
        String url = converter.saveBuffImgAsPNG(finalImage);
        progress.sendFinalImage(url);
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
        BufferedImage resultImage;

        if (firstImage.getHeight() != secondImage.getHeight()) {
            int newWidth = (int)(((double)firstImage.getHeight()/(double)secondImage.getHeight())*(double)secondImage.getWidth());
            secondImage = getScaledImage(secondImage, newWidth, firstImage.getHeight());
        }

        resultImage = new BufferedImage(firstImage.getWidth() +
                secondImage.getWidth(), firstImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = resultImage.getGraphics();
        g.drawImage(firstImage, 0, 0, null);
        g.drawImage(secondImage, firstImage.getWidth(), 0, null);

        resultImage = setBackgroundColor(resultImage, backgroundColor);
        return resultImage;
    }

    /**
     * Resizes an image using a Graphics2D object backed by a BufferedImage.
     * Source: http://stackoverflow.com/questions/16497853/scale-a-bufferedimage-the-fastest-and-easiest-way
     * @param src - source image to scale
     * @param w - desired width
     * @param h - desired height
     * @return - the new resized image
     */
    private BufferedImage getScaledImage(BufferedImage src, int w, int h){
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