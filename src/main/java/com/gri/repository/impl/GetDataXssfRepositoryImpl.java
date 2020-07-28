package com.gri.repository.impl;

import com.gri.Main;
import com.gri.repository.GetDataRepository;
import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.utils.Constants;
import com.gri.utils.XssfUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GetDataXssfRepositoryImpl implements GetDataRepository {
    private Logger logger = LoggerFactory.getLogger(GetDataXssfRepositoryImpl.class);

    private XSSFWorkbook workbook;

    public GetDataXssfRepositoryImpl(String fileName) throws IOException {
        workbook = XssfUtils.getWorkbook(fileName);
    }

    @Override
    public void close() throws IOException{
        workbook.close();
    }

    @Override
    public Character getCharacter() {
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.FIND);
        XSSFRow xx = sheet.getRow(Constants.Character.ROW_INDEX_NAME);
        String name = XssfUtils.getStringValueSafe(sheet.getRow(Constants.Character.ROW_INDEX_NAME).getCell(Constants.Character.COLL_INDEX_NAME));
        String element = XssfUtils.getStringValueSafe(sheet.getRow(Constants.Character.ROW_INDEX_ELEMENT).getCell(Constants.Character.COLL_INDEX_ELEMENT));
        String fraction = XssfUtils.getStringValueSafe(sheet.getRow(Constants.Character.ROW_INDEX_FRACTION).getCell(Constants.Character.COLL_INDEX_FRACTION));
        Main.ATTRIBUTE_FILTER_VALUE = XssfUtils.getDoubleValueSafe(sheet.getRow(Constants.Character.ROW_INDEX_FILTER).getCell(Constants.Character.COLL_INDEX_FILTER));

        logger.info("loaded Character; name = {}, fraction = {}, element = {}, filter value = {}", name, fraction, element, Main.ATTRIBUTE_FILTER_VALUE);
        return (new Character(name, fraction, element));
    }

    @Override
    public Place[] getPlaces() {
        logger.info("get Places");
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.FIND);
        String[] names = XssfUtils.getStringArray(sheet, Constants.PLACES_ROW, Constants.PLACES_COLL_START, Constants.PLACES_COUNT);
        double[] orderByArray = XssfUtils.getDoubleArray(sheet, Constants.PLACES_ROW + 1, Constants.PLACES_COLL_START, Constants.PLACES_COUNT);

        List<Place> placeList = new ArrayList<>();
        for (int i = 0; i < Constants.PLACES_COUNT; i++) {
            double[] t1 = getAttributeColorFilter(sheet, Constants.PLACES_ROW + 2, Constants.PLACES_COLL_START + i, 3, 3, 0);
            double[] t2 = getAttributeColorFilter(sheet, Constants.PLACES_ROW + 5, Constants.PLACES_COLL_START + i, 3, 8, 1);

            placeList.add(new Place(names[i], i > 5, (int) orderByArray[i], t1, t2));
        }
        return placeList.stream().sorted(Comparator.comparingInt(i -> i.orderBy)).toArray(Place[]::new);
    }

    @Override
    public double[] getBase() {
        logger.info("get Base");
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.FIND);
        return XssfUtils.getDoubleArray(sheet, Constants.VAL_BASE_ROW, Constants.VAL_COLL_START, Constants.VAL_COUNT);
    }

    @Override
    public double[] getLeagueAndZal(double[] base) {
        logger.info("get League And Zal");
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.FIND);
        double[] valuesPr = XssfUtils.getDoubleArray(sheet, Constants.VAL_LEAGUE_ROW, Constants.VAL_COLL_START - 3, 3);
        double[] values = XssfUtils.getDoubleArray(sheet, Constants.VAL_LEAGUE_ROW, Constants.VAL_COLL_START, Constants.VAL_COUNT);

        values[Constants.Indexes.ZD] = base[Constants.Indexes.ZD] * valuesPr[0] / 100;
        values[Constants.Indexes.ATK] = base[Constants.Indexes.ATK] * valuesPr[1] / 100;
        values[Constants.Indexes.DEF] = base[Constants.Indexes.DEF] * valuesPr[2] / 100;

        return values;
    }

    @Override
    public double[] getGlyphs() {
        logger.info("get Glyphs");
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.FIND);
        return XssfUtils.getDoubleArray(sheet, Constants.VAL_GLYPHS_ROW, Constants.VAL_COLL_START - 4, Constants.VAL_COUNT + 4);
    }

    @Override
    public double[] getTarget() {
        logger.info("get Target");
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.FIND);

        double[] result = XssfUtils.getDoubleArray(sheet, Constants.VAL_TARGET_ROW, Constants.VAL_COLL_START, Constants.VAL_COUNT);
        for (int i = 0; i < result.length; i++) {
            result[i] = (int) result[i];
        }

        return result;
    }

    @Override
    public double[] getEffectiveTarget() {
        logger.info("get Effective Target");
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.FIND);
        return XssfUtils.getDoubleArray(sheet, Constants.VAL_TARGET_ROW, Constants.VAL_EFFECTIVE_COLL_START, Constants.VAL_EFFECTIVE_COUNT);
    }

    @Override
    public Map<String, Bonus> getBonuses(double[] base) {
        logger.info("get Bonuses");
        Map<String, Bonus> result = new TreeMap<>();
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.SETS);
        for (int i = Constants.BONUSES_ROW_START; i < Constants.BONUSES_ROW_END; i++) {
            XSSFRow row = sheet.getRow(i);
            String name = XssfUtils.getStringValueSafe(row.getCell(Constants.BONUSES_COLL_START));
            double quantum = 1.0 / XssfUtils.getDoubleValueSafe(row.getCell(Constants.BONUSES_COLL_START + 1));
            double type = XssfUtils.getDoubleValueSafe(row.getCell(Constants.BONUSES_COLL_TYPE));

            double[] valuesPr = XssfUtils.getDoubleArray(sheet, i, Constants.BONUSES_COLL_START + 2, 3);
            double[] values = XssfUtils.getDoubleArray(sheet, i, Constants.BONUSES_COLL_START + 5, Constants.BONUSES_COLL_COUNT);

            values[Constants.Indexes.ZD] = base[Constants.Indexes.ZD] * valuesPr[0];
            values[Constants.Indexes.ATK] = base[Constants.Indexes.ATK] * valuesPr[1];
            values[Constants.Indexes.DEF] = base[Constants.Indexes.DEF] * valuesPr[2];
            values[Constants.Indexes.SKOR] = base[Constants.Indexes.SKOR] * values[Constants.Indexes.SKOR];

            Bonus bonus = new Bonus(name, quantum, values, type);
            result.put(name, bonus);
        }

        return result;
    }

    @Override
    public List<Attribute> getAllAttributes(double[] base, Map<String, Bonus> bonuses, Place[] places, double[] glyphs) {
        List<Attribute> result = new ArrayList<>();
        logger.info("get All Attributes");
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.ART);
        int cnt = sheet.getLastRowNum();

        Map<String, Place> placeMap = Arrays.stream(places).collect(Collectors.toMap(place -> place.name, Function.identity()));
