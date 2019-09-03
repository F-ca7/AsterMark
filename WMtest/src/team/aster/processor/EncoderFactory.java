package team.aster.processor;

public class EncoderFactory {

    /**
     * @Description 通过工厂来返回不同类型的水印编码器
     * @author Fcat
     * @date 2019/4/20
     * @param watermarkType 水印类型的枚举
     * @return team.aster.processor.WatermarkProcessor
     */
    public IEncoder getEncoder(WatermarkProcessorType watermarkType){
        switch (watermarkType){
            case OPTIMIZATION:
                return new OptimEncoder();
            case PRIMITIVE_LSB:
                return new PrimLSBEncoder();
            case SPACE:
                return new SpaceEncoder();
            case PUNCTUATION:
                return new PunctuationEncoder();
            case PART_OF_SPEECH:
                return new POSEncoder();
            default:
                return null;
        }
    }
}
