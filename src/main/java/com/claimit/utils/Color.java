package com.claimit.utils;

public class Color {
	private final String name;
	private final int r, g, b;

	public Color(String name, int r, int g, int b) {
		this.name = name;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	/**
	 * Gets the name of the color.
	 * 
	 * @return the name of the color.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Gets the red component of the color.
	 * 
	 * @return the red component of the color.
	 */
	public final int getR() {
		return r;
	}

	/**
	 * Gets the green component of the color.
	 * 
	 * @return the green component of the color.
	 */
	public final int getG() {
		return g;
	}

	/**
	 * Gets the blue component of the color.
	 * 
	 * @return the blue component of the color.
	 */
	public final int getB() {
		return b;
	}
}
