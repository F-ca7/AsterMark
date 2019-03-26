package team.aster.algorithm;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

public class GenericOptimization {


    /**
     * @Description 获取对应的hiding function的值
     * @author Fcat
     * @date 2019/3/26 16:15
     * @param colValues	一个字段的所有属性值
     * @param secretKey	秘参c
     * @return double
     */
    public static double getHidingValue(ArrayList<Double> colValues, double secretKey){
        DescriptiveStatistics stats = new DescriptiveStatistics();
        //把数组的值添加到统计量中
        colValues.forEach(stats::addValue);
        double mean = stats.getMean();
        double variance = stats.getVariance();
        return mean + secretKey*variance;
    }
}
