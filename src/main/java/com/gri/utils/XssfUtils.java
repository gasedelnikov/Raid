package com.gri.utils;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XssfUtils {
    private static Logger logger = LoggerFactory.getLogger(XssfUtils.class);

    public static XSSFWorkbook getWorkbook(String file) throws IOException {
        logger.info("get Workbook from file {}", file);
        InputStream ExcelFileToRead = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(ExcelFileToRead);
        workbook.setForceFormulaRecalculation(true);
        return workbook;
    }

    public static String[] getStringArray(XSSFSheet sheet, int rowNum, int colStart, int cnt) {
        XSSFRow row = sheet.getRow(rowNum);
        String[] result = new String[cnt];
        for (int i = 0; i < result.length; i++) {
            result[i] = XssfUtils.getStringValueSafe(row.getCell(colStart + i));
        }
        return result;
    }

    public static double[] getDoubleArray(XSSFSheet sheet, int rowNum, int colStart, int cnt) {
        XSSFRow row = sheet.getRow(rowNum);
        double[] result = new double[cnt];
        for (int i = 0; i < result.length; i++) {
            result[i] = XssfUtils.getDoubleValueSafe(row.getCell(colStart + i));
        }
        return result;
    }

    public static String getStringValueSafe(XSSFCell cell) {
        String result;
        try {
            result = cell == null ? "" : cell.getStringCellValue().trim();
        } catch (IllegalStateException ex) {
            try {
                result = Double.toString(cell.getNumericCellValue());
            } catch (IllegalStateException ex2) {
                result = "";
            }
        }
        return result;
    }

    public static double getDoubleValueSafe(XSSFCell cell) {
        double result = 0;
        try {
            result = (cell == null) ? 0 : cell.getNumericCellValue();
        } catch (IllegalStateException ex) {
//            logger.info("Empty cell = {}", cell.getStringCellValue());
        }
        return result;
    }

    public static int getColorIndex(XSSFSheet sheet, int rowNum, int colStart, int cnt, String color) {
        int result = -1;
        XSSFRow row = sheet.getRow(rowNum);
        for (int i = 0; i < cnt; i++) {
            XSSFCell cell = row.getCell(colStart + i);
            if (cell != null && cell.getCellStyle() != null && cell.getCellStyle().getFillForegroundColorColor() != null) {
                if (color.equals(cell.getCellStyle().getFillForegroundColorColor().getARGBHex())) {
                    result = i;
                }
            }
        }
        return result;
    }

}