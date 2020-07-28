package com.gri.model;

public class Place {
    public String name;
    public boolean checkFraction;
    public int orderBy;
    public double[] filterPr;
    public double[] filterMain;

    public Place(String name, boolean checkFraction, int orderBy, double[] filterPr, double[] filterMain) {
        this.name = name;
        this.checkFraction = checkFraction;
        this.orderBy = orderBy;
        this.filterPr = filterPr;
        this.filterMain = filterMain;
    }
}
