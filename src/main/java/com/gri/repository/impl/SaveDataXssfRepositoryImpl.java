package com.gri.repository.impl;

import com.gri.model.Character;
import com.gri.model.RankMask;
import com.gri.repository.SaveDataRepository;
import com.gri.model.Attribute;
import com.gri.model.Result;
import com.gri.utils.Constants;
import com.gri.utils.Utils;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SaveDataXssfRepositoryImpl implements SaveDataRepository {
    private Logger logger = LoggerFactory.getLogger(SaveDataXssfRepositoryImpl.class);

    private String outFileName;
    private XSSFWorkbook workbook;

    public SaveDataXssfRepositoryImpl(String infileName, String outFileName) throws IOException {
        workbook = XssfUtils.getWorkbook(infileName);
        this.outFileName = outFileName;
    }

    @Override
    public void saveMainResults(List<Result> resultList, Character character, double[] baseAndLeagueAndZal) throws IOException {
        for (int i = 0; i < resultList.size(); i++) {
            putResult(i, resultList.get(i).getAttributes(), resultList.get(i).getBonuses());
        }
        putSummaryResult(resultList, character, baseAndLeagueAndZal);
        saveFile();
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }

    @Override
    public void saveAttributeParentId(List<Attribute> allAttributes) throws IOException {
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.ART);
        int cnt = sheet.getLastRowNum();

        XSSFCellStyle yellowStyle = getStyle(IndexedColors.YELLOW.getIndex());
        XSSFCellStyle greenStyle = getStyle(IndexedColors.LIGHT_GREEN.getIndex());
        XSSFCellStyle styleDef = workbook.createCellStyle();

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
                xlsCellParent.setCellValue((int) filterId + "_c");
            }

            double parentId = allAttributes.stream()
                    .filter(attribute -> attribute.parentId == id)
                    .map(attribute -> attribute.parentId)
                    .findAny().orElse(-1d);

            if (parentId > 0) {
                xlsCellId.setCellStyle(greenStyle);
                xlsCellParent.setCellValue((int) id + "_p");
            }
        }

        saveFile();
    }

    @Override
    public void saveAttributeRang(Map<Double, Double[]> mapRank, List<RankMask> masks) throws IOException {
        final int startColumn = Constants.Columns.PARENT_ID + 5;
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.ART);
        int cnt = sheet.getLastRowNum();

        XSSFCellStyle yellowStyle = getStyle(IndexedColors.YELLOW.getIndex());
        XSSFCellStyle greenStyle = getStyle(IndexedColors.LIGHT_GREEN.getIndex());
        XSSFCellStyle styleDef = workbook.createCellStyle();

        for (int i = 0; i < masks.size(); i++) {
            XSSFCell xlsCellParent = getPreparedCell(sheet, Constants.ATR_START_ROW -1, startColumn + 1 + i);
            xlsCellParent.setCellValue(i);

            xlsCellParent = getPreparedCell(sheet, Constants.ATR_START_ROW -2, startColumn + 1 + i);
            xlsCellParent.setCellValue(masks.get(i).getName());
        }

        for (int i = Constants.ATR_START_ROW; i <= cnt; i++) {
            XSSFCell xlsCellId = getPreparedCell(sheet, i, Constants.Columns.ID);
            XSSFRow xlsRow = sheet.getRow(i);

            for (int j = 0; j < masks.size(); j++) {
                XSSFCell xlsCellParent = getPreparedCell(sheet, i, startColumn + j);
                xlsRow.removeCell(xlsCellParent);
            }

            double id = XssfUtils.getDoubleValueSafe(xlsCellId);
            Double[] ranks = mapRank.get(id);
            if (ranks != null) {
                double min = Double.MAX_VALUE;
                for (int j = 0; j < ranks.length; j++) {
//                    int maxCount = masks.get(j).getCount();
                    XSSFCell xlsCellParent = getPreparedCell(sheet, i, startColumn + 1 + j);

                    if (ranks[j] != null) {
                        double rank = ranks[j] ; // - maxCount - 1
                        xlsCellParent.setCellValue(rank);
                        min = Math.min(min, rank);
                    }
                }
                XSSFCell xlsCellParent = getPreparedCell(sheet, i, startColumn);
                if (min != Double.MAX_VALUE){
                    xlsCellParent.setCellValue(min);
                }
            }
        }
        saveFile();
    }

    @Override
    public void saveAttributeRangOnlyCount(Map<Double, Double[]> mapRank, List<RankMask> masks) throws IOException {
        final int startColumn = Constants.Columns.PARENT_ID + 4;
        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.ART);
        int cnt = sheet.getLastRowNum();

        for (int i = Constants.ATR_START_ROW; i <= cnt; i++) {
            XSSFCell xlsCellId = getPreparedCell(sheet, i, Constants.Columns.ID);
            XSSFRow xlsRow = sheet.getRow(i);

            double id = XssfUtils.getDoubleValueSafe(xlsCellId);
            Double[] ranks = mapRank.get(id);

            xlsRow.removeCell(getPreparedCell(sheet, i, startColumn));

            if (ranks != null) {
                double max = Double.MIN_VALUE;
                for (int j = 0; j < ranks.length; j++) {
                    if (ranks[j] != null) {
                        max = Math.max(max, ranks[j]);
                    }
                }

                if (max != Double.MAX_VALUE){
                    getPreparedCell(sheet, i, startColumn).setCellValue(max);
                }
            }
        }
        saveFile();
    }

    private void saveFile() throws IOException {
        FileOutputStream fos = new FileOutputStream(outFileName);
        workbook.write(fos);
        fos.close();
        workbook.close();
        logger.info("save result to File {}", outFileName);
    }

    private void putSummaryResult(List<Result> resultList, Character character, double[] baseAndLeagueAndZal) {
        int sRow = Constants.Result.SUMMARY_START_ROW - 1;
        int sCell = Constants.Result.SUMMARY_START_CELL;

        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.FIND);

        for (int i = 0; i < resultList.size(); i++) {
            Attribute[] attributes = Arrays.stream(resultList.get(i).getAttributes())
                    .sorted(Comparator.comparing(attribute -> attribute.placeName))
                    .toArray(Attribute[]::new);

            int row = sRow + i;
            setDoubleValueInXLS(sheet, row, sCell, i);

            int characterCnt = 0;
            for (int j = 0; j < Constants.PLACES_COUNT; j++) {
                String attributeInfo = attributes[j].characterName;
                if (character.name.equals(attributeInfo)) {
                    characterCnt++;
                }

                if (!attributes[j].place.checkFraction) {
                    if ("".equals(attributeInfo)) {
                        attributeInfo = attributes[j].type;
                    } else {
                        attributeInfo += "\\" + attributes[j].type;
                    }
                }
                setStringValueInXLS(sheet, row, sCell + j + 2, attributeInfo);
            }
            setDoubleValueInXLS(sheet, row, Constants.Result.SUMMARY_VAL_COLL_START - 1, characterCnt);

            double zd = 0;
            double atk = 0;
            double def = 0;
            double criticalValue = 0;
            for (int j = 0; j < Constants.VAL_COUNT; j++) {
                double value = resultList.get(i).getBonuses()[j] + baseAndLeagueAndZal[j];
                for (Attribute attribute : attributes) {
                    value += attribute.values[j];
                }
                switch (j) {
                    case Constants.Indexes.ZD: {
                        zd = value;
                        break;
                    }
                    case Constants.Indexes.ATK: {
                        atk = value;
                        break;
                    }
                    case Constants.Indexes.DEF: {
                        def = value;
                        break;
                    }
                    case Constants.Indexes.KRIT_V: {
                        criticalValue = value;
                        break;
                    }
                }

                setDoubleValueInXLS(sheet, row, j + Constants.Result.SUMMARY_VAL_COLL_START, value);
            }
            int index = Constants.Result.SUMMARY_EFFECTIVE_VAL_COLL_START;
            setDoubleValueInXLS(sheet, row, index, Utils.getCriticalEffectiveValue(zd, criticalValue));
            setDoubleValueInXLS(sheet, row, ++index, Utils.getCriticalEffectiveValue(atk, criticalValue));
            setDoubleValueInXLS(sheet, row, ++index, Utils.getCriticalEffectiveValue(def, criticalValue));
            setDoubleValueInXLS(sheet, row, ++index, Utils.getEffectiveZdValue(zd, def));

        }
    }

    private void putResult(int resultIndex, Attribute[] attributes, double[] bonuses) {
        int sRow = Constants.Result.ALL_START_ROW + Constants.Result.ALL_CNT_ROW * resultIndex;
        int sCell = Constants.Result.ALL_START_CELL;

        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.RESULT);
        attributes = Arrays.stream(attributes)
                .sorted(Comparator.comparing(i -> i.placeName))
                .toArray(Attribute[]::new);

        setDoubleValueInXLS(sheet, sRow, sCell, resultIndex);
        setStringValueInXLS(sheet, sRow, sCell + 1, resultIndex + Constants.Result.ALL_BONUS_TEXT);
        setStringValueInXLS(sheet, sRow, Constants.Columns.CHARACTER_NAME + 1, Constants.Result.ALL_BONUS_TEXT);
        for (int j = 0; j < Constants.VAL_COUNT; j++) {
            setDoubleValueInXLS(sheet, sRow, Constants.VAL_COLL_START + 1 + j, (int) bonuses[j]);
        }
        sRow = sRow + 1;
        for (int i = 0; i < Constants.PLACES_COUNT; i++) {
            int row = sRow + i;
            setDoubleValueInXLS(sheet, row, sCell, resultIndex);
            setStringValueInXLS(sheet, row, Constants.Columns.CHARACTER_NAME + 1, attributes[i].characterName);
            setStringValueInXLS(sheet, row, Constants.Columns.PLACE + 1, attributes[i].placeName);
            setStringValueInXLS(sheet, row, Constants.Columns.RARITY + 1, attributes[i].rarity);
            setDoubleValueInXLS(sheet, row, Constants.Columns.RANK + 1, attributes[i].rank);
            setStringValueInXLS(sheet, row, Constants.Columns.TYPE + 1, attributes[i].type);
            for (int j = 0; j < Constants.VAL_COUNT; j++) {
                setStringValueInXLS(sheet, row, sCell + 1, resultIndex + attributes[i].placeName);
                setDoubleValueInXLS(sheet, row, Constants.VAL_COLL_START + 1 + j, attributes[i].values[j]);
            }
            setDoubleValueInXLS(sheet, row, Constants.VAL_COLL_START + 1 + Constants.PLACES_COUNT, attributes[i].id);
            setDoubleValueInXLS(sheet, row, Constants.VAL_COLL_START + 2 + Constants.PLACES_COUNT, attributes[i].targetPriority);
            setDoubleValueInXLS(sheet, row, Constants.VAL_COLL_START + 3 + Constants.PLACES_COUNT, attributes[i].maxValuePriority);
        }
    }

    private XSSFCellStyle getStyle(short colorIndex) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(colorIndex);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void setDoubleValueInXLS(XSSFSheet sheet, int row, int cell, double value) {
        XSSFCell xlsCell = getPreparedCell(sheet, row, cell);
        xlsCell.setCellValue(value);
    }

    private void setStringValueInXLS(XSSFSheet sheet, int row, int cell, String value) {
        XSSFCell xlsCell = getPreparedCell(sheet, row, cell);
        xlsCell.setCellValue(value);
    }

    private XSSFCell getPreparedCell(XSSFSheet sheet, int row, int cell) {
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


}
