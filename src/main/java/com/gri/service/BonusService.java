package com.gri.service;

import com.gri.Main;
import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.utils.Constants;
import com.gri.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BonusService {
    private static Logger logger = LoggerFactory.getLogger(BonusService.class);

    public static double[] getAttributeBonusesTest(Attribute... attributes) {
        double[] newR = getAttributeBonuses(attributes);
        double[] oldR = getAttributeBonusesOld(attributes);

        for (int i = 0; i < Constants.BONUSES_VAL_COUNT; i++) {
            if (newR[i] != oldR[i]) {
                logger.info("alarm");

                double[] newR2 = getAttributeBonuses(attributes);
                double[] oldR2 = getAttributeBonusesOld(attributes);
            }
        }

        return newR;
    }

    public static double[] getAttributeBonuses(Attribute... attributes) {
        int pairCnt = 0;
        int attributeCnt = 0;
        int krazaCnt = 0;

        double[] result = new double[Constants.BONUSES_VAL_COUNT];
        Map<Bonus, Integer> bonusMap = new HashMap<>();
        for (Attribute attribute : attributes) {
            if (!attribute.place.checkFraction) {
                attributeCnt++;
                if (attribute.bonus != null) {
                    if (attribute.bonus.values != null && attribute.bonus.values[Constants.Indexes.KRAZA] > 0) {
                        krazaCnt++;
                    }

                    Bonus bonus = attribute.bonus;
                    if (bonusMap.containsKey(bonus)) {
                        bonusMap.put(bonus, bonusMap.get(bonus) + 1);
                    } else {
                        bonusMap.put(bonus, 1);
                    }
                }
            }
        }
        for (Map.Entry<Bonus, Integer> entry : bonusMap.entrySet()) {
            int quantum = (int) (entry.getValue() * entry.getKey().quantum);
            if (quantum >= 1) {
                pairCnt += quantum / entry.getKey().quantum;
                for (int i = 0; i < Constants.BONUSES_VAL_COUNT; i++) {
                    result[i] += quantum * entry.getKey().values[i];
                }
            }
        }

        int skips = (attributeCnt > 6) ? 0 : 6 - attributeCnt;
        if (skips >= 1) {
            double[] values = Main.possibleBonusesForSkips.get(0.5);
            int m = Math.min(skips, (int) (6 - pairCnt * 0.5));
            if (m >= 1) {
                for (int i = 0; i < Constants.BONUSES_VAL_COUNT; i++) {
                    result[i] += m * values[i];
                }
            }
        }
        if (skips + krazaCnt >= 4) {
            result[Constants.Indexes.KRAZA] = 1;
        }
        return result;
    }

    public static double[] getAttributeBonusesOld(Attribute... attributes) {
        List<Attribute> attributesList = Arrays.stream(attributes)
                .filter(attribute -> !attribute.place.checkFraction)
                .collect(Collectors.toList());

        final AtomicInteger pairCnt = new AtomicInteger(0);

        double[] result = attributesList.stream()
                .filter(attribute -> attribute.bonus != null)
                .map(attribute -> attribute.bonus)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .peek(entry -> entry.setValue((long) (entry.getKey().quantum * entry.getValue().intValue())))
                .filter(entry -> entry.getValue() >= 1)
                .peek(entry -> pairCnt.set(pairCnt.intValue() + (int) (entry.getValue() / entry.getKey().quantum)))
                .map(entry -> Utils.getMultiple(entry.getValue(), entry.getKey().values))
                .reduce(Utils::getSum).orElse(new double[Constants.BONUSES_VAL_COUNT]);

        int skips = (attributesList.size() > 6) ? 0 : 6 - attributesList.size();

        if (skips >= 1) {
            double[] values = Main.possibleBonusesForSkips.get(0.5);
            int m = Math.min(skips, (int) (6 - pairCnt.intValue() * 0.5)); //entry.getKey()
            if (m >= 1) {
                for (int i = 0; i < Constants.BONUSES_VAL_COUNT; i++) {
                    result[i] += m * values[i]; //entry.getValue()
                }
            }
        }

        long krazaCnt = skips + attributesList.stream()
                .filter(attribute -> attribute.bonus != null && attribute.bonus.values != null)
                .filter(attribute -> attribute.bonus.values[Constants.Indexes.KRAZA] > 0)
                .count();
        if (krazaCnt >= 4) {
            result[Constants.Indexes.KRAZA] = 1;
        }

        return result;
    }


}
