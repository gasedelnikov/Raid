package com.gri.data;

import com.gri.Main;
import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.utils.Constants;
import com.gri.utils.XssfUtils;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataServiceImpl {
    private static Logger logger = LoggerFactory.getLogger(DataServiceImpl.class);
    private static int resultIndex = 0;

    public static void saveFile(XSSFWorkbook workbook, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    public static void putResult(XSSFWorkbook wb, Attribute[] attributes, double[] bonuses) {
        int sRow = Constants.Result.START_ROW + Constants.Result.CNT_ROW * DataServiceImpl.resultIndex;
        int sCell = Constants.Result.START_CELL;
        DataServiceImpl.resultIndex++;

        XSSFSheet sheet = wb.getSheet(Constants.Sheets.RESULT);
        attributes = Arrays.stream(attributes)
                .sorted(Comparator.comparing(i -> i.placeName))
                .toArray(Attribute[]::new);

        setDoubleValueInXLS(sheet, sRow, sCell, DataServiceImpl.resultIndex);
        setStringValueInXLS(sheet, sRow, sCell + 1, DataServiceImpl.resultIndex + "Бонусы");
        setStringValueInXLS(sheet, sRow, Constants.Columns.CHARACTER_NAME + 1, "Бонусы");
        for (int j = 0; j < Constants.VAL_COUNT; j++) {
            setDoubleValueInXLS(sheet, sRow, Constants.VAL_COLL_START + 1 + j, (int) bonuses[j]);
        }
        sRow = sRow + 1;
        for (int i = 0; i < Constants.PLACES_COUNT; i++) {
            int row = sRow + i;
            setDoubleValueInXLS(sheet, row, sCell, DataServiceImpl.resultIndex);
            setStringValueInXLS(sheet, row, Constants.Columns.CHARACTER_NAME + 1, attributes[i].characterName);
            setStringValueInXLS(sheet, row, Constants.Columns.PLACE + 1, attributes[i].placeName);
            setStringValueInXLS(sheet, row, Constants.Columns.RARITY + 1, attributes[i].rarity);
            setDoubleValueInXLS(sheet, row, Constants.Columns.RANK + 1, attributes[i].rank);
            setStringValueInXLS(sheet, row, Constants.Columns.TYPE + 1, attributes[i].type);
            for (int j = 0; j < Constants.VAL_COUNT; j++) {
                setStringValueInXLS(sheet, row, sCell + 1, DataServiceImpl.resultIndex + attributes[i].placeName);
                setDoubleValueInXLS(sheet, row, Constants.VAL_COLL_START + 1 + j, attributes[i].values[j]);
            }
            setDoubleValueInXLS(sheet, row, Constants.VAL_COLL_START + 1 + Constants.PLACES_COUNT, attributes[i].id);
        }
    }

    public static void setDoubleValueInXLS(XSSFSheet sheet, int row, int cell, double value) {
        XSSFCell xlsCell = getPreparedCell(sheet, row, cell);
        xlsCell.setCellValue(value);
    }

    public static void setStringValueInXLS(XSSFSheet sheet, int row, int cell, String value) {
        XSSFCell xlsCell = getPreparedCell(sheet, row, cell);
        xlsCell.setCellValue(value);
    }

    public static XSSFCell getPreparedCell(XSSFSheet sheet, int row, int cell) {
        XSSFRow xlsRow = sheet.getRow(row);
        if (xlsRow == null) {
            xlsRow = sheet.createRow(row);
        }
        XSSFCell xlsCell = xlsRow.getCell(cell);
        if (xlsCell == null) {
            xlsCell = xlsRow.createCell(cell);
        }
        return xlsCell;
    }

    public static XSSFWorkbook getWorkbook(String file) throws IOException {
        logger.info("Reading file {}", file);
        InputStream ExcelFileToRead = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(ExcelFileToRead);
        workbook.setForceFormulaRecalculation(true);
        return workbook;
    }

    public static Character getCharacter(XSSFWorkbook wb) {
        XSSFSheet sheet = wb.getSheet(Constants.Sheets.FIND);
        XSSFRow xx = sheet.getRow(Constants.Character.ROW_INDEX_NAME);
        String name = XssfUtils.getStringValueSafe(sheet.getRow(Constants.Character.ROW_INDEX_NAME).getCell(Constants.Character.COLL_INDEX_NAME));
        String element = XssfUtils.getStringValueSafe(sheet.getRow(Constants.Character.ROW_INDEX_ELEMENT).getCell(Constants.Character.COLL_INDEX_ELEMENT));
        String fraction = XssfUtils.getStringValueSafe(sheet.getRow(Constants.Character.ROW_INDEX_FRACTION).getCell(Constants.Character.COLL_INDEX_FRACTION));
        Main.ATTRIBUTE_FILTER_VALUE = XssfUtils.getDoubleValueSafe(sheet.getRow(Constants.Character.ROW_INDEX_FILTER).getCell(Constants.Character.COLL_INDEX_FILTER));

        logger.info("loaded Character; name = {}, fraction = {}, element = {}, filter value = {}", name, fraction, element, Main.ATTRIBUTE_FILTER_VALUE);
        return (new Character(name, fraction, element));
    }

    public static Place[] getPlaces(XSSFWorkbook wb) {
        logger.info("get Places");
        XSSFSheet sheet = wb.getSheet(Constants.Sheets.FIND);
        String[] names = XssfUtils.getStringArray(sheet, Constants.PLACES_ROW, Constants.PLACES_COLL_START, Constants.PLACES_COUNT);
        double[] orderByArray = XssfUtils.getDoubleArray(sheet, Constants.PLACES_ROW + 1, Constants.PLACES_COLL_START, Constants.PLACES_COUNT);

        List<Place> placeList = new ArrayList<>();
        for (int i = 0; i < Constants.PLACES_COUNT; i++) {
            double[] t1 = XssfUtils.getAttribColorFilter(sheet, Constants.PLACES_ROW + 2, Constants.PLACES_COLL_START + i, 3, 3, 0);
            double[] t2 = XssfUtils.getAttribColorFilter(sheet, Constants.PLACES_ROW + 5, Constants.PLACES_COLL_START + i, 3, 8, 1);

            placeList.add(new Place(names[i], i > 5, (int) orderByArray[i], t1, t2));
        }
        return placeList.stream().sorted(Comparator.comparingInt(i -> i.orderBy)).toArray(Place[]::new);
    }

    public static double[] getBase(XSSFWorkbook wb) {
        logger.info("get Base");
        XSSFSheet sheet = wb.getSheet(Constants.Sheets.FIND);
        return XssfUtils.getDoubleArray(sheet, Constants.VAL_BASE_ROW, Constants.VAL_COLL_START, Constants.VAL_COUNT);
    }

    public static double[] getLeagueAndZal(XSSFWorkbook wb, double[] base) {
        logger.info("get League And Zal");
        XSSFSheet sheet = wb.getSheet(Constants.Sheets.FIND);
        double[] valuesPr = XssfUtils.getDoubleArray(sheet, Constants.VAL_LEAGUE_ROW, Constants.VAL_COLL_START - 3, 3);
        double[] values = XssfUtils.getDoubleArray(sheet, Constants.VAL_LEAGUE_ROW, Constants.VAL_COLL_START, Constants.VAL_COUNT);

        values[Constants.Indexes.ZD] = base[Constants.Indexes.ZD] * valuesPr[0] / 100;
        values[Constants.Indexes.ATK] = base[Constants.Indexes.ATK] * valuesPr[1] / 100;
        values[Constants.Indexes.DEF] = base[Constants.Indexes.DEF] * valuesPr[2] / 100;

        return values;
    }

    public static double[] getGlyphs(XSSFWorkbook wb) {
        logger.info("get Glyphs");
        XSSFSheet sheet = wb.getSheet(Constants.Sheets.FIND);
        return XssfUtils.getDoubleArray(sheet, Constants.VAL_GLYPHS_ROW, Constants.VAL_COLL_START - 4, Constants.VAL_COUNT + 4);
    }

    public static double[] getTarget(XSSFWorkbook wb) {
        logger.info("get Target");
        XSSFSheet sheet = wb.getSheet(Constants.Sheets.FIND);

        double[] result = XssfUtils.getDoubleArray(sheet, Constants.VAL_TARGET_ROW, Constants.VAL_COLL_START, Constants.VAL_COUNT);
        for (int i =0; i < result.length;i++){
            result[i] = (int)result[i];
        }

        return  result;
    }

    public static double[] getEffectiveTarget(XSSFWorkbook wb) {
        logger.info("get Effective Target");
        XSSFSheet sheet = wb.getSheet(Constants.Sheets.FIND);
        return XssfUtils.getDoubleArray(sheet, Constants.VAL_TARGET_ROW, Constants.VAL_EFFECTIVE_COLL_START, Constants.VAL_EFFECTIVE_COUNT);
    }

    public static Map<String, Bonus> getBonuses(XSSFWorkbook wb, double[] base) {
        logger.info("get Bonuses");
        Map<String, Bonus> result = new TreeMap<>();
        XSSFSheet sheet = wb.getSheet(Constants.Sheets.SETS);
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

    public static void setParentId(XSSFWorkbook wb, List<Attribute> allAttributes) {
        XSSFSheet sheet = wb.getSheet(Constants.Sheets.ART);
        int cnt = sheet.getLastRowNum();

        XSSFCellStyle yellowStyle = DataServiceImpl.getStyle(wb, IndexedColors.YELLOW.getIndex());
        XSSFCellStyle greenStyle = DataServiceImpl.getStyle(wb, IndexedColors.LIGHT_GREEN.getIndex());
        XSSFCellStyle styleDef = wb.createCellStyle();

        for (int i = Constants.ATR_START_ROW; i <= cnt; i++) {
            XSSFCell xlsCellId = getPreparedCell(sheet, i, Constants.Columns.ID);
            XSSFCell xlsCellParent = getPreparedCell(sheet, i, Constants.Columns.PARENT_ID);

            xlsCellId.setCellStyle(styleDef);
            xlsCellParent.setCellStyle(styleDef);
            xlsCellParent.setCellValue("");

            double id = XssfUtils.getDoubleValueSafe(xlsCellId);
            double filterId = allAttributes.stream()
                    .filter(attribute -> attribute.parentId > 0)
                    .filter(attribute -> attribute.id == id)
                    .map(attribute -> attribute.parentId)
                    .findAny().orElse(-1d);

            if (filterId > 0) {
                xlsCellId.setCellStyle(yellowStyle);
                xlsCellParent.setCellValue(filterId);
            }

            double parentId = allAttributes.stream()
                    .filter(attribute -> attribute.parentId == id)
                    .map(attribute -> attribute.parentId)
                    .findAny().orElse(-1d);

            if (parentId > 0) {
                xlsCellId.setCellStyle(greenStyle);
                xlsCellParent.setCellValue(0);
            }
        }
    }

    private static XSSFCellStyle getStyle(XSSFWorkbook wb, short colorIndex) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(colorIndex);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    public static List<Attribute> getAllAttributes(XSSFWorkbook wb, double[] base, Map<String, Bonus> bonuses, Place[] places, double[] glyphs) {
        List<Attribute> result = new ArrayList<>();
        logger.info("get All Attributes");
        XSSFSheet sheet = wb.getSheet(Constants.Sheets.ART);
        int cnt = sheet.getLastRowNum();

        Map<String, Place> placeMap = Arrays.asList(places).stream().collect(Collectors.toMap(place -> place.name, Function.identity()));
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
                    }
                }
                for (int j = 0; j < pl.filterMain.length; j++) {
                    if (values[j] >= pl.filterMain[j]) {
                        filter = true;
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
                }

                values[Constants.Indexes.ZD] = values[Constants.Indexes.ZD] + base[Constants.Indexes.ZD] * valuesPr[0] / 100;
                values[Constants.Indexes.ATK] = values[Constants.Indexes.ATK] + base[Constants.Indexes.ATK] * valuesPr[1] / 100;
                values[Constants.Indexes.DEF] = values[Constants.Indexes.DEF] + base[Constants.Indexes.DEF] * valuesPr[2] / 100;

                Bonus bonus = bonuses.get(type);
                if (bonus == null && !pl.checkFraction ) {
                    logger.info("Empty Bonus = {}", type);
                }
                Attribute attr = new Attribute(id, character, place, rarity, glyphsIndex, rank, type, bonus, filterFlag, values);
                attr.tmpCharacterName = tmpCharacter;
                attr.place = pl;
                result.add(attr);
            }

//            if (bonus == null && placesNameList.contains(place)) {
//                //   logger.info("Empty place name = {}", attr);
//                //   throw new Error("Empty place name = " + attr);
//            }
//            if (place.equals("6.Сапоги") && attr.values[Constants.Indexes.SKOR] < 40){
//                filter = false;
//            }
//            if (place.equals("4.Перчатки") &&
//                    !(attr.values[Constants.Indexes.KRIT_S] >= 50 || attr.values[Constants.Indexes.KRIT_V] >= 65)){
//                filter = false;
//            }
        }

        return result;
    }

    public static void addGlyphs(double glyphsIndex, int mainPrIndex, double[] valuesPr, int mainIndex,
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

    public static double addGlyph(double glyphsIndex, double glyphsMax, double glyph) {
        double result = 0;
        if (glyphsIndex < glyphsMax) {
            result = glyph * (glyphsMax - glyphsIndex) / glyphsMax;
        }
        return result;
    }


}
