package com.gri;

import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.model.Regime;
import com.gri.model.Result;
import com.gri.repository.GetDataRepository;
import com.gri.repository.SaveDataRepository;
import com.gri.repository.impl.GetDataXssfRepositoryImpl;
import com.gri.repository.impl.SaveDataXssfRepositoryImpl;
import com.gri.service.BonusService;
import com.gri.service.CalculationService;
import com.gri.service.FilterService;
import com.gri.service.impl.BonusServiceImpl;
import com.gri.service.impl.CalculationServiceImpl;
import com.gri.service.impl.FilterServiceImpl;
import com.gri.utils.Constants;
import com.gri.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final int calcAtrLimitCount = 0;
    private static final int calcDelta = 0;
    private static final double calcDeltaMultiplier = 1;

    private static final String defPath = "C:\\Users\\grse1118\\Desktop\\Raid200729";
    private static final String defType = ".xlsx";
    private static final Regime defRegime = Regime.FIND_DOUBLES;
    private static final Double defAttributeFilterValue = null;
    private static final int defResultsLimitCnt = 500;

    public static void main(String[] args) throws IOException {
        String fileName;
        Regime regime;
        int resultsLimitCnt;

        if (args == null || args.length == 0) {
            fileName = Main.defPath + Main.defType;
            regime = defRegime;
            resultsLimitCnt = defResultsLimitCnt;
        } else {
            fileName = args[0];
            regime = Regime.valueOf(args[1]);
            resultsLimitCnt = Integer.parseInt(args[2]);
        }
        GetDataRepository getDataRepository = new GetDataXssfRepositoryImpl(fileName);

        Character character = getDataRepository.getCharacter(defAttributeFilterValue);
        Place[] places = getDataRepository.getPlaces();
        double[] base = new double[Constants.VAL_COUNT];
        if (regime == Regime.FIND_DOUBLES) {
            base[Constants.Indexes.KRIT_S] = 10;
            base[Constants.Indexes.KRIT_V] = 50;
            base[Constants.Indexes.ZD] = 12000;
            base[Constants.Indexes.ATK] = 630;
            base[Constants.Indexes.DEF] = 730;
            base[Constants.Indexes.SKOR] = 85;
            base[Constants.Indexes.METK] = 0;
            base[Constants.Indexes.SOP] = 30;
        } else {
            base = getDataRepository.getBase();
        }

        double[] leagueAndZal = getDataRepository.getLeagueAndZal(base);
        double[] target = getDataRepository.getTarget();
        double[] effectiveTarget = getDataRepository.getEffectiveTarget();
        double[] glyphs = getDataRepository.getGlyphs();
        Map<String, Bonus> bonuses = getDataRepository.getBonuses(base);

        Map<Double, double[]> possibleBonusesForSkips = Utils.getPossibleBonusesForSkips(bonuses);

        List<Attribute> allAttributes = getDataRepository.getAllAttributes(base, bonuses, places, glyphs);
        getDataRepository.close();

        double[] baseAndLeagueAndZal = Utils.getSum(base, leagueAndZal);
        double[] targetDeltaReal = Utils.getDelta(target, baseAndLeagueAndZal);
        double[] targetDelta = new double[targetDeltaReal.length];

        for (int i = 0; i < Constants.VAL_COUNT; i++) {
            targetDelta[i] = calcDeltaMultiplier * targetDeltaReal[i] - calcDelta;
        }

        BonusService bonusService = new BonusServiceImpl(possibleBonusesForSkips);
        CalculationService calculationService = new CalculationServiceImpl(bonusService, baseAndLeagueAndZal, effectiveTarget, resultsLimitCnt);
        FilterService filterService = new FilterServiceImpl(calcAtrLimitCount);

        switch (regime) {
            case FIND_MAIN: {
                Attribute[][] attributes = filterService.convertListToArray(places, allAttributes, character);
                attributes = filterService.filterAttributesByValues(attributes);

                double[] tmpTargetDelta = Utils.getDelta(targetDelta, bonusService.getAttributeBonuses());
                attributes = calculationService.filterAttributesRecursive(attributes, tmpTargetDelta);

                List<Result> resultList = calculationService.startCalculation(0, attributes, targetDelta, new Attribute[0]);
                if (resultList.size() > 0) {
                    String outFileName = defPath + "_" + character.name + defType;
                    SaveDataRepository saveDataRepository = new SaveDataXssfRepositoryImpl(fileName, outFileName);
                    saveDataRepository.saveMainResults(resultList, character, baseAndLeagueAndZal);
                    saveDataRepository.close();
                }
                break;
            }
            case CHECK_CHARACTER: {
                Attribute[][] attributes = filterService.getCharacterAttributes(targetDelta, allAttributes, places, character);
                double[] tmpTargetDelta = Utils.getDelta(targetDelta, bonusService.getAttributeBonuses());

                attributes = calculationService.filterAttributesRecursive(attributes, tmpTargetDelta);
                boolean check1 = true;
                for (Attribute[] attribute : attributes) {
                    check1 = check1 && attribute.length > 0;
                }
                logger.info("test character; filterAttributesRecursive: {} ", check1);

                List<Result> resultList = calculationService.startCalculation(0, attributes, targetDelta, new Attribute[0]);
                logger.info("test character; final : {} ", resultList.size() > 0);
                break;
            }
            case FIND_DOUBLES: {
                logger.info("start find doubles");
                Attribute[][] attributes = filterService.convertListToArray(places, allAttributes, null);

                filterService.setAttributeParentId(attributes);
//                String outFileName = defPath + "_" + character.name + defType;
                SaveDataRepository saveDataRepository = new SaveDataXssfRepositoryImpl(fileName, fileName);
                saveDataRepository.saveAttributeParentId(allAttributes);
                saveDataRepository.close();

                break;
            }
        }

    }

}
