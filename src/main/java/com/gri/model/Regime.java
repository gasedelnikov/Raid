package com.gri.model;

public enum Regime {

    FIND_MAIN("FIND_MAIN"),
    FIND_DOUBLES("FIND_DOUBLES"),
    FIND_CHANGE1("FIND_CHANGE1"),
    TEST_CHARACTER("TEST_CHARACTER");

    private final String description;

    Regime(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
