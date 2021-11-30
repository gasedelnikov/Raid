package com.gri;

import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.model.RankMask;
import com.gri.model.Regime;
import com.gri.model.Result;
import com.gri.repository.DataRepository;
import com.gri.repository.SaveDataRepository;
import com.gri.repository.impl.DataXssfRepositoryImpl;
import com.gri.repository.impl.SaveDataXssfRepositoryImpl;
import com.gri.service.BonusService;
import com.gri.service.impl.BonusServiceImpl;
import com.gri.service.impl.FilterTargetServiceImpl;
import com.gri.utils.Constants;
import com.gri.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final int calcAtrLimitCount = 0;
    private static final int saveFileIntrval = 15*60*1000;
    private static final int logIntrval = 30_000;
    private static final int calcDelta = 0;
    private static final double calcDeltaMultiplier = 1;
    private static final String EMPTY_CHARACTER_FIELDS_VALUE = "<<NONE>>";

    private static final String defPath = "C:\\Users\\grse1118\\Desktop\\Raid210907";
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
        DataRepository dataRepository = new DataXssfRepositoryImpl(fileName);

        final long startTime = System.currentTimeMillis();
        switch (regime) {
            case FIND_MULTI_THREAD:{
                Character character = dataRepository.getCharacter(defAttributeFilterValue);
                character = (character == null) ?
                        new Character(EMPTY_CHARACTER_FIELDS_VALUE,
                                EMPTY_CHARACTER_FIELDS_VALUE,
                                EMPTY_CHARACTER_FIELDS_VALUE,
                                0,
                                Double.MAX_VALUE,
                                Double.MAX_VALUE)
                        : character;

                Place[] places = dataRepository.getPlaces();
                double[] base = dataRepository.getBase();

                double[] leagueAndZal = dataRepository.getLeagueAndZal(base);
                double[] target = dataRepository.getTarget();
                double[] effectiveTarget = dataRepository.getEffectiveTarget();
                double[] glyphs = dataRepository.getGlyphs();
                Map<String, Bonus> bonuses = dataRepository.getBonuses(base);

                Map<Double, double[]> possibleBonusesForSkips = Utils.getPossibleBonusesForSkips(bonuses);

                List<Attribute> allAttributes = dataRepository.getAllAttributes(base, bonuses, places, glyphs, character);
                dataRepository.close();

                double[] baseAndLeagueAndZal = Utils.getSum(base, leagueAndZal);
                double[] targetDeltaReal = Utils.getDelta(target, baseAndLeagueAndZal);
                double[] targetDelta = new double[targetDeltaReal.length];

                for (int i = 0; i < Constants.VAL_COUNT; i++) {
                    targetDelta[i] = calcDeltaMultiplier * targetDeltaReal[i] - calcDelta;
                }

                int startSize = allAttributes.size();
                allAttributes = allAttributes
                        .stream()
                        .peek(attribute -> attribute.setTargetPriority(targetDelta))
                        .filter(attribute -> attribute.targetPriority > 0)
                        .collect(Collectors.toList());
                logger.info("filter by targetPriority = {};", startSize - allAttributes.size());

                BonusService bonusService = new BonusServiceImpl(possibleBonusesForSkips);

                long saveTime = System.currentTimeMillis();
                long logTime = System.currentTimeMillis();
                FilterTargetServiceImpl filterService = new FilterTargetServiceImpl(targetDelta,
                        allAttributes,
                        character,
                        bonusService,
                        baseAndLeagueAndZal,
                        effectiveTarget,
                        resultsLimitCnt,
                        places);

                Attribute[][] attribFiltered = filterService.getAttributes();
                ExecutorService threadPool = Executors.newFixedThreadPool(1);
                logger.info("SumTargetPriority = {}", filterService.getSumTargetPriority());
                boolean notStoped = true;
                int progressIndex = 1;
                int progressEnd = attribFiltered[0].length * attribFiltered[1].length;

                List<Future<List<Result>>> futures = new ArrayList<>();
                for (int i = 0; i < attribFiltered[0].length; i++) {
                    for (int j = 0; j < attribFiltered[1].length; j++) {
                        final int fi = i;
                        final int fj = j;
                        futures.add(
                                CompletableFuture.supplyAsync(() -> filterService.startCalculation(fi, fj), threadPool));
                    }
                }

                List<Result> resultList = new ArrayList<>();
                for (Future<List<Result>> future : futures) {
                    try {
                        if (resultList.size() <= resultsLimitCnt){
                            List<Result> res = future.get();
                            resultList.addAll(res) ;

                            if ((System.currentTimeMillis() - logTime) > logIntrval) {
                                logger.info("progress = {}%; index = {}/{}; time = {}; goodCnt = {}"
                                        , 100 * progressIndex / progressEnd
                                        , progressIndex
                                        , progressEnd
                                        , getTime(System.currentTimeMillis() - startTime)
                                        , resultList.size());
                                logTime = System.currentTimeMillis();
                            }

                            progressIndex++;
                        }
                        else{
                            if (notStoped) {
                                logger.info("Stop by results Limit Cnt = {}", resultsLimitCnt);
                                notStoped = false;
                            }
                        }

                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    if ((System.currentTimeMillis() - saveTime) > saveFileIntrval) {
                        saveData(resultList, character, fileName, baseAndLeagueAndZal);
                        saveTime = System.currentTimeMillis();
                    }
                }

                logger.info("Executed time = {}; goodCnt = {}", getTime(System.currentTimeMillis() - startTime), resultList.size());

                threadPool.shutdown();
                if (resultList.size() > 0) {
                    saveData(resultList, character, fileName, baseAndLeagueAndZal);
                }

                break;
            }
            case GET_RAITING:{
                Character character = new Character(EMPTY_CHARACTER_FIELDS_VALUE,
                                EMPTY_CHARACTER_FIELDS_VALUE,
                                EMPTY_CHARACTER_FIELDS_VALUE,
                                0,
                                Double.MAX_VALUE,
                                Double.MAX_VALUE);

                Place[] places = dataRepository.getPlaces(6, false); //false
//                double[] base = dataRepository.getBase();
                //                          К.Ш	К.УР ЗДР    АТК   ЗЩТ СОПР СКР МЕТК
                double[] base = new double[]{0, 0, 23000, 1500, 1500, 0, 100, 0};
                double[] glyphs = dataRepository.getGlyphs();
                Map<String, Bonus> bonuses = dataRepository.getBonuses(base);

//                Map<Double, double[]> possibleBonusesForSkips = Utils.getPossibleBonusesForSkips(bonuses);
//                BonusService bonusService = new BonusServiceImpl(possibleBonusesForSkips);

                List<Attribute> allAttributes = dataRepository.getAllAttributes(base, bonuses, places, glyphs, character);
                dataRepository.close();

                List<Attribute> allAttributesFilter = allAttributes.stream()
                        .filter(attribute -> "".equals(attribute.characterName))
                        .filter(attribute -> attribute.rank < 16)
                        .collect(Collectors.toList());

                Map<Double, List<Double>> mapRank = allAttributesFilter.stream()
                        .peek(attribute -> {
                            for (int i = 0; i < attribute.values.length ; i++) {
                                double bonusAdd = (attribute.bonus == null)
                                        ? 0
                                        : attribute.bonus.values[i] * attribute.bonus.quantum;
                                attribute.values[i] += bonusAdd;
                            }
                        })
                        .collect(Collectors.toMap(attribute -> attribute.id,
                                attribute -> new ArrayList<>()));

                List<List<Attribute>> listAttributes = Arrays.stream(places)
                        .map(place -> allAttributesFilter.stream()
                                .filter(attribute -> place.name.equals(attribute.placeName))
                                .collect(Collectors.toList()))
                        .collect(Collectors.toList());

                Attribute[][] attributes = listAttributes.stream()
                        .map(list -> list.toArray(new Attribute[0]))
                        .collect(Collectors.toList()).toArray(new Attribute[0][0]);

                double[][] maxValues = new double[places.length][Constants.VAL_COUNT];
                for (int i = 0; i < attributes.length; i++) {
                    for (int j = 0; j < attributes[i].length; j++) {
                        for (int k = 0; k < Constants.VAL_COUNT; k++) {
                                maxValues[i][k] = Math.max(maxValues[i][k], attributes[i][j].values[k]);
                        }
                    }
                }

                int rank1 = 50;
                int rank2 = 25;
                int rank3 = 10;

                List<RankMask> masks = new ArrayList<>();
//                               KRIT_S, KRIT_V, ZD, ATK, DEF, SOPR, SKOR, METK;
                masks.add(new RankMask(1,      1,  0,   1,   0,    0,    1,   0, rank1)); // ск атк
                masks.add(new RankMask(1,      1,  0,   1,   0,    0,    1,   1, rank1)); // ск атк мет

                masks.add(new RankMask(1,      1,  0,   0,   1,    0,    1,   0, rank1)); // ск def
                masks.add(new RankMask(1,      1,  0,   0,   1,    0,    1,   1, rank1)); // ск def метк
                masks.add(new RankMask(1,      1,  0,   0,   1,    1,    1,   0, rank1)); // ск def сопр
                masks.add(new RankMask(1,      1,  0,   0,   1,    1,    1,   1, rank2)); // ск def метк сопр

                masks.add(new RankMask(0,      0,  1,   0,   0,    0,    1,   0, rank1)); // ск здр
                masks.add(new RankMask(1,      1,  1,   0,   0,    0,    1,   0, rank2)); // ск здр ку
                masks.add(new RankMask(1,      1,  1,   0,   0,    0,    1,   1, rank2)); // ск здр ку метк
                masks.add(new RankMask(0,      0,  1,   0,   0,    0,    1,   1, rank1)); // ск здр метк
                masks.add(new RankMask(0,      0,  1,   0,   0,    1,    1,   0, rank1)); // ск здр сопр

                masks.add(new RankMask(0,      0,  1,   0,   1,    1,    0,   0, rank2)); // здр def сопр

                masks.add(new RankMask(0,      0,  1,   0,   1,    0,    1,   0, rank1)); // ск def здр
                masks.add(new RankMask(0,      0,  1,   0,   1,    0,    1,   1, rank1)); // ск def здр метк
                masks.add(new RankMask(0,      0,  1,   0,   1,    1,    1,   0, rank1)); // ск def здр сопр
                masks.add(new RankMask(0,      0,  1,   0,   1,    1,    1,   1, rank1)); // ск def здр метк сопр

                masks.add(new RankMask(0,      0,  0,   0,   0,    0,    1,   1, rank1)); // ск метк
                masks.add(new RankMask(0,      0,  0,   0,   0,    1,    1,   0, rank1)); // ск сопр
                masks.add(new RankMask(0,      0,  0,   0,   0,    1,    1,   1, rank1)); // ск метк сопр

                masks.add(new RankMask(1,      1,  0,   0,   0,    0,    0,   0, rank3)); // KRIT_S KRIT_V
                masks.add(new RankMask(1,      1,  0,   0,   0,    0,    1,   0, rank3)); // KRIT_S KRIT_V скорость
//                masks.add(new RankMask(0,      1,  0,   0,   0,    0,    0,   0, rank3)); // KRIT_V
//                masks.add(new RankMask(0,      0,  1,   0,   0,    0,    0,   0, rank3)); // здр
//                masks.add(new RankMask(0,      0,  0,   1,   0,    0,    0,   0, rank3)); // атк
//                masks.add(new RankMask(0,      0,  0,   0,   1,    0,    0,   0, rank3)); // def
//                masks.add(new RankMask(0,      0,  0,   0,   0,    1,    0,   0, rank3)); // сопр
                masks.add(new RankMask(0,      0,  0,   0,   0,    0,    1,   0, rank3)); // скорость
//                masks.add(new RankMask(0,      0,  0,   0,   0,    0,    0,   1, rank3)); // метк

                for (int j = 0; j < masks.size(); j++) {
                    getRank(mapRank, listAttributes, attributes, masks.get(j).getMask(), maxValues);
                }

                String outFileName = defPath + defType;
                SaveDataRepository saveDataRepository = new SaveDataXssfRepositoryImpl(fileName, outFileName);
                saveDataRepository.saveAttributeRang(mapRank, masks);
                saveDataRepository.close();
            }
            case FIND_MAIN: {
//                Attribute[][] attributes = filterService.convertListToArray(places, allAttributes, character);
//                attributes = filterService.filterAttributesByDoubles(attributes);
//
//                double[] tmpTargetDelta = Utils.getDelta(targetDelta, bonusService.getAttributeBonuses());
//                attributes = calculationService.filterAttributesRecursive(attributes, tmpTargetDelta);
//
//                List<Result> resultList = calculationService.startCalculation(attributes, targetDelta);
//                if (resultList.size() > 0) {
//                    String outFileName = defPath + "_" + character.name + defType;
//                    SaveDataRepository saveDataRepository = new SaveDataXssfRepositoryImpl(fileName, outFileName);
//                    saveDataRepository.saveMainResults(resultList, character, baseAndLeagueAndZal);
//                    saveDataRepository.close();
//                }
                break;
            }
            case CHECK_CHARACTER: {
//                Attribute[][] attributes = filterService.getCharacterAttributes(targetDelta, allAttributes, places, character);
//                double[] tmpTargetDelta = Utils.getDelta(targetDelta, bonusService.getAttributeBonuses());
//
//                attributes = calculationService.filterAttributesRecursive(attributes, tmpTargetDelta);
//                boolean check1 = true;
//                for (Attribute[] attribute : attributes) {
//                    check1 = check1 && attribute.length > 0;
//                }
//                logger.info("test character; filterAttributesRecursive: {} ", check1);
//
//                List<Result> resultList = calculationService.startCalculation(attributes, targetDelta);
//                logger.info("test character; final : {} ", resultList.size() > 0);
                break;
            }
            case FIND_DOUBLES: {
//                base[Constants.Indexes.KRIT_S] = 10;
//                base[Constants.Indexes.KRIT_V] = 50;
//                base[Constants.Indexes.ZD] = 12000;
//                base[Constants.Indexes.ATK] = 630;
//                base[Constants.Indexes.DEF] = 730;
//                base[Constants.Indexes.SKOR] = 85;
//                base[Constants.Indexes.METK] = 0;
//                base[Constants.Indexes.SOP] = 30;

//                logger.info("start find doubles");
//                Attribute[][] attributes = filterService.convertListToArray(places, allAttributes, null);
//
//                filterService.setAttributeParentId(attributes);
////                String outFileName = defPath + "_" + character.name + defType;
//                SaveDataRepository saveDataRepository = new SaveDataXssfRepositoryImpl(fileName, fileName);
//                saveDataRepository.saveAttributeParentId(allAttributes);
//                saveDataRepository.close();

                break;
            }
        }
        System.exit(0);
    }

    public static void getRank(Map<Double, List<Double>> mapRank, List<List<Attribute>> listAttributes, Attribute[][] attributes, double[] mask, double[][] maxValues){
        for (int i = 0; i < attributes.length; i++) {
            for (int j = 0; j < attributes[i].length; j++) {
                double r = 0;
                for (int k = 0; k < mask.length; k++) {
                    if (mask[k] > 0 && maxValues[i][k] > 0) {
                        r +=  attributes[i][j].values[k] / maxValues[i][k] ;
                    }
                }
                attributes[i][j].targetPriority = r;
            }
        }

        List<List<Attribute>> sortedListAttributes = listAttributes.stream()
                .map(list -> list.stream()
                        .sorted((p1,p2) -> Double.compare(p2.targetPriority, p1.targetPriority))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        sortedListAttributes = sortedListAttributes.stream()
                .peek(list -> {
                    double targetPriority = 0;
                    double rank = 0;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).targetPriority == targetPriority){
                            list.get(i).rank = rank;
                        }
                        else {
                            list.get(i).rank = i + 1;
                            targetPriority = list.get(i).targetPriority;
                            rank = list.get(i).rank;
                        }
                    }
                })
               .collect(Collectors.toList());

        sortedListAttributes = sortedListAttributes.stream()
                .map(list -> list.stream()
                        .peek(a -> mapRank.get(a.id).add(a.rank))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        int x = 1;
    }

    public static void saveData(List<Result> resultList, Character character, String fileName, double[] baseAndLeagueAndZal) throws IOException {
        String outFileName = defPath + "_" + character.name + defType;
        SaveDataRepository saveDataRepository = new SaveDataXssfRepositoryImpl(fileName, outFileName);
        saveDataRepository.saveMainResults(resultList, character, baseAndLeagueAndZal);
        saveDataRepository.close();
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
