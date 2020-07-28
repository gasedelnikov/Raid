package com.gri.service;

import com.gri.Main;
import com.gri.data.DataServiceImpl;
import com.gri.model.Attribute;
import com.gri.utils.Constants;
import com.gri.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CalculationService0 {
    private static Logger logger = LoggerFactory.getLogger(CalculationService0.class);

    public static void startCalculation(int index, Attribute[][] attribute, double[] targetDelta, Attribute[] cortege) {
        if (attribute != null && (Main.goodMax == 0 || Main.goodIndex <= Main.goodMax - 1)) {
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
                if (index < 40) {
                    logger.info("2 = {}%; index = {}/{}; time = {}s; goodCnt = {}", 100 * i / attribute[index].length, i, attribute[index].length, (System.currentTimeMillis() - t0) / 1000, Main.goodIndex);
                }
                if (index == 1) {
                    logger.info("1 = {}%; index = {}/{}; time = {}s; goodCnt = {}", 100 * i / attribute[index].length, i, attribute[index].length, (System.currentTimeMillis() - t0) / 1000, Main.goodIndex);
                }
                if (index == 0) {
                    logger.info("progress = {}%; index = {}/{}; time = {}s; goodCnt = {}", 100 * i / attribute[0].length, i, attribute[0].length, (System.currentTimeMillis() - t0) / 1000, Main.goodIndex);
                }
            }
            if (index == 0) {
                logger.info("END; goodCnt = {}; time = {}", Main.goodIndex, (System.currentTimeMillis() - t0) / 1000);
            }
        }
    }

    public static boolean calcAttributeCortege(double[] targetDelta, Attribute... attributes) {
        double[] delta = new double[Constants.VAL_COUNT];
        double[] bonuses = BonusService.getAttributeBonuses(attributes);
        targetDelta = Utils.getDelta(targetDelta, bonuses);

        boolean good = true;
        int v = 0;
        while (good && v < Constants.VAL_COUNT) {
            double sum = 0;
            for (Attribute attribute : attributes) {
                sum += attribute.values[v];
            }
            delta[v] = sum - targetDelta[v];
            good = sum >= targetDelta[v];
            v++;
        }
        if (good) {
            DataServiceImpl.putResult(Main.workbook, attributes, bonuses);
            Main.goodIndex++;
            logger.info("good {}: {}", Main.goodIndex, Arrays.stream(delta).boxed().map(d -> Integer.toString(d.intValue())).collect(Collectors.joining("; ")));
//            logger.info("good: {}", Arrays.stream(attributes).map(Attribute::toString).collect(Collectors.joining("; ")));
        }
        return good;
    }

    public static Attribute[][] filterAttributesRecursive2(Attribute[][] attributes, double[] targetDelta) {
        return attributes;
    }

    public static Attribute[][] filterAttributesRecursive(Attribute[][] attributes, double[] targetDelta) {
        Attribute[][] result = new Attribute[Constants.PLACES_COUNT][];
        int endCnt = 0;
        for (int i = 0; i < Constants.PLACES_COUNT; i++) {  // по местам
            endCnt += attributes[i].length;
        }
        int startCnt = endCnt + 1;

        while (attributes != null && endCnt > 0 && endCnt < startCnt) {
            startCnt = endCnt;
            double[][] placesMax = getPlacesMax(attributes, targetDelta);

            List<List<Attribute>> attributesFiltered = new ArrayList<>(Constants.PLACES_COUNT);
            for (int i = 0; i < Constants.PLACES_COUNT; i++) {  // по местам
                List<Attribute> tmp = new ArrayList<>(attributes[i].length);
                for (int j = 0; j < attributes[i].length; j++) { // по атрибутам
                    if (attributes[i][j].filter(placesMax[i])) {
                        tmp.add(attributes[i][j]);
                    }
                }
                attributesFiltered.add(tmp);
            }

            endCnt = 0;
            for (int i = 0; i < Constants.PLACES_COUNT; i++) {  // по местам
                int size = attributesFiltered.get(i).size();
                if (result != null &&  size > 0) {
                    result[i] = attributesFiltered.get(i).toArray(new Attribute[0]);
                    endCnt += size;
                }
                else{
                    result = null;
                }
            }

        }
        return result;
    }

    public static Attribute[][] filterByCortege(double[] targetDelta, Attribute[][] attribute, Attribute[] cortege) {
        Attribute[][] result = new Attribute[Constants.PLACES_COUNT][];
        for (int i = 0; i < Constants.PLACES_COUNT; i++) {
            if (i < cortege.length) {
                result[i] = new Attribute[]{cortege[i]};
            } else {
                result[i] = attribute[i];
            }
        }
        return filterAttributesRecursive(result, Utils.getDelta(targetDelta, BonusService.getAttributeBonuses(cortege)));
    }

    private static double[][] getPlacesMax(Attribute[][] attributes, double[] targetDelta) {
        double[][] placesMax = new double[Constants.PLACES_COUNT][Constants.VAL_COUNT];
        double[] valuesMax = new double[Constants.VAL_COUNT];
        for (int i = 0; i < Constants.VAL_COUNT; i++) { // по хар-кам
            for (int j = 0; j < Constants.PLACES_COUNT; j++) {  // по местам
                double max = 0;
                for (int k = 0; k < attributes[j].length; k++) { // по атрибутам
                    max = Math.max(max, attributes[j][k].values[i]);
                }
                placesMax[j][i] = max;
                valuesMax[i] += max;
            }
            valuesMax[i] = targetDelta[i] - valuesMax[i];
            for (int j = 0; j < Constants.PLACES_COUNT; j++) { // по местам
                placesMax[j][i] += valuesMax[i];
            }
        }
        return placesMax;
    }
}
