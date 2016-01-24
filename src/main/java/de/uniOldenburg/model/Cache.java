package de.uniOldenburg.model;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Cache {

    private HashMap<String, HashMap<Character, LetterResult>> letterImageCache = new HashMap<String, HashMap<Character, LetterResult>>();
    private HashMap<String, LetterResult> qualityAreasCache = new HashMap<String, LetterResult>();
    private ResourceLoader loader = new ResourceLoader();
    private Converter converter = new Converter();

    public LetterResult getLetterResult(LetterImageCombination letter) {

        String url = letter.photoUrl;
        Character character = letter.letter;

        if (letterImageCache.containsKey(url)) {
            if (!letterImageCache.get(url).containsKey(character)) {
                letterImageCache.get(url).put(character, letter.getOptimalPosition());
            }
        } else {
            letterImageCache.put(url, new HashMap<Character, LetterResult>());
            letterImageCache.get(url).put(character, letter.getOptimalPosition());
        }
        return letterImageCache.get(url).get(character);
    }

    public LetterResult getQualityAreas(String photoUrl) {

        if (!qualityAreasCache.containsKey(photoUrl)) {

            BufferedImage qualityAreas;
            BufferedImage image = loader.getImage(photoUrl);

            Mat tempMat = converter.BufferedToMat(converter.toBufferedImageOfType(image, BufferedImage.TYPE_3BYTE_BGR));
            Rect[] faces = converter.detectFaces(tempMat);

            if (faces.length == 0) {
                qualityAreas = converter.swapBlackToRed(converter.getTresholdImage(image));
            } else {
                qualityAreas = converter.drawEllipses(faces, image.getWidth(), image.getHeight());
            }

            qualityAreasCache.put(photoUrl, new LetterResult(qualityAreas, 'A', faces.length, 0));
        }
        return qualityAreasCache.get(photoUrl);
    }
}