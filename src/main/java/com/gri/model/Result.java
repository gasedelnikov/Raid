package com.gri.model;

public class Result {

    Attribute[] attributes;
    double[] bonuses;

    public Result(Attribute[] attributes, double[] bonuses) {
        this.attributes = attributes;
        this.bonuses = bonuses;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public double[] getBonuses() {
        return bonuses;
    }
}
