package com.gri.model;

public class Character {
    public String name;
    public String fraction;
    public String element;
    public double attributeFilterValue;

    public Character(String name, String fraction, String element, double attributeFilterValue) {
        this.name = name;
        this.fraction = fraction;
        this.element = element;
        this.attributeFilterValue = attributeFilterValue;
    }
}
