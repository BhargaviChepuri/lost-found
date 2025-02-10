package com.claimit.utils;

import java.util.Arrays;
import java.util.List;

public class ColorUtils {

	private static final List<Color> COLORS = Arrays.asList(new Color("White", 255, 255, 255),
			new Color("Black", 0, 0, 0), new Color("Red", 255, 0, 0), new Color("Green", 0, 255, 0),
			new Color("Blue", 0, 0, 255), new Color("Yellow", 255, 255, 0), new Color("Cyan", 0, 255, 255),
			new Color("Magenta", 255, 0, 255), new Color("Gray", 128, 128, 128), new Color("Orange", 255, 165, 0),
			new Color("Pink", 255, 192, 203));

	public static String getClosestColorName(int r, int g, int b) {
		Color closestColor = null;
		double minDistance = Double.MAX_VALUE;

		for (Color color : COLORS) {
			double distance = Math.sqrt(
					Math.pow(color.getR() - r, 2) + Math.pow(color.getG() - g, 2) + Math.pow(color.getB() - b, 2));
			if (distance < minDistance) {
				minDistance = distance;
				closestColor = color;
			}
		}
		if (closestColor != null) {
			return closestColor.getName();
		} else {
			return "Unknown";
		}

	}
}
