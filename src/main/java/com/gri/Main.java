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

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static int CALC_ATR_COUNT = 40;
    public static double ATTRIBUTE_FILTER_VALUE = 0;

    public static int CALC_DELTA = 0;
    public static double CALC_DELTA_MULTIPLIER = 1;

    private static String defPath = "C:\\Users\\grse1118\\Desktop\\Raid200712";
    private static String defType = ".xlsx";
    private static Regime defRegime = Regime.FIND_MAIN;

    public static int RESULTS_LIMIT_CNT = 500;

    public static void main(String[] args) throws IOException {
        String fileName;
        Regime regime;

        if (args == null || args.length == 0) {
            fileName = Main.defPath + Main.defType;
            regime = defRegime;
        } else {
            fileName = args[0];
            regime = Regime.valueOf(args[1]);
        }
        GetDataRepository getDataRepository = new GetDataXssfRepositoryImpl(fileName);

        Character character = getDataRepository.getCharacter();
        Place[] places = getDataRepository.getPlaces();
        double[] base = getDataRepository.getBase();
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
            targetDelta[i] = Main.CALC_DELTA_MULTIPLIER * targetDeltaReal[i] - Main.CALC_DELTA;
        }

        BonusService bonusService = new BonusServiceImpl(possibleBonusesForSkips);
        CalculationService calculationService = new CalculationServiceImpl(bonusService, baseAndLeagueAndZal, effectiveTarget);
        FilterService filterService = new FilterServiceImpl();

        switch (regime) {
            case FIND_MAIN: {
                Attribute[][] attributes = filterService.startFilter(places, allAttributes, character, targetDelta);
                double[] tmpTargetDelta = Utils.getDelta(targetDelta, bonusService.getAttributeBonuses());
                attributes = calculationService.filterAttributesRecursive(attributes, tmpTargetDelta);

                List<Result> resultList = calculationService.startCalculation(0, attributes, targetDelta, new Attribute[0]);
                if (resultList.size() > 0) {
                    String outFileName = defPath + "_" + character.name + defType;
                    SaveDataRepository saveDataRepository = new SaveDataXssfRepositoryImpl(fileName, outFileName);
                    saveDataRepository.saveMainResults(resultList);
                    saveDataRepository.close();
                }
                break;
            }
            case TEST_CHARACTER: {
                Attribute[][] attributes = filterService.getCharacterAttributes(targetDelta, allAttributes, places, character);
                double[] tmpTargetDelta = Utils.getDelta(targetDelta, bonusService.getAttributeBonuses());

                attributes = calculationService.filterAttributesRecursive(attributes, tmpTargetDelta);
                boolean check1 = true;
                for (int i=0; i < attributes.length; i++){
                    check1 = check1 && attributes[i].length > 0;
                }
                logger.info("test character; filterAttributesRecursive: {} ", check1);

                List<Result> resultList = calculationService.startCalculation(0, attributes, targetDelta, new Attribute[0]);
                logger.info("test character; final : {} ", resultList.size() > 0);
                break;
            }
            case FIND_DOUBLES: {
//                FilterService.setParentId(allAttributes);

//                XssfDataServiceImpl.setParentId(workbook, allAttributes);
//                XssfDataServiceImpl.saveFile(workbook, defPath + defType);
                break;
            }
        }

    }

}
