package com.gri.service.impl;

import com.gri.Main;
import com.gri.model.Attribute;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.model.Result;
import com.gri.service.BonusService;
import com.gri.service.FilterService;
import com.gri.utils.Constants;
import com.gri.utils.Utils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterTargetServiceImpl  {
    private static final Logger logger = LoggerFactory.getLogger(FilterTargetServiceImpl.class);
    private boolean checkEffectiveTarget = false;


    private final double[] targetDelta;

    private final Attribute[][] attributes;
    private double sumTargetPriority;
    private  Double[] maxTargetPriority;
    private Character character;
    private BonusService bonusService;
    private double[] baseAndLeagueAndZal;
    private double[] effectiveTarget;
    private int resultsLimitCnt;
    private Place[] places;

    public FilterTargetServiceImpl(double[] targetDelta,
                                   List<Attribute> allAttributes,
                                   Character character,
                                   BonusService bonusService,
                                   double[] baseAndLeagueAndZal,
                                   double[] effectiveTarget,
                                   int resultsLimitCnt,
                                   Place[] places
                                   ) {
        this.targetDelta = targetDelta;
        this.character = character;
        this.bonusService = bonusService;
        this.baseAndLeagueAndZal = baseAndLeagueAndZal;
        this.effectiveTarget = effectiveTarget;
        this.resultsLimitCnt = resultsLimitCnt;
        this.places = places;

        this.attributes = setTargetPriorityAndFilter(allAttributes);
    }

    public double getSumTargetPriority(){
        return this.sumTargetPriority;
    }

    public Attribute[][] getAttributes(){
        return this.attributes;
    }

    private Attribute[][] setTargetPriorityAndFilter(List<Attribute> allAttributes) {
        int startSize = allAttributes.size();
        List<Attribute> tmpAttributes = allAttributes
                .stream()
                .peek(attribute -> attribute.setTargetPriority(targetDelta))
                .filter(attribute -> attribute.targetPriority > 0)
                .collect(Collectors.toList());
        logger.info("filter by targetPriority = {};", startSize - tmpAttributes.size());

        Map<String, List<Attribute>> mapOfAttributes = Arrays.stream(places)
                .collect(Collectors.toMap(place -> place.name,
                        place -> getAttributeList(allAttributes, place.name, place.checkFraction)));

        List<Pair<String, Double>> listMax = Arrays.stream(places)
                .map(place -> new Pair<>(place.name, mapOfAttributes.get(place.name).stream()
                        .map(a -> a.targetPriority)
                        .max(Double::compareTo)
                        .orElse(0.0)))
//                .sorted(Comparator.comparingDouble(Pair::getValue))
                .sorted((p1,p2) -> Double.compare(p2.getValue(), p1.getValue()))
                .collect(Collectors.toList());

        this.sumTargetPriority = listMax.stream().map(Pair::getValue).reduce(Double::sum).orElse(0.0);

        this.maxTargetPriority = listMax.stream()
                .map(Pair::getValue)
                .toArray(Double[]::new);

        Attribute[][] tmpAttributesArray = listMax.stream()
                .map(p -> p.getKey())
                .map(s -> mapOfAttributes.get(s).toArray(new Attribute[0]))
                .collect(Collectors.toList()).toArray(new Attribute[0][0]);

        return filterAttributesByDoublesAndMask(tmpAttributesArray);
    }

    private Attribute[][] filterAttributesByDoublesAndMask(Attribute[][] attributes) {
        double[][] maxValues = new double[Constants.PLACES_COUNT][Constants.VAL_COUNT];
        for (int i = 0; i < attributes.length; i++) {
            for (int j = 0; j < attributes[i].length; j++) {
                for (int k = 0; k < targetDelta.length; k++) {
                    if (targetDelta[k] > 0) {
                        int place_id = attributes[i][j].place.id;
                        maxValues[place_id][k] = Math.max(maxValues[i][k], attributes[i][j].values[k]);
                    }
                }
            }
        }

        Attribute[][] attributesTmp = Arrays.stream(attributes)
                .map(tmpAttributes -> Arrays.stream(tmpAttributes)
                        .peek(attribute -> attribute.setMaxValuePriority(maxValues[attribute.place.id]))
                        .filter(attribute1 -> Arrays.stream(tmpAttributes).noneMatch(attribute -> attribute1.filter(attribute, targetDelta)))
                        .sorted()
                        .sorted(Comparator.comparingInt(o -> (character.name.equals(o.characterName) ? -1 : 1)))
                        .limit((long)character.priorityLimitFilterValue)
                        .collect(Collectors.toList()).toArray(new Attribute[0])
                )
//                .peek(l -> logger.info("{} Из {} Загружено {} ", l, mapOfAttributes.get(s).size(), l.length))
                .collect(Collectors.toList())
                .toArray(new Attribute[0][0]);

        List<String> stats = new ArrayList<>();
        int cnt = 0;
        for (int i = 0; i < attributes.length; i++) {
            cnt += attributes[i].length - attributesTmp[i].length;
            stats.add(Integer.toString(attributes[i].length - attributesTmp[i].length));
            logger.info("{}: final counts = {}", attributesTmp[i][0].placeName, attributesTmp[i].length);
        }
        logger.info("filter allCnt = {}; Counts: {}", cnt, String.join(", ", stats));

        return attributesTmp;
    }

    private List<Attribute> getAttributeList(List<Attribute> allAttributes, String place, boolean checkFraction) {
        List<Attribute> result = allAttributes.stream()
                .filter(attribute -> place.equals(attribute.placeName))
                .filter(attribute -> attribute.filterFlag <= character.attributeGroupFilterValue || character.name.equals(attribute.characterName))
                .filter(attribute -> !checkFraction || character.fraction.equals(attribute.type))
                .collect(Collectors.toList());
        if (result.size() == 0) {
            result = Collections.singletonList(new Attribute(place));
        }
        return result;
    }

    private void get0(List<Attribute> allAttributes, double[] targetDelta, Place[] places, Character character) {
        Map<String, List<Attribute>> mapOfAttributes = Arrays.stream(places)
                .collect(Collectors.toMap(place -> place.name,
                        place -> getAttributeList(allAttributes, place.name, place.checkFraction)));

        List<Pair<String, Double>> listMax = Arrays.stream(places)
                .map(place -> new Pair<>(place.name, mapOfAttributes.get(place.name).stream()
                        .map(a -> a.targetPriority)
                        .max(Double::compareTo)
                        .orElse(0.0)))
//                .sorted(Comparator.comparingDouble(Pair::getValue))
                .sorted((p1,p2) -> Double.compare(p2.getValue(), p1.getValue()))
                .collect(Collectors.toList());

        double sumTargetPriority = listMax.stream().map(Pair::getValue).reduce(Double::sum).orElse(0.0);

        Double[] maxTargetPriority = listMax.stream()
                .map(Pair::getValue)
                .toArray(Double[]::new);

        Attribute[][] attributes = listMax.stream()
                .map(p -> p.getKey())
                .map(s -> mapOfAttributes.get(s)
                        .toArray(new Attribute[0]))
                .collect(Collectors.toList())
                .toArray(new Attribute[0][0]);
        double rr = 30.0*30*30*30*30;
        double all = Arrays.stream(places)
                .map(place -> 1.0 * mapOfAttributes.get(place.name).size()).reduce((i1,i2) -> i1*i2).orElse(0.0);

        final long startTime = System.currentTimeMillis();
        long[] p = new long[9];
        long[] m = new long[9];
        getfor(0, new Attribute[9], attributes, maxTargetPriority, sumTargetPriority, p, m);
        double d = p[8] / all;
        logger.info("Executed time = {}", Main.getTime(System.currentTimeMillis() - startTime));
        int x = 0;
    }

    private void getfor(int index, Attribute[] setIn, Attribute[][] attributes, Double[] maxTargetPriority, double sumTargetPriority, long[] p, long[] m ) {
        Attribute[] set = Arrays.copyOf(setIn, setIn.length);

        for (int i = 0; i < attributes[index].length; i++) {
            if (index == 0){
                int x = 0;
            }

            double currentTargetPriority = attributes[index][i].targetPriority;
            double balancePriority = sumTargetPriority - maxTargetPriority[index] + currentTargetPriority;

            if (balancePriority > 1) {
                if (index < 8) {
                    set[index] = attributes[index][i];
                    getfor(index+1, set, attributes, maxTargetPriority, balancePriority, p, m);
                }
                else{
                    set[index] = attributes[index][i];
                    p[index] ++;
                }
            }
            else{
                set[index] = attributes[index][i];
                m[index] ++;
            }
        }
    }


    //        for (int i = 0; i < attributes.length; i++) {
//
//            for (int j = 0; j < attributes[i].length; j++) {
//                maxTargetPriority[i] = Math.max(maxTargetPriority[i], attributes[i][j].targetPriority);
//            }
//            sumTargetPriority += maxTargetPriority[i];
//            logger.info("i = {}; {}; max = {}", i, attributes[i][0].placeName, maxTargetPriority[i]);
//        }
//        logger.info("sumTargetPriority = {}", sumTargetPriority);

    public List<Result> startCalculation(int i, int j) {
        final Attribute[][] attribFiltered3 = new Attribute[attributes.length][];
        attribFiltered3[0] = new Attribute[]{attributes[0][i]};
        attribFiltered3[1] = new Attribute[]{attributes[1][j]};

        for (int k = 2; k < attributes.length; k++) {
            attribFiltered3[k] = new Attribute[attributes[k].length];
            System.arraycopy(attributes[k], 0, attribFiltered3[k], 0, attributes[k].length);
        }

        return  startCalculation(attribFiltered3);
    }

    public List<Result> startCalculation(Attribute[][] attribute) {
//        logger.info("start : {}", attribute[0][0]);
        List<Result> resultList = new ArrayList<>();
        return  startCalculation(0, attribute, new Attribute[0], resultList, sumTargetPriority);
    }

    private List<Result> startCalculation(int index, Attribute[][] calcAttributes, Attribute[] cortege, List<Result> resultList, double balancePriority) {
        if (calcAttributes != null && (resultsLimitCnt == 0 || resultList.size() <= resultsLimitCnt - 1)) {
//            long t0 = System.currentTimeMillis();
            double tmpTargetPriority = sumTargetPriority - maxTargetPriority[index];
            for (int i = 0; i < calcAttributes[index].length; i++) {
                double newBalancePriority = tmpTargetPriority + calcAttributes[index][i].targetPriority;

                if (newBalancePriority >= 1) {
                    if (index < Constants.PLACES_COUNT - 1) {

                        Attribute[] newCortege = Arrays.copyOf(cortege, index + 1);
                        newCortege[index] = (calcAttributes[index][i]);
                        Attribute[][] aa = filterByCortege(calcAttributes, newCortege);

//                    logger.info("filterByCortege index = {}; lengths: {}", index, Arrays.stream(aa).map(attributes -> "" + attributes.length).collect(Collectors.joining(", ")));
                        if (aa != null) {
                            startCalculation(index + 1, aa, newCortege, resultList, newBalancePriority);
                        }

                    } else {
                        Attribute[] newCortege = Arrays.copyOf(cortege, index + 1);
                        newCortege[index] = (calcAttributes[index][i]);
                        double[] bonuses = bonusService.getAttributeBonuses(newCortege);
                        double[] targetDeltaAndBonuses = Utils.getDelta(targetDelta, bonuses);

                        if (calcAttributeCortege(targetDeltaAndBonuses, newCortege)) {
                            resultList.add(new Result(newCortege, bonuses));
                        }
//                    logger.info("calcAttributeCortege time = {}s", (System.currentTimeMillis() - t0) / 1000);

                    }
                }
//                if (index == 2) {
//                    logger.info("2 : {}%; index = {}/{}; time = {}s; goodCnt = {}", 100 * i / attribute[index].length, i, attribute[index].length, (System.currentTimeMillis() - t0) / 1000, resultList.size());
//                }
//                if (index > 0 && index <= 1) {
//                    logger.info("{} : {}; index = {}/{}; time = {}s; goodCnt = {}", index, 100 * i / attribute[index].length, i, attribute[index].length, (System.currentTimeMillis() - t0) / 1000, resultList.size());
//                }
//                if (index == 0) {
//                    logger.info("progress = {}%; index = {}/{}; time = {}s; goodCnt = {}", 100 * i / attribute[0].length, i, attribute[0].length, (System.currentTimeMillis() - t0) / 1000, resultList.size());
//                }
            }
//            if (index == 0) {
//                logger.info("END; goodCnt = {}; time = {}", resultList.size(), (System.currentTimeMillis() - t0) / 1000);
//            }
        }
        return resultList;
    }

    private boolean calcAttributeCortege(double[] targetDeltaAndBonuses, Attribute... attributes) {
        double[] result = new double[Constants.VAL_COUNT];

        boolean good = true;
        int v = 0;
        while (good && v < Constants.VAL_COUNT) {
            result[v] = 0;
            for (Attribute attribute : attributes) {
                result[v] += attribute.values[v];
            }
            good = result[v] >= targetDeltaAndBonuses[v];
            v++;
        }
        if (good && checkEffectiveTarget) {
            good = checkCriticalEffectiveValue(good, result, Constants.Indexes.ZD, effectiveTarget[0]);
            good = checkCriticalEffectiveValue(good, result, Constants.Indexes.ATK, effectiveTarget[1]);
            good = checkCriticalEffectiveValue(good, result, Constants.Indexes.DEF, effectiveTarget[2]);
            good = checkEffectiveZdValue(good, result, effectiveTarget[3]);
        }

        return good;
    }

    private Attribute[][] filterAttributesRecursive(Attribute[][] attributes, double[] targetDeltaAndBonuses) {
        int endCnt = 0;
        for (int i = 0; i < Constants.PLACES_COUNT; i++) {  // по местам
            endCnt += attributes[i].length;
        }
        int startCnt = endCnt + 1;
        while (endCnt > 0 && endCnt < startCnt) {
            Attribute[][] tmpAttributes = new Attribute[Constants.PLACES_COUNT][];
            startCnt = endCnt;
            endCnt = 0;
            int[][] placesMax = getPlacesMax(attributes, targetDeltaAndBonuses);

            for (int i = 0; i < Constants.PLACES_COUNT; i++) {  // по местам
                tmpAttributes[i] = new Attribute[attributes[i].length];
                int cnt = 0;

                if (placesMax[i] != null) {
                    for (int j = 0; j < attributes[i].length; j++) { // по атрибутам
                        if (attributes[i][j].filter(placesMax[i])) {
                            tmpAttributes[i][cnt] = attributes[i][j];
                            cnt++;
                        }
                    }
                    endCnt += cnt;
                    tmpAttributes[i] = Arrays.copyOf(tmpAttributes[i], cnt);
                }
                else{
                    endCnt = attributes[i].length;
                    tmpAttributes[i] = attributes[i];
                }
            }
            attributes = tmpAttributes;
        }
        return attributes;
    }

    private Attribute[][] filterByCortege(Attribute[][] attribute, Attribute[] cortege) {
        Attribute[][] result = new Attribute[Constants.PLACES_COUNT][];
        for (int i = 0; i < Constants.PLACES_COUNT; i++) {
            if (i < cortege.length) {
                result[i] = new Attribute[]{cortege[i]};
            } else {
                result[i] = attribute[i];
            }
        }
        return filterAttributesRecursive(result, Utils.getDelta(targetDelta, bonusService.getAttributeBonuses(cortege)));
    }

    private int[][] getPlacesMax(Attribute[][] attributes, double[] targetDeltaAndBonuses) {
        int[][] placesMax = new int[Constants.PLACES_COUNT][Constants.VAL_COUNT];
        int[] valuesMax = new int[Constants.VAL_COUNT];
        for (int i = 0; i < Constants.VAL_COUNT; i++) { // по хар-кам
            if (targetDeltaAndBonuses[i] > 0) {
                for (int j = 0; j < Constants.PLACES_COUNT; j++) {  // по местам
                    double max = 0;
                    for (int k = 0; k < attributes[j].length; k++) { // по атрибутам
                        max = Math.max(attributes[j][k].values[i], max);
                    }
                    placesMax[j][i] = (int) max;
                    valuesMax[i] += (int) max;
                }
                valuesMax[i] = ((int) targetDeltaAndBonuses[i]) - valuesMax[i];

                for (int j = 0; j < Constants.PLACES_COUNT; j++) { // по местам
                    placesMax[j][i] += valuesMax[i];
                }
            }
        }

        for (int j = 0; j < Constants.PLACES_COUNT; j++) {
            boolean setNull = true;
            for (int i = 0; i < Constants.VAL_COUNT; i++) {
                setNull = setNull && placesMax[j][i] <= 0;
            }
            if (setNull){
                placesMax[j] = null;
            }
        }

        return placesMax;
    }

    private boolean checkCriticalEffectiveValue(boolean good, double[] result, int v_index, double checkValue) {
        if (good) {
            if (checkValue > 0) {
//                double value = baseAndLeagueAndZal[v_index] + result[v_index];
//                double krit = baseAndLeagueAndZal[Constants.Indexes.KRIT_V] + result[Constants.Indexes.KRIT_V];
                return getCriticalEffectiveValue(v_index, result) >= checkValue;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    private boolean checkEffectiveZdValue(boolean good, double[] result, double checkValue) {
        if (good) {
            if (checkValue > 0) {
//                double zd = baseAndLeagueAndZal[Constants.Indexes.ZD] + result[Constants.Indexes.ZD];
//                double def = baseAndLeagueAndZal[Constants.Indexes.DEF] + result[Constants.Indexes.DEF];
                return getEffectiveZdValue(result) >= checkValue;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private double getCriticalEffectiveValue(int v_index, double[] result) {
        double value = baseAndLeagueAndZal[v_index] + result[v_index];
        double criticalValue = baseAndLeagueAndZal[Constants.Indexes.KRIT_V] + result[Constants.Indexes.KRIT_V];

        return Utils.getCriticalEffectiveValue(value , criticalValue);
    }

    private double getEffectiveZdValue(double[] result) {
        double zd = baseAndLeagueAndZal[Constants.Indexes.ZD] + result[Constants.Indexes.ZD];
        double def = baseAndLeagueAndZal[Constants.Indexes.DEF] + result[Constants.Indexes.DEF];

        return Utils.getEffectiveZdValue(zd, def);
    }

}
