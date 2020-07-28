package com.gri.data;

import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.model.Character;
import com.gri.model.Place;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DataService {

    XSSFWorkbook getWorkbook(String file) throws IOException;

    XSSFCell getPreparedCell(XSSFSheet sheet, int row, int cell);

    void setDoubleValueInXLS(XSSFSheet sheet, int row, int cell, double value);

    void setStringValueInXLS(XSSFSheet sheet, int row, int cell, String value);



    void saveFile(XSSFWorkbook workbook, String filename) throws IOException;

    void putResult(XSSFWorkbook wb, Attribute[] attributes, double[] bonuses);






    Character getCharacter(XSSFWorkbook wb);

    Place[] getPlaces(XSSFWorkbook wb);

    double[] getBase(XSSFWorkbook wb);

    double[] getLeagueAndZal(XSSFWorkbook wb, double[] base);

    double[] getGlyphs(XSSFWorkbook wb);

    double[] getTarget(XSSFWorkbook wb);

    double[] getEffectiveTarget(XSSFWorkbook wb);

    Map<String, Bonus> getBonuses(XSSFWorkbook wb, double[] base);

    void setParentId(XSSFWorkbook wb, List<Attribute> allAttributes);

    List<Attribute> getAllAttributes(XSSFWorkbook wb, double[] base, Map<String, Bonus> bonuses, Place[] places, double[] glyphs);

    void addGlyphs(double glyphsIndex, int mainPrIndex, double[] valuesPr, int mainIndex,
                   double[] values, double[] glyphs);

    double addGlyph(double glyphsIndex, double glyphsMax, double glyph);


}
