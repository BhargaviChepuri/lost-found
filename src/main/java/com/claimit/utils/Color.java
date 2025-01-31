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

    public String getName() { return name; }
    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }
}
