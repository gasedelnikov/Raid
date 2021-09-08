package com.gri.service;

import com.gri.model.Attribute;
import com.gri.model.Result;

import java.util.List;

public interface CalculationService {

    List<Result> startCalculation(int i, int j, Attribute[][] attribute, double[] targetDelta);

    List<Result> startCalculation(Attribute[][] attribute, double[] targetDelta);

//    boolean calcAttributeCortege(double[] targetDelta, Attribute... attributes);

    Attribute[][] filterAttributesRecursive(Attribute[][] attributes, double[] targetDelta);

    Attribute[][] filterByCortege(double[] targetDelta, Attribute[][] attribute, Attribute[] cortege);

}
