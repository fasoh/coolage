package de.uniOldenburg.model;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

/**
 * Created by adrian-jagusch on 16.01.16.
 */
public class LetterResult {

    public final BufferedImage letterImage;
    public final char letter;
    public final int position;
    public final int numberOfFaces;
    public final double quality;

    public LetterResult(BufferedImage letterImage, char letter, int position, int numberOfFaces, double quality) {
        this.letterImage = letterImage;
        this.letter = letter;
        this.position = position;
        this.numberOfFaces = numberOfFaces;
        this.quality = quality;
    }
}
