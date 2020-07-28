package com.gri.repository.impl;

import com.gri.repository.SaveDataRepository;
import com.gri.model.Attribute;
import com.gri.model.Result;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SaveDataXssfRepositoryImpl implements SaveDataRepository {
    private Logger logger = LoggerFactory.getLogger(SaveDataXssfRepositoryImpl.class);

        private String outFileName;
    private XSSFWorkbook workbook;

    public SaveDataXssfRepositoryImpl(String infileName, String outFileName) throws IOException {
        workbook = XssfUtils.getWorkbook(infileName);
        this.outFileName = outFileName;
    }

    @Override
    public void saveMainResults(List<Result> resultList) throws IOException{
        for (int i=0; i < resultList.size(); i++){
            putResult(i, resultList.get(i).getAttributes(), resultList.get(i).getBonuses());
        }
        saveFile();
    }

    @Override
    public void close() throws IOException{
        workbook.close();
    }

    public void saveAttributeParentId(List<Attribute> allAttributes) throws IOException{
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

        saveFile();
    }

    private void saveFile() throws IOException {
        logger.info("save result to File {}", outFileName);
        FileOutputStream fos = new FileOutputStream(outFileName);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    private void putResult(int resultIndex, Attribute[] attributes, double[] bonuses) {
        int sRow = Constants.Result.START_ROW + Constants.Result.CNT_ROW * resultIndex;
        int sCell = Constants.Result.START_CELL;

        XSSFSheet sheet = workbook.getSheet(Constants.Sheets.RESULT);
        attributes = Arrays.stream(attributes)
                .sorted(Comparator.comparing(i -> i.placeName))
                .toArray(Attribute[]::new);

        setDoubleValueInXLS(sheet, sRow, sCell, resultIndex);
        setStringValueInXLS(sheet, sRow, sCell + 1, resultIndex + "Бонусы");
        setStringValueInXLS(sheet, sRow, Constants.Columns.CHARACTER_NAME + 1, "Бонусы");
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
