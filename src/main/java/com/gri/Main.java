package com.gri;

import com.gri.data.repository.GetDataRepository;
import com.gri.data.repository.SaveDataRepository;
import com.gri.data.repository.impl.GetDataXssfRepositoryImpl;
import com.gri.data.repository.impl.SaveDataXssfRepositoryImpl;
import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.model.Regime;
import com.gri.model.Result;
import com.gri.service.CalculationService;
import com.gri.service.StartFilterService;
import com.gri.utils.Constants;
import com.gri.utils.Tests;
import com.gri.utils.Utils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static Map<Double, double[]> possibleBonusesForSkips = null;
    public static XSSFWorkbook workbook = null;
    public static double[] baseAndLeagueAndZal = null;
    public static double[] targetDeltaReal;
    public static double[] effectiveTarget;
    public static double[] leagueAndZal;
    public static boolean checkEffectiveTarget = false;

    public static int CALC_ATR_COUNT = 0;
    public static double ATTRIBUTE_FILTER_VALUE = 0;

    public static int CALC_DELTA = 0;
    public static double CALC_DELTA_MULTIPLIER = 1;

    private static String defPath = "C:\\Users\\grse1118\\Desktop\\Raid200712";
    private static String defType = ".xlsx";
    private static Regime defRegime = Regime.FIND_MAIN;

    public static int goodIndex = 0;
    public static int goodMax = 500;

    public static  List<Result> resultList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        String fileName ;
        Regime regime;

        if (args == null || args.length == 0) {
            fileName = Main.defPath + Main.defType;
            regime = defRegime;
        }
        else{
            fileName = args[0];
            regime = Regime.valueOf(args[1]);
        }
        GetDataRepository getDataRepository = new GetDataXssfRepositoryImpl(fileName);

        Character character = getDataRepository.getCharacter();
        Place[] places = getDataRepository.getPlaces();
        double[] base = getDataRepository.getBase();
        Main.leagueAndZal = getDataRepository.getLeagueAndZal(base);
        double[] target = getDataRepository.getTarget();
        Main.effectiveTarget = getDataRepository.getEffectiveTarget();
        double[] glyphs = getDataRepository.getGlyphs();
        Map<String, Bonus> bonuses = getDataRepository.getBonuses( base);

        Main.possibleBonusesForSkips = Utils.getPossibleBonusesForSkips(bonuses);

        for (int i=0; i < Main.effectiveTarget.length; i++){
            Main.checkEffectiveTarget = Main.checkEffectiveTarget || Main.effectiveTarget[i] > 0;
        }

        List<Attribute> allAttributes = getDataRepository.getAllAttributes(base, bonuses, places, glyphs);
        getDataRepository.close();

        Main.baseAndLeagueAndZal = Utils.getSum(base, leagueAndZal);
        targetDeltaReal = Utils.getDelta(target, baseAndLeagueAndZal);
        double[] targetDelta = new double[targetDeltaReal.length];

        for (int i = 0; i < Constants.VAL_COUNT; i++) {  // по местам
            targetDelta[i] = Main.CALC_DELTA_MULTIPLIER * targetDeltaReal[i] - Main.CALC_DELTA;
        }

        switch (regime){
            case FIND_MAIN:{
                Attribute[][] attributes = StartFilterService.startFilter(places, allAttributes, character, targetDelta);

                CalculationService.startCalculation(0, attributes, targetDelta, new Attribute[0]);
                if (goodIndex > 0) {
                    String outFileName  = defPath + "_" + character.name + defType;
                    SaveDataRepository saveDataRepository = new SaveDataXssfRepositoryImpl(fileName, outFileName);
                    saveDataRepository.saveMainResults(resultList);
                    saveDataRepository.close();
                }
                break;
            }
            case TEST_CHARACTER:{
                Tests.test_character(targetDelta, allAttributes, places, character);
                break;
            }
            case FIND_DOUBLES:{
                StartFilterService.setParentId(allAttributes);

//                XssfDataServiceImpl.setParentId(workbook, allAttributes);
//                XssfDataServiceImpl.saveFile(workbook, defPath + defType);
                break;
            }
        }

    }

}
