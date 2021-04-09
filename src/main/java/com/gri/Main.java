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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final int calcAtrLimitCount = 0;
    private static final int calcDelta = 0;
    private static final double calcDeltaMultiplier = 1;

    private static final String defPath = "C:\\Users\\grse1118\\Desktop\\Raid200824";
    private static final String defType = ".xlsx";
    private static final Regime defRegime = Regime.FIND_MULTI_THREAD;
    private static final Double defAttributeFilterValue = null;
    private static final int defResultsLimitCnt = 2000;

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

        List<Attribute> allAttributes = getDataRepository.getAllAttributes(base, bonuses, places, glyphs, character);
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

        final long startTime = System.currentTimeMillis();
        switch (regime) {
            case FIND_MULTI_THREAD:{
                Attribute[][] attributes = filterService.convertListToArray(places, allAttributes, character);
                attributes = filterService.filterAttributesByDoublesAndMask(attributes, target);

                double[] tmpTargetDelta = Utils.getDelta(targetDelta, bonusService.getAttributeBonuses());
                final Attribute[][] attribFiltered = calculationService.filterAttributesRecursive(attributes, tmpTargetDelta);

//                final Attribute[][] attribFiltered = getFilterByValuesAndMask(attribFiltered0, target);

                final Attribute[][][] attribFiltered3 = new Attribute[attribFiltered[0].length][attribFiltered.length][];
                for (int i = 0; i < attribFiltered[0].length; i++) {
                    attribFiltered3[i][0] = new Attribute[]{attribFiltered[0][i]};
                    System.arraycopy(attribFiltered, 1, attribFiltered3[i], 1, attribFiltered.length - 1);
                }

                ExecutorService threadPool = Executors.newFixedThreadPool(8);

                int progressIndex = 1;
                int progressEnd =attribFiltered[0].length;

                List<Future<List<Result>>> futures = new ArrayList<>();
                for (int i = 0; i < attribFiltered3.length; i++) {
                    final int j = i;
                    futures.add(
                            CompletableFuture.supplyAsync(
                                    () -> {
                                        return calculationService.startCalculation(attribFiltered3[j], targetDelta);
                                    },
                                    threadPool
                            ));
                }

                List<Result> resultList = new ArrayList<>();
                for (Future<List<Result>> future : futures) {
                    try {
                        if (resultList.size() <= resultsLimitCnt){
                            List<Result> res = future.get();
                            resultList.addAll(res) ;

                            logger.info("progress = {}%; index = {}/{}; time = {}; goodCnt = {}"
                                    , 100 * progressIndex / progressEnd
                                    , progressIndex
                                    , progressEnd
                                    , getTime(System.currentTimeMillis() - startTime)
                                    , resultList.size());
                            progressIndex++;
                        }
                        else{
                            logger.info("Stop by results Limit Cnt = {}", resultsLimitCnt);
                        }

                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                logger.info("Executed time = {}", getTime(System.currentTimeMillis() - startTime));

                threadPool.shutdown();
                if (resultList.size() > 0) {
                    String outFileName = defPath + "_" + character.name + defType;
                    SaveDataRepository saveDataRepository = new SaveDataXssfRepositoryImpl(fileName, outFileName);
                    saveDataRepository.saveMainResults(resultList, character, baseAndLeagueAndZal);
                    saveDataRepository.close();
                }
                break;
            }
            case FIND_MAIN: {
                Attribute[][] attributes = filterService.convertListToArray(places, allAttributes, character);
                attributes = filterService.filterAttributesByDoubles(attributes);

                double[] tmpTargetDelta = Utils.getDelta(targetDelta, bonusService.getAttributeBonuses());
                attributes = calculationService.filterAttributesRecursive(attributes, tmpTargetDelta);

                List<Result> resultList = calculationService.startCalculation(attributes, targetDelta);
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

                List<Result> resultList = calculationService.startCalculation(attributes, targetDelta);
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
        System.exit(0);
    }

    public static String getTime(long time) {
        String result;
        if (time < 60 * 1_000) {
            double dd = time / 10.0;

            result = getStringFromTime(time / 1_000.0)+ "s";
        }
        else if (time < 3600 * 1_000) {
            result = getStringFromTime(time / 60.0 / 1_000) + "m";
        }
        else {
            result = getStringFromTime(time / 3600.0 / 1_000)+ "h";
        }
        return result;
    }

    public static String getStringFromTime(double time) {
        return String.format("%4.2f", time);
    }

}
