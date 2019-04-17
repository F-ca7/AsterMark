package team.aster.processor;

/**
 * 构造水印处理器的对外方法
 */
public class WatermarkFactory {

    /**
     * @Title: getWatermarkProcessor
     * @Description: 通过工厂来返回不同类型的水印处理器
     * @author Fcat
     * @date 2019/3/23
     * @param watermarkType 水印类型的枚举
     * @return team.aster.processor.WatermarkProcessor
     */
    public WatermarkProcessor getWatermarkProcessor(WatermarkProcessorType watermarkType){

        switch (watermarkType){
            case OPTIMIZATION:
                return new OptimBasedWatermark();
            case PRIMITIVE_LSB:
                return new PrimitiveLSBWatermark();
            case SPACE:
                return new SpaceBasedWatermark();
            case PUNCTUATION:
                return new PunctuationBasedWatermark();
            default:
                return null;
        }
    }


}