//        List<String> placesNameList = Arrays.asList(places).stream().map(place -> place.name).limit(6).collect(Collectors.toList());

        for (int i = Constants.ATR_START_ROW; i <= cnt; i++) {
            XSSFRow row = sheet.getRow(i);
            String character = XssfUtils.getStringValueSafe(row.getCell(Constants.Columns.CHARACTER_NAME));
            String tmpCharacter = XssfUtils.getStringValueSafe(row.getCell(Constants.Columns.CHARACTER_NAME - 1));
            String place = XssfUtils.getStringValueSafe(row.getCell(Constants.Columns.PLACE));
            String type = XssfUtils.getStringValueSafe(row.getCell(Constants.Columns.TYPE));
            String rarity = XssfUtils.getStringValueSafe(row.getCell(Constants.Columns.RARITY));

            double rank = XssfUtils.getDoubleValueSafe(row.getCell(Constants.Columns.RANK));

            double id = XssfUtils.getDoubleValueSafe(row.getCell(Constants.Columns.ID));
            double filterFlag = XssfUtils.getDoubleValueSafe(row.getCell(Constants.Columns.FILTER_FLAG));

            double[] valuesPr = XssfUtils.getDoubleArray(sheet, i, Constants.Columns.ZD_PR, 3);
            double[] values = XssfUtils.getDoubleArray(sheet, i, Constants.Columns.KRIT_S, Constants.ATR_VALUES_COUNT);
            values[Constants.Indexes.KRAZA] = 0;

            boolean filter = false;
            Place pl = placeMap.get(place);

            if (pl != null) {
                for (int j = 0; j < pl.filterPr.length; j++) {
                    if (valuesPr[j] >= pl.filterPr[j]) {
                        filter = true;
                        break;
                    }
                }
                for (int j = 0; j < pl.filterMain.length; j++) {
                    if (values[j] >= pl.filterMain[j]) {
                        filter = true;
                        break;
                    }
                }
            }
            filter = filter && !rarity.equals("Нет");
            if (filter) {
                // получение основных х-к (заливка - желтый цвет)
                int mainPrIndex = XssfUtils.getColorIndex(sheet, i, Constants.Columns.ZD_PR, 3, "FFFFFF00");
                int mainIndex = XssfUtils.getColorIndex(sheet, i, Constants.Columns.KRIT_S, Constants.ATR_VALUES_MAIN_COUNT, "FFFFFF00");

                double glyphsIndex = 0;
                try {
                    glyphsIndex = XssfUtils.getDoubleValueSafe(row.getCell(Constants.Columns.GLYPHS));
                    addGlyphs(glyphsIndex, mainPrIndex, valuesPr, mainIndex, values, glyphs);
                } catch (IllegalStateException ex) {
                    logger.warn("IllegalStateException: getAllAttributes");
                }

                values[Constants.Indexes.ZD] = values[Constants.Indexes.ZD] + base[Constants.Indexes.ZD] * valuesPr[0] / 100;
                values[Constants.Indexes.ATK] = values[Constants.Indexes.ATK] + base[Constants.Indexes.ATK] * valuesPr[1] / 100;
                values[Constants.Indexes.DEF] = values[Constants.Indexes.DEF] + base[Constants.Indexes.DEF] * valuesPr[2] / 100;

                Bonus bonus = bonuses.get(type);
                if (bonus == null && !pl.checkFraction) {
                    logger.info("Empty Bonus = {}", type);
                }
                Attribute attr = new Attribute(id, character, place, rarity, glyphsIndex, rank, type, bonus, filterFlag, values);
                attr.tmpCharacterName = tmpCharacter;
                attr.place = pl;
                result.add(attr);
            }
        }

        return result;
    }

    private void addGlyphs(double glyphsIndex, int mainPrIndex, double[] valuesPr, int mainIndex,
                           double[] values, double[] glyphs) {
        for (int i = 0; i < valuesPr.length; i++) {
            if (i != mainPrIndex && valuesPr[i] > 0) {
                double adds = addGlyph(glyphsIndex, glyphs[0], glyphs[i + 1]);
                valuesPr[i] += adds;
            }
        }
        for (int i = 0; i < values.length; i++) {
            if (i != mainIndex && values[i] > 0) {
                double adds = addGlyph(glyphsIndex, glyphs[0], glyphs[i + 4]);
                values[i] += adds;
            }
        }
    }

    private double addGlyph(double glyphsIndex, double glyphsMax, double glyph) {
        double result = 0;
        if (glyphsIndex < glyphsMax) {
            result = glyph * (glyphsMax - glyphsIndex) / glyphsMax;
        }
        return result;
    }

    private double[] getAttributeColorFilter(XSSFSheet sheet, int rowStart, int colNum, int cnt, int arraySize, int key) {
        double[] result = new double[arraySize];

        XSSFRow row0 = sheet.getRow(Constants.PLACES_ROW + 1);
        XSSFCell cell0 = row0.getCell(colNum);
        boolean getAll = false;
        if (cell0 != null && cell0.getCellStyle() != null && cell0.getCellStyle().getFillForegroundColorColor() != null) {
            String color = cell0.getCellStyle().getFillForegroundColorColor().getARGBHex();
            getAll = Constants.Filter.COLOR_FILTER_COLOR.equals(color);
        }
        if (!getAll) {
            for (int i = 0; i < arraySize; i++) {
                result[i] = Double.MAX_VALUE;
            }

            for (int i = 0; i < cnt; i++) {
                XSSFRow row = sheet.getRow(rowStart + i);
                if (key == XssfUtils.getDoubleValueSafe(row.getCell(Constants.Filter.COLOR_FILTER_KEY))) {
                    int index = (int) XssfUtils.getDoubleValueSafe(row.getCell(Constants.Filter.COLOR_FILTER_INDEX));

                    XSSFCell cell = row.getCell(colNum);
                    if (cell != null && cell.getCellStyle() != null && cell.getCellStyle().getFillForegroundColorColor() != null) {
                        String color = cell.getCellStyle().getFillForegroundColorColor().getARGBHex();
                        if (Constants.Filter.COLOR_FILTER_COLOR.equals(color)) {
                            result[index] = XssfUtils.getDoubleValueSafe(cell);
                        }
                    }
                }
            }
        }
        return result;
    }

}
