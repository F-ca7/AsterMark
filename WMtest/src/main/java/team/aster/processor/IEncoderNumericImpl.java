package team.aster.processor;

import team.aster.model.ColumnDataConstraint;
import team.aster.model.StoredKey;
import team.aster.model.WaterMark;

/**
 * @Description IEncoder接口对于数值型数据的实现
 * @author Fcat
 * @date 2019/4/10 9:20
 */
public abstract class IEncoderNumericImpl implements IEncoder {

    // 秘钥信息构造器，初始化时只有部分信息
    StoredKey.Builder storedKeyBuilder;

    // 编码器对应的约束
    ColumnDataConstraint dataConstraint;


    @Override
    public StoredKey getStoredKey() {
        return storedKeyBuilder.build();
    }

    void completeStoredKey(int partitionCount, String secretCode, double threshold, WaterMark waterMark){
        storedKeyBuilder.setSecretCode(secretCode);
        storedKeyBuilder.setThreshold(threshold);
        storedKeyBuilder.setWaterMark(waterMark);
        storedKeyBuilder.setWmLength(waterMark.getLength());
        storedKeyBuilder.setPartitionCount(partitionCount);
    }

    // 如果约束不为数值型，抛出非法参数异常
    public void setDataConstraint(ColumnDataConstraint dataConstraint) throws IllegalArgumentException {
        if (!dataConstraint.getConstraintType().isNumeric()){
            throw new IllegalArgumentException("只能为数值型的约束! ");
        }
        this.dataConstraint = dataConstraint;
    }

    @Override
    public void setStoredKeyBuilder(StoredKey.Builder storedKeyBuilder) {
        this.storedKeyBuilder = storedKeyBuilder;
    }
}
