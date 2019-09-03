package team.aster.processor;

import team.aster.model.DatasetWithPK;
import team.aster.model.WatermarkException;

import java.util.ArrayList;

public abstract class WatermarkProcessor {
    IEncoder encoder;
    IDecoder decoder;

    public void encodeDB(DatasetWithPK datasetWithPK, ArrayList<String> watermarkList) throws WatermarkException {

        encoder.encode(datasetWithPK, watermarkList);
    }

    public String decodeDB(DatasetWithPK datasetWithPK){

        return decoder.decode(datasetWithPK);
    }

    public IEncoder getEncoder() {
        return encoder;
    }

    public IDecoder getDecoder() {
        return decoder;
    }
}
