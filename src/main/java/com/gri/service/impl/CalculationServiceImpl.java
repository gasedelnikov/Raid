package com.gri.service.impl;

import com.gri.Main;
import com.gri.model.Attribute;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.model.Result;
import com.gri.service.BonusService;
import com.gri.service.CalculationService;
import com.gri.utils.Constants;
import com.gri.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CalculationServiceImpl implements CalculationService {
    private static Logger logger = LoggerFactory.getLogger(CalculationServiceImpl.class);

    private List<Result> resultList = new ArrayList<>();
    private boolean checkEffectiveTarget = false;

    private final BonusService bonusService;
    private final double[] baseAndLeagueAndZal;
    private final double[] effectiveTarget;

    public CalculationServiceImpl(BonusService bonusService, double[] baseAndLeagueAndZal, double[] effectiveTarget) {
        this.bonusService = bonusService;
        this.baseAndLeagueAndZal = baseAndLeagueAndZal;
        this.effectiveTarget = effectiveTarget;

        for (double v : effectiveTarget) {
            checkEffectiveTarget = checkEffectiveTarget || v > 0;
        }
    }

    @Override
    public List<Result> startCalculation(int index, Attribute[][] attribute, double[] targetDelta, Attribute[] cortege) {
        if (attribute != null && (Main.RESULTS_LIMIT_CNT == 0 || resultList.size() <= Main.RESULTS_LIMIT_CNT - 1)) {
            long t0 = System.currentTimeMillis();
            for (int i = 0; i < attribute[index].length; i++) {
                if (index < Constants.PLACES_COUNT - 1) {
                    Attribute[] newCortege = Arrays.copyOf(cortege, index + 1);
                    newCortege[index] = (attribute[index][i]);
                    Attribute[][] aa = filterByCortege(targetDelta, attribute, newCortege);

//                    logger.info("filterByCortege index = {}; lengths: {}", index, Arrays.stream(aa).map(attributes -> "" + attributes.length).collect(Collectors.joining(", ")));
                    if (aa != null) {
                        startCalculation(index + 1, aa, targetDelta, newCortege);
                    }
                } else {
                    Attribute[] newCortege = Arrays.copyOf(cortege, index + 1);
                    newCortege[index] = (attribute[index][i]);
                    calcAttributeCortege(targetDelta, newCortege);
//                    logger.info("calcAttributeCortege time = {}s", (System.currentTimeMillis() - t0) / 1000);

                }
//                if (index == 2) {
//                    logger.info("2 : {}%; index = {}/{}; time = {}s; goodCnt = {}", 100 * i / attribute[index].length, i, attribute[index].length, (System.currentTimeMillis() - t0) / 1000, resultList.size());
//                }
//                if (index > 0 && index <= 1) {
//                    logger.info("{} : {}; index = {}/{}; time = {}s; goodCnt = {}", index, 100 * i / attribute[index].length, i, attribute[index].length, (System.currentTimeMillis() - t0) / 1000, resultList.size());
//                }
                if (index == 0) {
                    logger.info("progress = {}%; index = {}/{}; time = {}s; goodCnt = {}", 100 * i / attribute[0].length, i, attribute[0].length, (System.currentTimeMillis() - t0) / 1000, resultList.size());
                }
            }
            if (index == 0) {
                logger.info("END; goodCnt = {}; time = {}", resultList.size(), (System.currentTimeMillis() - t0) / 1000);
            }
        }
        return resultList;
    }

    @Override
    public boolean calcAttributeCortege(double[] targetDelta, Attribute... attributes) {
        double[] result = new double[Constants.VAL_COUNT];
        double[] bonuses = bonusService.getAttributeBonuses(attributes);
        targetDelta = Utils.getDelta(targetDelta, bonuses);

        boolean good = true;
        int v = 0;
        while (good && v < Constants.VAL_COUNT) {
            result[v] = 0;
            for (Attribute attribute : attributes) {
                result[v] += attribute.values[v];
            }
            good = result[v] >= targetDelta[v];
            v++;
        }
        if (good && checkEffectiveTarget) {
            good = checkKritEffectiveValue(good, result, Constants.Indexes.ZD, effectiveTarget[0]);
            good = checkKritEffectiveValue(good, result, Constants.Indexes.ATK, effectiveTarget[1]);
            good = checkKritEffectiveValue(good, result, Constants.Indexes.DEF, effectiveTarget[2]);
            good = checkEffectiveZdValue(good, result, effectiveTarget[3]);
        }

        if (good) {
            resultList.add(new Result(attributes, bonuses));
            logger.info("good {}: {}", resultList.size(), Arrays.stream(result).boxed().map(d -> Integer.toString(d.intValue())).collect(Collectors.joining("; ")));
//            logger.info("good: {}", Arrays.stream(attributes).map(Attribute::toString).collect(Collectors.joining("; ")));
        }
        return good;
    }

    @Override
    public Attribute[][] filterAttributesRecursive(Attribute[][] attributes, double[] targetDelta) {
        int endCnt = 0;
        for (int i = 0; i < Constants.PLACES_COUNT; i++) {  // по местам
            endCnt += attributes[i].length;
        }
        int startCnt = endCnt + 1;
        while (endCnt > 0 && endCnt < startCnt) {
            Attribute[][] tmpAttributes = new Attribute[Constants.PLACES_COUNT][];
            startCnt = endCnt;
            endCnt = 0;
            int[][] placesMax = getPlacesMax(attributes, targetDelta);

            for (int i = 0; i < Constants.PLACES_COUNT; i++) {  // по местам
                tmpAttributes[i] = new Attribute[attributes[i].length];
                int cnt = 0;
                for (int j = 0; j < attributes[i].length; j++) { // по атрибутам
                    if (attributes[i][j].filter(placesMax[i])) {
                        tmpAttributes[i][cnt] = attributes[i][j];
                        cnt++;
                    }
                }
                endCnt += cnt;
                tmpAttributes[i] = Arrays.copyOf(tmpAttributes[i], cnt);
            }
            attributes = tmpAttributes;
        }
        return attributes;
    }

    @Override
    public Attribute[][] filterByCortege(double[] targetDelta, Attribute[][] attribute, Attribute[] cortege) {
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

    private int[][] getPlacesMax(Attribute[][] attributes, double[] targetDelta) {
        int[][] placesMax = new int[Constants.PLACES_COUNT][Constants.VAL_COUNT];
        int[] valuesMax = new int[Constants.VAL_COUNT];
        for (int i = 0; i < Constants.VAL_COUNT; i++) { // по хар-кам
            for (int j = 0; j < Constants.PLACES_COUNT; j++) {  // по местам
                double max = 0;
                for (int k = 0; k < attributes[j].length; k++) { // по атрибутам
                    max = Math.max(attributes[j][k].values[i], max);
                }
                placesMax[j][i] = (int) max;
                valuesMax[i] += max;
            }
            valuesMax[i] = ((int) targetDelta[i]) - valuesMax[i];
            for (int j = 0; j < Constants.PLACES_COUNT; j++) { // по местам
                placesMax[j][i] += valuesMax[i];
            }
        }
        return placesMax;
    }

    private boolean checkKritEffectiveValue(boolean good, double[] result, int v_index, double checkValue) {
        if (good) {
            if (checkValue > 0) {
                double value = baseAndLeagueAndZal[v_index] + result[v_index];
                double krit = baseAndLeagueAndZal[Constants.Indexes.KRIT_V] + result[Constants.Indexes.KRIT_V];
                return value * (1 + krit / 100) >= checkValue;
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
                double zd = baseAndLeagueAndZal[Constants.Indexes.ZD] + result[Constants.Indexes.ZD];
                double def = baseAndLeagueAndZal[Constants.Indexes.DEF] + result[Constants.Indexes.DEF];

                return zd * (1 + def / 600) >= checkValue;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }


}
