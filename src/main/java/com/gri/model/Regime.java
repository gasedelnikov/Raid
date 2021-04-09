package com.gri.model;

public enum Regime {

    FIND_MULTI_THREAD("FIND_MULTI_THREAD"),
    FIND_MAIN("FIND_MAIN"),
    FIND_DOUBLES("FIND_DOUBLES"),
    FIND_CHANGE1("FIND_CHANGE1"),
    CHECK_CHARACTER("TEST_CHARACTER");

    private final String description;

    Regime(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
