package com.gri.repository;

import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.model.Character;
import com.gri.model.Place;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface GetDataRepository {

    Character getCharacter(Double attributeFilterValue);

    Place[] getPlaces();

    double[] getBase();

    double[] getLeagueAndZal(double[] base);

    double[] getGlyphs();

    double[] getTarget();

    double[] getEffectiveTarget();

    Map<String, Bonus> getBonuses(double[] base);

    List<Attribute> getAllAttributes(double[] base, Map<String, Bonus> bonuses, Place[] places, double[] glyphs, Character character);

    void close() throws IOException;
}
