package com.gri.model;

import com.gri.utils.Constants;
import com.gri.utils.XssfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Attribute implements Comparable{
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
    public double bonusType;
    public double filterFlag;
    public Place place;
    public double parentId;
    public int mainIndex;

    public double targetPriority;
    public double maxValuePriority;

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
        this.place = new Place(0, placeName, true, 0, null, null);
        this.type = "Заглушка";
        this.characterName = this.type;
        this.glyphsIndex = Double.MAX_VALUE;
        this.bonus = null;
        this.bonusType = 0;
        this.values = new double[Constants.ATR_VALUES_COUNT];
    }

    public void setTargetPriority(double[] target) {
        int targets = 0;
        for (int i = 0; i < values.length; i++) {
            if (target[i] > 0) {
                double bonusAdd = (bonus == null) ? 0 : bonus.values[i] * bonus.quantum;
                this.targetPriority += (values[i] + bonusAdd) / target[i];
                targets++;
            }
        }
        this.targetPriority /= targets;
    }

    public void setMaxValuePriority(double[] target) {
        for (int i = 0; i < values.length; i++) {
            if (target[i] > 0) {
                double bonusAdd = (bonus == null) ? 0 : bonus.values[i] * bonus.quantum;
                this.maxValuePriority += (values[i] + bonusAdd) / target[i];
            }
        }
    }

    public boolean filter(int[] filterValues) {
        int i = 0;
        boolean result;

        do {
            result = filterValues[i] <= 0 || values[i] >= filterValues[i];
            i++;
        }
        while (result && i < Constants.ATR_VALUES_COUNT && i < filterValues.length);

        return result;
    }

    public boolean filterByMask(int[] filterValues) {
        int i = 0;
        boolean result;

        do {
            result = filterValues[i] > 0 && values[i] >= filterValues[i];
            i++;
        }
        while (!result && i < Constants.ATR_VALUES_COUNT && i < filterValues.length);

        return result;
    }

    public boolean filter(Attribute attribute) {
        int i = 0;
        boolean greatOrEquals = true;
        boolean equals = true;
        if (this.id != attribute.id &&
                this.bonusType == attribute.bonusType &&
                this.placeName.equals(attribute.placeName) &&
                (!this.place.checkFraction || this.type.equals(attribute.type))) {
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

    public boolean filter(Attribute attribute, double[] mask) {
        int i = 0;
        boolean greatOrEquals = true;
        boolean equals = true;
        if (this.id != attribute.id &&
                this.bonusType == attribute.bonusType &&
                this.placeName.equals(attribute.placeName) &&
                (!this.place.checkFraction || this.type.equals(attribute.type))) {
            while (i < this.values.length && greatOrEquals) {
                greatOrEquals = mask[i] <= 0 || attribute.values[i] >= this.values[i];
                equals = equals && (mask[i] <= 0 || attribute.values[i] == this.values[i]);
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

    @Override
    public int compareTo(Object o) {
        return Double.compare(((Attribute)o).maxValuePriority, this.maxValuePriority);
    }
}
