package com.shmkane.sellstick;

import java.util.Random;

import net.md_5.bungee.api.ChatColor;

public class RandomString {

    private static final char[] symbols;

    /**
     * Just generates random color code combinations Only symbols 0-9 and a-f (for
     * color codes)
     *
     * This is used so I can make an invisible message Purpose being items don't
     * stack unless their metadata is the same. By having random(invisible) codes
     * SellSticks will never stack
     */
    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch)
            tmp.append(ch);
        for (char ch = 'a'; ch <= 'f'; ++ch)
            tmp.append(ch);

        symbols = tmp.toString().toCharArray();
    }

    /**
     * Random num generator
     **/
    private final Random random = new Random();

    /**
     * Buffer
     **/
    private final char[] buf;

    /**
     * Constructs a string of 'length'
     *
     * @param length How long to make the random string
     */
    public RandomString(int length) {
        if (length < 1)
            throw new IllegalArgumentException("length < 1: " + length);
        buf = new char[length];
    }

    /**
     * Return the string and generate another. Make it invisible
     *
     * @return Returns an invisible string of random chars
     */
    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return makeInvis(new String(buf));
    }

    /**
     * Makes invisible by putting a charcode infront of it.
     */
    public String makeInvis(String original) {
        char[] letters = original.toCharArray();
        String newLetters = "";
        for (int i = 0; i < original.length(); i++) {
            newLetters += ChatColor.getByChar(letters[i]);

        }
        return newLetters;
    }
}