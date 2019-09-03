package team.aster.processor;

import team.aster.model.ColumnDataConstraint;
import team.aster.model.StoredKey;
import team.aster.model.WaterMark;

public abstract class IEncoderTextImpl implements IEncoder {
    // 秘钥信息构造器，初始化时只有部分信息
    StoredKey.Builder storedKeyBuilder;

    @Override
    public void setStoredKeyBuilder(StoredKey.Builder storedKeyBuilder) {
            this.storedKeyBuilder = storedKeyBuilder;
    }


    @Override
    public StoredKey getStoredKey() {
        return storedKeyBuilder.build();
    }

    void completeStoredKey(int partitionCount, String secretCode, WaterMark waterMark){
        storedKeyBuilder.setSecretCode(secretCode);
        storedKeyBuilder.setWaterMark(waterMark);
        storedKeyBuilder.setWmLength(waterMark.getLength());
        storedKeyBuilder.setPartitionCount(partitionCount);
    }

    @Override
    public void setDataConstraint(ColumnDataConstraint dataConstraint) {

    }
}
