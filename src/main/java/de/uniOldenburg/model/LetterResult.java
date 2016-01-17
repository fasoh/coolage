package de.uniOldenburg.model;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

/**
 * Created by adrian-jagusch on 16.01.16.
 */
public class LetterResult {

    public BufferedImage letterImage;
    public char letter;
    public int position;
    public int numberOfFaces;
    public double quality;
    public Boolean isEmpty;

    public LetterResult(BufferedImage letterImage, char letter, int position, int numberOfFaces, double quality) {
        this.isEmpty = false;
        this.letterImage = letterImage;
        this.letter = letter;
        this.position = position;
        this.numberOfFaces = numberOfFaces;
        this.quality = quality;
    }

    public LetterResult(BufferedImage letterImage, Boolean isEmpty, int position) {
        this.letterImage = letterImage;
        this.isEmpty = isEmpty;
        this.position = position;
    }
}
