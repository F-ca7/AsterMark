package team.aster.algorithm;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 最优化算法抽象
 */
public abstract class OptimizationAlgorithm {
    private static Logger logger =  LoggerFactory.getLogger(OptimizationAlgorithm.class);

    public abstract double maximizeByHidingFunction(ArrayList<Double> colValues, double ref, double lower, double upper);
    public abstract double minimizeByHidingFunction(ArrayList<Double> colValues, double ref, double lower, double upper);
    public abstract ArrayList<Double> getModifiedColumn();



    /**
     * 获取ref值，供sigmoid函数使用
     * @author Fang
     * @date 2019/3/26 16:15
     * @param colValues	一个字段的所有值
     * @param secretKey	秘参c
     */
    public static double getRefValue(ArrayList<Double> colValues, double secretKey){
        //把数组的值添加到统计量中
        logger.info("ref为{}",PatternSearch.OREF);
        return PatternSearch.OREF;
    }

    public static double getOHidingValue(ArrayList<Double> colValues, double secretKey){
        double ref = getRefValue(colValues, secretKey);
        double sum = 0.0;
        for (double i:colValues){
            sum += getSigmoid(i, ref);
        }
        return sum/(double)colValues.size();
    }


    private static double getSigmoid(double i, double oref) {
        final double ALPHA = 8;
        return (1.0-1.0/(1+Math.exp(ALPHA*(i-oref))));
    }


    /**
     * 计算最优化的阈值T,为了防止精度丢失导致的巨大误差
     * 此处，阈值计算约为meanMax与meanMin的均值
     * @author Fang
     * @date 2019/3/26 17:13
     * @param minList
     * @param maxList
     * @return double 最优化的阈值T
     */
    public static double calcOptimizedThreshold(ArrayList<Double> minList, ArrayList<Double> maxList)
    {
        DescriptiveStatistics minStats = new DescriptiveStatistics();
        DescriptiveStatistics maxStats = new DescriptiveStatistics();
        minList.forEach(minStats::addValue);
        maxList.forEach(maxStats::addValue);

        double minMean = minStats.getMean();
        double maxMean = maxStats.getMean();
//        double minVar = minStats.getVariance();
//        double maxVar = maxStats.getVariance();
//        logger.info("min均值：{}, 方差：{}", minMean, minVar);
//        logger.info("max均值：{}, 方差：{}", maxMean, maxVar);

        return (minMean+maxMean)/2;

    }


    /**
     * @Description 返回一元二次方程较小的根
     * @author Fang
     * @date 2019/3/26 17:07
     * @return double
     */
    private static double getSmallerRootForQuad(double A, double B, double C){
        return ((-B - Math.sqrt(B*B - 4*A*C))/(2*A));
    }


    private static double getRootForLinear(double A, double B){
        return (-B)/A;
    }
}
