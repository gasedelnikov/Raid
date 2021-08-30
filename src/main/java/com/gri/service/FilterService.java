package com.gri.service;

import com.gri.model.Attribute;
import com.gri.model.Character;
import com.gri.model.Place;

import java.util.List;

public interface FilterService {

    Attribute[][] convertListToArray(Place[] places,
                                     List<Attribute> allAttributes,
                                     Character character);

    Attribute[][] getCharacterAttributes(double[] targetDelta,
                                         List<Attribute> allAttributes,
                                         Place[] places,
                                         Character character) ;

    void setAttributeParentId(Attribute[][] attributes);

    Attribute[][] filterAttributesByDoubles(Attribute[][] attributes);

    Attribute[][] filterAttributesByDoublesAndMask(Attribute[][] attributes, double[] target, Character character);
}
