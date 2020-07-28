package com.gri.utils;

import com.gri.Main;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XssfUtils {
    private static Logger logger = LoggerFactory.getLogger(XssfUtils.class);

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
                result = Double.toString(cell.getNumericCellValue()) ;
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
//            throw new IllegalStateException("Empty cell");
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

    public static double[] getAttribColorFilter(XSSFSheet sheet, int rowStart, int colNum, int cnt, int arraySize, int key) {
        double[] result = new double[arraySize];

        XSSFRow row0 = sheet.getRow(Constants.PLACES_ROW +1);
        XSSFCell cell0 = row0.getCell(colNum);
        boolean getAll = false;
        if (cell0 != null && cell0.getCellStyle() != null && cell0.getCellStyle().getFillForegroundColorColor() != null) {
            String color = cell0.getCellStyle().getFillForegroundColorColor().getARGBHex();
            getAll = "FF00B050".equals(color); // зеленый
        }
        if (!getAll) {
            for (int i = 0; i < arraySize; i++) {
                result[i] = Double.MAX_VALUE;
            }

            for (int i = 0; i < cnt; i++) {
                XSSFRow row = sheet.getRow(rowStart + i);
                if (key == getDoubleValueSafe(row.getCell(32))) {
                    int index = (int) getDoubleValueSafe(row.getCell(33));

                    XSSFCell cell = row.getCell(colNum);
                    if (cell != null && cell.getCellStyle() != null && cell.getCellStyle().getFillForegroundColorColor() != null) {
                        String color = cell.getCellStyle().getFillForegroundColorColor().getARGBHex();
                        if ("FF00B050".equals(color)){  // зеленый  //      "FFFF0000"  // красный
                            result[index] = getDoubleValueSafe(cell);
                        }
                    }
                }
            }
        }
//        else{
//            for (int i = 0; i < arraySize; i++) {
//                result[i] = true;
//            }
//        }
        return result;
    }
}