package com.gri.service;

import com.gri.Main;
import com.gri.model.Attribute;
import com.gri.model.Character;
import com.gri.model.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface FilterService {

    Attribute[][] convertListToArray(Place[] places,
                                     List<Attribute> allAttributes,
                                     Character character);

    Attribute[][] getCharacterAttributes(double[] targetDelta,
                                         List<Attribute> allAttributes,
                                         Place[] places,
                                         Character character) ;

    void setAttributeParentId(Attribute[][] attributes);

    Attribute[][] filterAttributesByValues(Attribute[][] attributes);
}
