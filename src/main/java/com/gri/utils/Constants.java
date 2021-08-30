package com.gri.utils;

public class Constants {
    public static final int ATR_START_ROW = 8;
    public static final int ATR_VALUES_COUNT = 9;
    public static final int ATR_VALUES_MAIN_COUNT = 8;

    public static final int PLACES_ROW = 0;
    public static final int PLACES_COLL_START = 22;
    public static final int PLACES_COUNT = 9;

    public static final int VAL_BASE_ROW = 1;
    public static final int VAL_LEAGUE_ROW = 2;
    public static final int VAL_GLYPHS_ROW = 3;
    public static final int VAL_TARGET_ROW = 4;

    public static final int VAL_COLL_START = 12;
    public static final int VAL_COUNT = 9;
    public static final int VAL_EFFECTIVE_COLL_START = 8;
    public static final int VAL_EFFECTIVE_COUNT = 4;

    public static final int BONUSES_ROW_START = 1;
    public static final int BONUSES_ROW_END = 43;
    public static final int BONUSES_COLL_START = 0;
    public static final int BONUSES_COLL_COUNT = 9;
    public static final int BONUSES_VAL_COUNT = 9;
    public static final int BONUSES_COLL_TYPE = 14;

    public static class Character {
        public static final int ROW_INDEX_NAME = 0;
        public static final int COLL_INDEX_NAME = 0;

        public static final int ROW_INDEX_ELEMENT = 1;
        public static final int COLL_INDEX_ELEMENT = 0;

        public static final int ROW_INDEX_FRACTION = 2;
        public static final int COLL_INDEX_FRACTION = 0;
    }

    public static class Filter {
        public static final String GRIN = "FF00B050";
        public static final String RED = "FFFF0000";

        public static final String COLOR_FILTER_COLOR = GRIN;

        public static final int COLOR_FILTER_KEY = 32;
        public static final int COLOR_FILTER_INDEX = 33;

        public static final int COLL_INDEX_FILTER = 0;
        public static final int ROW_INDEX_GROUP_FILTER = 3;
        public static final int ROW_INDEX_PRIORITY_FILTER = 4;
        public static final int ROW_INDEX_PRIORITY_COUNT_FILTER = 5;
    }

    public static class Indexes {
        public static final int SIZE = 9;

        public static final int KRIT_S = 0;
        public static final int KRIT_V = 1;
        public static final int ZD = 2;
        public static final int ATK = 3;
        public static final int DEF = 4;
        public static final int SOP = 5;
        public static final int SKOR = 6;
        public static final int METK = 7;
        public static final int KRAZA = 8;
    }

    public static class Columns {
        public static final int CHARACTER_NAME = 1;
        public static final int PLACE = 2;
        public static final int RARITY = 5;
        public static final int GLYPHS = 6;
        public static final int RANK = 7;
        public static final int TYPE = 8;
        public static final int ZD_PR = 9;
        public static final int ATK_PR = 10;
        public static final int DEF_PR = 11;
        public static final int KRIT_S = 12;
        public static final int KRIT_V = 13;
        public static final int ZD = 14;
        public static final int ATK = 15;
        public static final int DEF = 16;
        public static final int SOP = 17;
        public static final int SKOR = 18;
        public static final int METK = 19;
        public static final int ID = 20;
        public static final int PARENT_ID = 24;
        public static final int FILTER_FLAG = 26;

    }

    public static class Sheets {
        public static final String FIND = "find";
        public static final String SETS = "Sets";
        public static final String ART = "Art";
        public static final String RESULT = "findData";
    }

    public static class Result {
        public static final int ALL_START_ROW = 1;
        public static final int ALL_START_CELL = 0;
        public static final int ALL_CNT_ROW = 10;
        public static final String ALL_BONUS_TEXT = "Бонусы";

        public static final int SUMMARY_START_ROW = 10;
        public static final int SUMMARY_START_CELL = 0;
        public static final int SUMMARY_VAL_COLL_START = 12;
        public static final int  SUMMARY_EFFECTIVE_VAL_COLL_START = 21;
    }
}
