package com.acropolismc.play.sellstick;
import java.util.Random;

public class RandomString {

	private static final char[] symbols;

	/*
	 * Just generates random color code combinations
	 * Only symbols 0-9 and a-f (for color codes)
	 * 
	 * This is used so I can make an invisible message
	 * Purpose being items don't stack unless
	 * their metadata is the same.
	 * By having random(invisible) codes
	 * Sellsticks will never stack
	 */
	static {
		StringBuilder tmp = new StringBuilder();
		for (char ch = '0'; ch <= '9'; ++ch)
			tmp.append(ch);
		for (char ch = 'a'; ch <= 'f'; ++ch)
			tmp.append(ch);
		
		symbols = tmp.toString().toCharArray();
	}

	private final Random random = new Random();

	private final char[] buf;

	public RandomString(int length) {
		if (length < 1)
			throw new IllegalArgumentException("length < 1: " + length);
		buf = new char[length];
	}

	public String nextString() {
		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = symbols[random.nextInt(symbols.length)];
		return makeInvis(new String(buf));
	}
	
	/*
	 * This then makes them invisible by putting a charcode infront of it.
	 */
	public String makeInvis(String original) {
		char[] letters = original.toCharArray();
		String newLetters = "";
		for(int i = 0; i < original.length(); i ++) {
			newLetters += "§" + letters[i] + "";
		}
		return newLetters;
	}
}