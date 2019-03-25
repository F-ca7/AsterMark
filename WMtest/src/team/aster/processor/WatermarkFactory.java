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
    public WatermarkProcessor getWatermarkProcessor(WatermarkType watermarkType){

        switch (watermarkType){
            case OPTIMIZATION:
                return new OptimBasedWatermark();
            default:
                return null;
        }
    }


}
