package com.mobile.daryldaryl.mobile_computing.models;

/**
 * Created by liboa on 20/09/2017.
 */

public class RecognitionWord {
    private int x, y, w, h;
    private String text;

    public RecognitionWord(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public RecognitionWord(int x, int y, int w, int h, String text) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.text = text;
    }

    public RecognitionWord() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return this.text + " " + x + " " + y + " " + w + " " + h;
    }
}
