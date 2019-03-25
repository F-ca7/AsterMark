package team.aster.processor;

import team.aster.model.DatasetWithPK;

public abstract class WatermarkProcessor {
    IEncoder encoder;
    IDecoder decoder;

    public void encodeDB(DatasetWithPK datasetWithPK){

        encoder.encode(datasetWithPK);
    }

    public String decodeDB(DatasetWithPK datasetWithPK){

        return decoder.decode(datasetWithPK);
    }
}
