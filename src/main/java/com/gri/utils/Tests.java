package com.gri.utils;

import com.gri.model.Attribute;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.service.BonusService;
import com.gri.service.CalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Tests {
    private static Logger logger = LoggerFactory.getLogger(Tests.class);

    public static void test_character(double[] targetDelta, List<Attribute> allAttributes, Place[] places, Character character) {
        Map<String, List<Attribute>> mapOfAttributes = Arrays.stream(places)
                .collect(Collectors.toMap(place -> place.name,
                        place -> UtilsStreams.getAttributeList(allAttributes, place.name, character, place.checkFraction)));

        Attribute[][] attributes = Arrays.stream(places)
                .map(place -> place.name)
                .map(s -> mapOfAttributes.get(s).stream()
                        .filter(attribute -> character.name.equals(attribute.characterName))
                        .collect(Collectors.toList()).toArray(new Attribute[0])
                )
                .collect(Collectors.toList()).toArray(new Attribute[0][0]);

        attributes = CalculationService.filterAttributesRecursive(attributes, Utils.getDelta(targetDelta, BonusService.getAttributeBonuses()));
        CalculationService.startCalculation(0, attributes, targetDelta, new Attribute[0]);
        System.out.println("");
    }

    public static void test_test(double[] targetDelta, Attribute[][] attributes) {
        Attribute a0 = attributes[0][0];
        Attribute a1 = attributes[1][0];
        Attribute a2 = attributes[2][0];
        Attribute a3 = attributes[3][0];
        Attribute a4 = attributes[4][0];
        Attribute a5 = attributes[5][0];

        attributes[0] = new Attribute[]{a0};
        attributes[1] = new Attribute[]{a1};
        attributes[2] = new Attribute[]{a2};
        attributes[3] = new Attribute[]{a3};
        attributes[4] = new Attribute[]{a4};
        double[] bb = BonusService.getAttributeBonuses(a0, a1, a2, a3, a4);
        double[] tmpTargetDelta = Utils.getDelta(targetDelta, bb);
        Attribute[][] attributes2 = CalculationService.filterAttributesRecursive(attributes, tmpTargetDelta);
        System.out.println("");
    }


}
