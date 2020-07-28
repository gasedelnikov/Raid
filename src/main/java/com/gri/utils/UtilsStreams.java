package com.gri.utils;

import com.gri.Main;
import com.gri.model.Attribute;
import com.gri.model.Character;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UtilsStreams {
    private static Logger logger = LoggerFactory.getLogger(UtilsStreams.class);

    public static Map<String, List<Attribute>> filterAttributesRecursive(Map<String, List<Attribute>> attributes, double[] targetDelta) {
        int startCnt = 2;
        int endCnt = 1;
        Map<String, List<Attribute>> result = attributes;
        while (endCnt > 0 && endCnt < startCnt) {
            startCnt = getSumCount(result);
            result = filterAttributes(result, targetDelta);
            endCnt = getSumCount(result);
            logger.info("filterAttributesRecursive: startCnt= {}; endCnt = {}", startCnt, endCnt);
        }
        return result;
    }

    public static Map<String, List<Attribute>> filterAttributes(Map<String, List<Attribute>> attributes, double[] targetDelta) {
        double[] maxSum = attributes.values().stream()
                .map(UtilsStreams::getMax)
                .reduce(Utils::getSum)
                .orElse(null);

        targetDelta = Utils.getDelta(targetDelta, maxSum);

        Map<String, double[]> mapOfMax = attributes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> getMax(entry.getValue())));

        Map<String, double[]> mapOfFilter = getAttribFilter(targetDelta, mapOfMax);

        return attributes.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey
                        , entry -> entry.getValue().stream()
                                .filter(attribute -> attribute.filter(mapOfFilter.get(entry.getKey())))
                                .collect(Collectors.toList())
                ));
    }

    public static Map<String, double[]> getAttribFilter(double[] targetDelta, Map<String, double[]> mapOfMax) {
        return mapOfMax.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Utils.getSum(targetDelta, entry.getValue())));
    }

    public static double[] getMax(List<Attribute> attribs) {
        return attribs.stream()
                .map(attribute -> attribute.values)
                .reduce(Utils::getMax)
                .orElse(null);
    }

    public static List<Attribute> getAttributeList(List<Attribute> allAttributes, String place, Character character, boolean checkFraction) {
        List<Attribute> result = allAttributes.stream()
                .filter(attribute -> place.equals(attribute.placeName))
                .filter(attribute -> attribute.filterFlag <= Main.ATTRIBUTE_FILTER_VALUE || character.name.equals(attribute.characterName))
                .filter(attribute -> !checkFraction || character.fraction.equals(attribute.type))
//                .filter(attribute -> character.name.equals(attribute.characterName) || attribute.id == -1)
//                .filter(attribute -> attribute.characterName == null || attribute.characterName.equals(""))
                .limit((Main.CALC_ATR_COUNT > 0) ? Main.CALC_ATR_COUNT : Integer.MAX_VALUE)
                .collect(Collectors.toList());
        if (result.size() == 0){
            result = Collections.singletonList(new Attribute(place));
        }
        return result;
    }

    public static int getSumCount(Map<String, List<Attribute>> attributes) {
        return attributes.entrySet().stream()
                .map(entry -> entry.getValue().size())
                .reduce((i, j) -> (i == 0 || j == 0) ? 0 : i + j)
                .orElse(0);
    }

    public static void printSize(Map<String, List<Attribute>> attributes) {
        logger.info(
                attributes.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(entry -> entry.getKey() + ": " + entry.getValue().size())
                        .collect(Collectors.joining("; "))
        );
    }
}
