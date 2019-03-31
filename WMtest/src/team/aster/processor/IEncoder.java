package team.aster.processor;

import team.aster.model.DatasetWithPK;

import java.util.ArrayList;

public interface IEncoder {
    void encode(DatasetWithPK datasetWithPK, ArrayList<String> watermarkList);
}
