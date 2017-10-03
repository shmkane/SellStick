package com.acropolismc.play.sellstick;
import java.util.Random;

import org.bukkit.ChatColor;

public class RandomString {

	private static final char[] symbols;

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
	
	
	public String makeInvis(String original) {
		char[] letters = original.toCharArray();
		String newLetters = "";
		for(int i = 0; i < original.length(); i ++) {
			newLetters += "§" + letters[i] + "";
		}
		return newLetters;
	}
}