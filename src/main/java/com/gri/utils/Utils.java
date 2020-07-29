package com.gri.utils;

import com.gri.model.Bonus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class Utils {
    private static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static double[] getMax(double[] m1, double[] m2) {
        if (m1 == null) {
            return m2;
        } else {
            double[] result = new double[m1.length];
            for (int i = 0; i < m1.length; i++) {
                result[i] = Math.max(m1[i], m2[i]);
            }
            return result;
        }
    }

    public static double[] getSum(double[] m1, double[] m2) {
        if (m2 == null) {
            return m1;
        } else {
            double[] result = new double[m1.length];
            //           logger.info("m1" + m1 + ", m2 =" + m2);
            for (int i = 0; i < m1.length; i++) {
                result[i] = m1[i] + m2[i];
            }
            return result;
        }
    }

    public static double[] getDelta(double[] m1, double[] m2) {
        double[] result = new double[m1.length];
        for (int i = 0; i < m1.length; i++) {
            result[i] = m1[i] - m2[i];
        }
        return result;
    }

    public static double[] getMultiple(double m, double[] m1) {
        double[] result = new double[m1.length];
        m = Math.floor(m);
        if (m >= 1) {
            for (int i = 0; i < m1.length; i++) {
                result[i] = m1[i] * m;
            }
        }
        return result;
    }

    public static Map<Double, double[]> getPossibleBonusesForSkips(Map<String, Bonus> bonuses) {
        return bonuses.values().stream()
                .collect(Collectors.groupingBy(Bonus::getQuantum))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey
                        , entry -> entry.getValue().stream()
                                .map(b -> b.values)
                                .reduce(Utils::getMax)
                                .orElse(new double[Constants.BONUSES_VAL_COUNT])
                        )
                );
    }

    public static double getCriticalEffectiveValue(double value, double criticalValue) {
        return value * (1 + criticalValue / 100);
    }

    public static double getEffectiveZdValue(double zd, double def) {
        return zd * (1 + def / 600) ;
    }

}
