package com.gri.model;

public class RankMask {

    private String name;
    private int count;
    private double[] mask;

    public RankMask(int c, int v, int h, int a, int d, int r, int s, int m, int count) {
        this.name = ((c == 1) ? "c" : "") +
                ((v == 1) ? "v" : "") +
                ((h == 1) ? "h" : "") +
                ((a == 1) ? "a" : "") +
                ((d == 1) ? "d" : "") +
                ((r == 1) ? "r" : "") +
                ((s == 1) ? "s" : "") +
                ((m == 1) ? "m" : "");
        this.count = count;
        this.mask = new double[]{c,v,h,a,d,r,s,m};
    }

    public RankMask(double[] mask, int count, String name) {
        this.name = name;
        this.count = count;
        this.mask = mask;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double[] getMask() {
        return mask;
    }

    public void setMask(double[] mask) {
        this.mask = mask;
    }
}
