package com.gri.repository;

import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.model.Character;
import com.gri.model.Place;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DataRepository {

    Character getCharacter(Double attributeFilterValue);

    Place[] getPlaces();

    Place[] getPlaces(int limit);

    Place[] getPlaces(int limit, boolean setFilter);

    double[] getBase();

    double[] getLeagueAndZal(double[] base);

    double[] getGlyphs();

    double[] getTarget();

    double[] getEffectiveTarget();

    Map<String, Bonus> getBonuses(double[] base);

    List<Attribute> getAllAttributes(double[] base, Map<String, Bonus> bonuses, Place[] places, double[] glyphs, Character character);

    List<Attribute> getAllAttributes(double[] base, Map<String, Bonus> bonuses, Place[] places, double[] glyphs, Character character, boolean withoutFlatValues) ;

    void close() throws IOException;
}
