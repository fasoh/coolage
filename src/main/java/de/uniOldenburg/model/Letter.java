package de.uniOldenburg.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by adrian-jagusch on 22.01.16.
 */
public class Letter {

    private final ArrayList<BufferedImage> photoOptions;
    private final char letter;
    private final int index;
    private final Font font;
    private final float borderSize;
    private final Color borderColor;
    private final int margin;


    public Letter(ArrayList<BufferedImage> photoOptions, char letter, int index, Font font, float borderSize, Color borderColor, int margin) {

        this.photoOptions = photoOptions;
        this.letter = letter;
        this.index = index;
        this.font = font;
        this.borderSize = borderSize;
        this.borderColor = borderColor;
        this.margin = margin;
    }

    public LetterResult getOptimalLetter() {

        LetterResult bestCombination = null;
        LetterImageCombination letterImageCombination;
        LetterResult letterResult;
        double bestQuality = 0;

        for (int photoIndex = 0; photoIndex < photoOptions.size(); photoIndex++) {
            letterImageCombination = new LetterImageCombination(photoOptions.get(photoIndex), letter, font, borderSize, borderColor, margin);
            letterResult = letterImageCombination.getOptimalPosition();

            if (letterResult.quality > bestQuality) {
                bestCombination = letterResult;
                bestCombination.bestIndex = photoIndex;
            }
        }
        return bestCombination;
    }
}
