package com.gri.service;

import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public interface BonusService {

    double[] getAttributeBonuses(Attribute... attributes);

}
