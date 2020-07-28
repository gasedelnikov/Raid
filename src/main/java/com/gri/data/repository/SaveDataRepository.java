package com.gri.data.repository;

import com.gri.model.Attribute;
import com.gri.model.Bonus;
import com.gri.model.Character;
import com.gri.model.Place;
import com.gri.model.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SaveDataRepository {

    void saveMainResults(List<Result> resultList) throws IOException;

    void close() throws IOException;
}
