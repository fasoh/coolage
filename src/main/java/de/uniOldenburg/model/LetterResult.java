package de.uniOldenburg.model;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

/**
 * Created by adrian-jagusch on 16.01.16.
 */
public class LetterResult {

    public final BufferedImage letterImage;
    public final char letter;
    public final int numberOfFaces;
    public final double quality;
    public int bestIndex;

    public LetterResult(BufferedImage letterImage, char letter, int numberOfFaces, double quality) {
        this.letterImage = letterImage;
        this.letter = letter;
        this.numberOfFaces = numberOfFaces;
        this.quality = quality;
    }
}
