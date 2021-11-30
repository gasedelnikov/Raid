package com.gri.model;

public class Character {
    public String name;
    public String fraction;
    public String element;
    public double attributeGroupFilterValue;
    public double rankFilterValue;
    public double priorityLimitFilterValue;

    public Character(String name, String fraction, String element, double attributeGroupFilterValue, double rankFilterValue, double priorityLimitFilterValue) {
        this.name = name;
        this.fraction = fraction;
        this.element = element;
        this.attributeGroupFilterValue = attributeGroupFilterValue;
        this.rankFilterValue = rankFilterValue;
        this.priorityLimitFilterValue = priorityLimitFilterValue;
    }
}
