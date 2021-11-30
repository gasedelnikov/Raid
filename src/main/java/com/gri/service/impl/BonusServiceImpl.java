package com.gri.service.impl;

import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.service.BonusService;
import com.gri.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BonusServiceImpl implements BonusService {
    private static Logger logger = LoggerFactory.getLogger(BonusServiceImpl.class);

    private static final int ATTRIBUTE_CNT_MAX = 6;

    private Map<Double, double[]> possibleBonusesForSkips;

    public BonusServiceImpl(Map<Double, double[]> possibleBonusesForSkips) {
        this.possibleBonusesForSkips = possibleBonusesForSkips;
    }

    @Override
    public double[] getAttributeBonuses(Attribute... attributes) {
        int pairCnt = 0;
        int attributeCnt = 0;
        int targetBonusesCnt = 0;
        Bonus targetBonus = null;

        double[] result = new double[Constants.BONUSES_VAL_COUNT];
        Map<Bonus, Integer> bonusMap = new HashMap<>();
        for (Attribute attribute : attributes) {
            if (!attribute.place.checkFraction) {
                attributeCnt++;
                if (attribute.bonus != null) {
                    if (attribute.bonus.values != null && attribute.bonus.values[Constants.Indexes.KRAZA] > 0) {
                        targetBonusesCnt++;
                        if (targetBonus == null){
                            targetBonus = attribute.bonus;
                        }
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

        int skips = (attributeCnt > ATTRIBUTE_CNT_MAX) ? 0 : ATTRIBUTE_CNT_MAX - attributeCnt;
        if (skips >= 1) {
            double[] values = possibleBonusesForSkips.get(0.5);
            int m = Math.min(skips, (int) (ATTRIBUTE_CNT_MAX - pairCnt * 0.5));
            if (m >= 1) {
                for (int i = 0; i < Constants.BONUSES_VAL_COUNT; i++) {
                    result[i] += m * values[i];
                }
            }

//            values = possibleBonusesForSkips.get(0.25);
//            m = Math.min(skips, (int) (ATTRIBUTE_CNT_MAX - pairCnt * 0.25));
//            if (m >= 1) {
//                for (int i = 0; i < Constants.BONUSES_VAL_COUNT; i++) {
//                    result[i] += m * values[i];
//                }
//            }
        }


//        if (skips + targetBonusesCnt >= 4) {
//            result[Constants.Indexes.KRAZA] = 1;
//        }
        if (targetBonus != null) {
            int cnt = (int) ((skips + targetBonusesCnt) * targetBonus.quantum);
            result[Constants.Indexes.KRAZA] = cnt;
        }
        return result;
    }

}
