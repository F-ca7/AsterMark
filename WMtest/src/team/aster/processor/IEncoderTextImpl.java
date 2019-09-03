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

    void completeStoredKey(String secretCode, WaterMark waterMark){
        storedKeyBuilder.setSecretCode(secretCode);
        storedKeyBuilder.setWaterMark(waterMark);
        storedKeyBuilder.setWmLength(waterMark.getLength());
    }

    @Override
    public void setDataConstraint(ColumnDataConstraint dataConstraint) {

    }
}
