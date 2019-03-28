package team.aster.processor;

import team.aster.model.DatasetWithPK;

public interface IDecoder {
    //todo 返回watermark对象
    String decode(DatasetWithPK datasetWithPK);
}
