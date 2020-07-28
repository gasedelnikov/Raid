package com.gri.model;

import com.gri.utils.Constants;
import com.gri.utils.XssfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Attribute {
    private static Logger logger = LoggerFactory.getLogger(XssfUtils.class);

    public double id;
    public String characterName;
    public String tmpCharacterName;
    public String placeName;
    public String rarity;
    public double glyphsIndex;
    public double rank;
    public String type;
    public Bonus bonus;
    public double bonusType = 0;
    public double filterFlag;
    public Place place;
    public double parentId;

    public double[] values;

    public Attribute(double id, String characterName, String placeName, String rarity, double glyphsIndex, double rank, String type, Bonus bonus, double filterFlag, double[] values) {
        this.id = id;
        this.characterName = characterName;
        this.placeName = placeName;
        this.rarity = rarity;
        this.glyphsIndex = glyphsIndex;
        this.rank = rank;
        this.type = type;
        this.bonus = bonus;
        this.bonusType = (bonus != null) ? bonus.type : 0;
        this.filterFlag = filterFlag;
        this.values = values;
    }

    public Attribute(String placeName) {
        this.id = -1;
        this.placeName = placeName;
        this.place = new Place(placeName, true, 0, null, null);
        this.type = "Заглушка";
        this.characterName = this.type;
        this.glyphsIndex = Double.MAX_VALUE;
        this.bonus = null;
        this.bonusType = 0;
        this.values = new double[Constants.ATR_VALUES_COUNT];
    }

    public boolean filter(int[] filterValues) {
        int i = 0;
        boolean result = true;
        while (i < values.length && result) {
            result = values[i] >= filterValues[i];
            i++;
        }
        return result;
    }

    public boolean filter(double[] filterValues) {
        int i = 0;
        boolean result = true;
        while (i < values.length && result) {
//            result = values[i] >= Math.floor(filterValues[i]);
            result = values[i] >= (int)filterValues[i];
//            result = values[i] >= filterValues[i];
            i++;
        }
        return result;
    }

    public boolean filter(Attribute attribute) {
        int i = 0;
        boolean greatOrEquals = true;
        boolean equals = true;
        if (this.id != attribute.id && this.bonusType == attribute.bonusType && this.placeName.equals(attribute.placeName)) {
            while (i < this.values.length && greatOrEquals) {
                greatOrEquals = attribute.values[i] >= this.values[i];
                equals = equals && attribute.values[i] == this.values[i];
                i++;
            }
            if (equals) {
                greatOrEquals = this.id > attribute.id;
            }
//            if (greatOrEquals) {
//                List<String> s1 = Arrays.stream(attribute.values).mapToObj(Double::toString).collect(Collectors.toList());
//                List<String> s2 = Arrays.stream(values).mapToObj(Double::toString).collect(Collectors.toList());
//                logger.info("1 id = {}; bonusType = {}; s1 = {}", attribute.id, attribute.bonusType, s1);
//                logger.info("2 id = {}; bonusType = {}; s2 = {}", id, bonusType, s2);
//                logger.info("");
//            }
        } else {
            greatOrEquals = false;
        }
        return greatOrEquals;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", characterName='" + characterName + '\'' +
                ", place='" + placeName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
