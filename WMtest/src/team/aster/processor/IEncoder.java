package team.aster.processor;

import team.aster.model.DatasetWithPK;

public interface IEncoder {
    void encode(DatasetWithPK datasetWithPK);
}
