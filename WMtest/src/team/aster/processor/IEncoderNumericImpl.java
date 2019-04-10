package team.aster.processor;

import team.aster.model.ColumnDataConstraint;
import team.aster.model.StoredKey;
import team.aster.model.WaterMark;

/**
 * @ClassName IEncoderNumericImpl
 * @Description IEncoder接口对于数值型数据的实现
 * @author Fcat
 * @date 2019/4/10 9:20
 */
public abstract class IEncoderNumericImpl implements IEncoder {

    // 秘钥信息构造器，初始化时只有部分信息
    StoredKey.Builder storedKeyBuilder;



    // 编码器对应的约束
    ColumnDataConstraint dataConstraint;



    abstract public void completeStoredKey(String secretCode,double threshold, WaterMark waterMark);

    // 如果约束不为数值型，抛出非法参数异常
    public void setDataConstraint(ColumnDataConstraint dataConstraint) throws IllegalArgumentException {
        if (!dataConstraint.getConstraintType().isNumeric()){
            throw new IllegalArgumentException("只能为数值型的约束! ");
        }
        this.dataConstraint = dataConstraint;
    }

    public void setStoredKeyBuilder(StoredKey.Builder storedKeyBuilder) {
        this.storedKeyBuilder = storedKeyBuilder;
    }
}
