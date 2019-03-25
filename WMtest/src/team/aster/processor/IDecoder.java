package team.aster.processor;

import team.aster.model.DatasetWithPK;

public interface IDecoder {
    String decode(DatasetWithPK datasetWithPK);
}
