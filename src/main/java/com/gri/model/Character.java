package com.gri.model;

public class Character {
    public String name;
    public String fraction;
    public String element;
    public double attributeGroupFilterValue;
    public double priorityFilterValue;
    public double priorityLimitFilterValue;

    public Character(String name, String fraction, String element, double attributeGroupFilterValue, double priorityFilterValue, double priorityLimitFilterValue) {
        this.name = name;
        this.fraction = fraction;
        this.element = element;
        this.attributeGroupFilterValue = attributeGroupFilterValue;
        this.priorityFilterValue = priorityFilterValue;
        this.priorityLimitFilterValue = priorityLimitFilterValue;
    }
}
