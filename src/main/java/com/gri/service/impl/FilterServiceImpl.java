package com.gri.service.impl;

import com.gri.model.Attribute;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.service.FilterService;
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

    private final String EMPTY_CHARACTER_FIELDS_VALUE = "<<NONE>>";
    private final int calcAtrLimitCount;

    public FilterServiceImpl(int calcAtrLimitCount) {
        this.calcAtrLimitCount = calcAtrLimitCount;
    }

    @Override
    public Attribute[][] convertListToArray(Place[] places,
                                            List<Attribute> allAttributes,
                                            Character character) {
        final Character newCharacter = (character == null) ? new Character(EMPTY_CHARACTER_FIELDS_VALUE, EMPTY_CHARACTER_FIELDS_VALUE, EMPTY_CHARACTER_FIELDS_VALUE, 0) : character;
        final boolean checkFraction = character != null;

        Map<String, List<Attribute>> mapOfAttributes = Arrays.stream(places)
                .collect(Collectors.toMap(place -> place.name,
                        place -> getAttributeList(allAttributes, place.name, newCharacter, checkFraction && place.checkFraction)));

        return Arrays.stream(places)
                .map(place -> place.name)
                .peek(s -> logger.info("Загружено {} : {}", s, mapOfAttributes.get(s).size()))
                .map(s -> mapOfAttributes.get(s).stream()
                                .sorted(Comparator.comparingInt(o -> (newCharacter.name.equals(o.characterName) ? -1 : 1)))
//                        .sorted((o1, o2) -> (character.name.equals(o1.characterName) ? -1 : 1) - (character.name.equals(o2.characterName) ? -1 : 1))
                                .collect(Collectors.toList())
                                .toArray(new Attribute[0])
                )
                .collect(Collectors.toList())
                .toArray(new Attribute[0][0]);
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

    @Override
    public Attribute[][] filterAttributesByDoubles(Attribute[][] attributes) {
        Attribute[][] attributesTmp = Arrays.stream(attributes)
                .map(tmpAttributes -> Arrays.stream(tmpAttributes)
                        .filter(attribute1 -> Arrays.stream(tmpAttributes).noneMatch(attribute1::filter))
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

    @Override
    public Attribute[][] filterAttributesByDoublesAndMask(Attribute[][] attributes, double[] target) {
        Attribute[][] attributesTmp = Arrays.stream(attributes)
                .map(tmpAttributes -> Arrays.stream(tmpAttributes)
                        .filter(attribute1 -> Arrays.stream(tmpAttributes).noneMatch(attribute -> attribute1.filter(attribute, target)))
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

    public void setAttributeParentId(Attribute[][] attributes) {
        Arrays.stream(attributes)
                .forEach(tmpAttributes -> Arrays.stream(tmpAttributes)
                        .filter(attribute -> "".equals(attribute.characterName) && "0.0".equals(attribute.tmpCharacterName))
                        .forEach(attribute1 -> {
                                    double filterId = Arrays.stream(tmpAttributes)
                                            .filter(attribute -> "".equals(attribute.characterName) && "0.0".equals(attribute.tmpCharacterName))
                                            .filter(attribute1::filter)
                                            .map(attribute -> attribute.id)
                                            .findAny().orElse(-1d);

                                    if (filterId > 0) {
                                        attribute1.parentId = filterId;
                                    }
                                }
                        )
                );
    }

    private List<Attribute> getAttributeList(List<Attribute> allAttributes, String place, Character character, boolean checkFraction) {
        List<Attribute> result = allAttributes.stream()
                .filter(attribute -> place.equals(attribute.placeName))
                .filter(attribute -> attribute.filterFlag <= character.attributeFilterValue || character.name.equals(attribute.characterName))
                .filter(attribute -> !checkFraction || character.fraction.equals(attribute.type))
//                .filter(attribute -> character.name.equals(attribute.characterName) || attribute.id == -1)
//                .filter(attribute -> attribute.characterName == null || attribute.characterName.equals(""))
                .limit((calcAtrLimitCount > 0) ? calcAtrLimitCount : Integer.MAX_VALUE)
                .collect(Collectors.toList());
        if (result.size() == 0) {
            result = Collections.singletonList(new Attribute(place));
        }
        return result;
    }


}
