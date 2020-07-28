package com.gri.service.impl;

import com.gri.Main;
import com.gri.model.Attribute;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.service.FilterService;
import com.gri.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterServiceImpl implements FilterService {
    private static final Logger logger = LoggerFactory.getLogger(FilterServiceImpl.class);

    @Override
    public Attribute[][] startFilter(Place[] places,
                                     List<Attribute> allAttributes,
                                     Character character,
                                     double[] targetDelta) {
        Map<String, List<Attribute>> mapOfAttributes = Arrays.stream(places)
                .collect(Collectors.toMap(place -> place.name,
                        place -> getAttributeList(allAttributes, place.name, character, place.checkFraction)));

        Attribute[][] attributes = Arrays.stream(places)
                .map(place -> place.name)
                .peek(s -> logger.info("Загружено {} : {}", s, mapOfAttributes.get(s).size()))
                .map(s -> mapOfAttributes.get(s).stream()
                                .sorted(Comparator.comparingInt(o -> (character.name.equals(o.characterName) ? -1 : 1)))
//                        .sorted((o1, o2) -> (character.name.equals(o1.characterName) ? -1 : 1) - (character.name.equals(o2.characterName) ? -1 : 1))
                                .collect(Collectors.toList())
                                .toArray(new Attribute[0])
                )
                .collect(Collectors.toList())
                .toArray(new Attribute[0][0]);

        return filterAttributesByValues(attributes);
    }

    @Override
    public Attribute[][] getCharacterAttributes(double[] targetDelta, List<Attribute> allAttributes, Place[] places, Character character) {
        Map<String, List<Attribute>> mapOfAttributes = Arrays.stream(places)
                .collect(Collectors.toMap(place -> place.name,
                        place -> getAttributeList(allAttributes, place.name, character, place.checkFraction)));

        return Arrays.stream(places)
                .map(place -> place.name)
                .map(s -> mapOfAttributes.get(s).stream()
                        .filter(attribute -> character.name.equals(attribute.characterName))
                        .collect(Collectors.toList()).toArray(new Attribute[0])
                )
                .collect(Collectors.toList()).toArray(new Attribute[0][0]);
    }

    private Attribute[][] filterAttributesByValues(Attribute[][] attributes) {
        List<Attribute> filteredList = new ArrayList<>();

        Attribute[][] attributesTmp = Arrays.stream(attributes)
                .map(attribs -> Arrays.stream(attribs)
                                .filter(attribute1 -> {
                                            boolean filter = Arrays.stream(attribs).noneMatch(attribute1::filter);
//                                            double filterId = Arrays.stream(attribs)
//                                                    .filter(attribute1::filter)
//                                                    .map(attribute -> attribute.id)
//                                                    .findAny().orElse(-1d);
//                                            Attribute filterIdd = Arrays.stream(attribs)
//                                                    .filter(attribute1::filter)
//                                                    .findAny().orElse(null);
//                                    if (filterId > 0 && filter) {
//                                        filteredList.add(attribute1);
//                                    }
                                            if (!filter) { // !filter
//                                                attribute1.parentId = filterId;
                                                filteredList.add(attribute1);
                                            }
                                            return filter;
                                        }
                                )
                                .collect(Collectors.toList()).toArray(new Attribute[0])
                )
                .collect(Collectors.toList())
                .toArray(new Attribute[0][0]);

        List<String> stats = new ArrayList<>();
        int cnt = 0;
        for (int i = 0; i < attributes.length; i++) {
            cnt += attributes[i].length - attributesTmp[i].length;
            stats.add(Integer.toString(attributes[i].length - attributesTmp[i].length));
        }
        logger.info("filter allCnt = {}; Counts: {}", cnt, String.join(", ", stats));

        return attributesTmp;
    }

    private List<Attribute> getAttributeList(List<Attribute> allAttributes, String place, Character character, boolean checkFraction) {
        List<Attribute> result = allAttributes.stream()
                .filter(attribute -> place.equals(attribute.placeName))
                .filter(attribute -> attribute.filterFlag <= Main.ATTRIBUTE_FILTER_VALUE || character.name.equals(attribute.characterName))
                .filter(attribute -> !checkFraction || character.fraction.equals(attribute.type))
//                .filter(attribute -> character.name.equals(attribute.characterName) || attribute.id == -1)
//                .filter(attribute -> attribute.characterName == null || attribute.characterName.equals(""))
                .limit((Main.CALC_ATR_COUNT > 0) ? Main.CALC_ATR_COUNT : Integer.MAX_VALUE)
                .collect(Collectors.toList());
        if (result.size() == 0) {
            result = Collections.singletonList(new Attribute(place));
        }
        return result;
    }


//    public static void setParentId(List<Attribute> attributes) {
//        List<Attribute> filteredList = attributes.stream()
//                .filter(attribute -> attribute.characterName.equals(""))
//                .filter(attribute -> attribute.tmpCharacterName.equals("0.0"))
//                .collect(Collectors.toList());
//
//        filteredList.forEach(attribute1 -> {
//                    double filterId = filteredList.stream()
//                            .filter(attribute1::filter)
//                            .map(attribute -> attribute.id)
//                            .findAny().orElse(-1d);
//                    if (filterId > 0) {
//                        attribute1.parentId = filterId;
//                    }
//                }
//        );
//    }

}
