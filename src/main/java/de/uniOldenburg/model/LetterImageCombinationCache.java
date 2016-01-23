package de.uniOldenburg.model;

import java.util.HashMap;

public class LetterImageCombinationCache {

    private HashMap<String, HashMap<Character, LetterResult>> cache = new HashMap<String, HashMap<Character, LetterResult>>();

    public LetterResult getLetterResult(LetterImageCombination letter) {

        String url = letter.photoUrl;
        Character character = letter.letter;

        if (cache.containsKey(url)) {
            if (!cache.get(url).containsKey(character)) {
                cache.get(url).put(character, letter.getOptimalPosition());
            }
        } else {
            cache.put(url, new HashMap<Character, LetterResult>());
            cache.get(url).put(character, letter.getOptimalPosition());
        }
        return cache.get(url).get(character);
    }
}