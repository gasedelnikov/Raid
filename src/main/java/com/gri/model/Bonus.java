package com.gri.model;

public class Bonus {
    public String name;
    public double quantum;
    public double[] values;
    public double type;

    public Bonus(String name, double quantum, double[] values, double type) {
        this.name = name;
        this.quantum = quantum;
        this.values = values;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public double getQuantum() {
        return quantum;
    }

}
