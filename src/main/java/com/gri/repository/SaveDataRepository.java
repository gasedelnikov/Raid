package com.gri.repository;

import com.gri.model.Attribute;
import com.gri.model.Character;
import com.gri.model.Result;

import java.io.IOException;
import java.util.List;

public interface SaveDataRepository {

    void saveMainResults(List<Result> resultList, Character character, double[] baseAndLeagueAndZal) throws IOException;

    void saveAttributeParentId(List<Attribute> allAttributes) throws IOException;

    void close() throws IOException;
}
