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
     * @update Kun-提出中位数分割法
     */
    public static double getRefValue(ArrayList<Double> colValues, double secretKey){
//        DescriptiveStatistics stats = new DescriptiveStatistics();
//        //把数组的值添加到统计量中
//        colValues.forEach(stats::addValue);
//        double mean = stats.getMean();
//        double varianceSqrt = Math.sqrt(stats.getVariance());
//        double ref = mean + secretKey*varianceSqrt;

        ArrayList<Double> tmpList = new ArrayList<>(colValues);
        tmpList.sort(Comparator.naturalOrder());
        double mid = tmpList.get(colValues.size()/2);

//        if (varianceSqrt>mean) {
//
//            // 若数据集过于分散，则进行均化处理
//            double sum = 0;
//            for (double i:colValues){
//                sum += getSigmoid(i, ref);
//            }
//            logger.info("标准差大于均值，返回{}", sum/(double)colValues.size());
//            return sum/(double)colValues.size();
//        }
        //logger.info("标准差小于均值，返回{}", mid);
        return mid;

    }


    public static double getOHidingValue(ArrayList<Double> colValues, double secretKey){
        double sum = 0.0;
        for (double i:colValues){
            sum += getSigmoid(i, PatternSearch.OREF);
        }
        return sum/(double)colValues.size();
    }

    private static double getSigmoid(double i, double oref) {
        double ALPHA = 8;
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
        double minVar = minStats.getVariance();
        double maxVar = maxStats.getVariance();

        logger.info("min均值：{}, 方差：{}", minMean, minVar);
        logger.info("max均值：{}, 方差：{}", maxMean, maxVar);

        return (minMean+maxMean)/2;

    }


    /**
     * @Description 返回一元二次方程较小的根
     * @author Fang
     * @date 2019/3/26 17:07
     * @param A
     * @param B
     * @param C
     * @return double
     */
    private static double getSmallerRootForQuad(double A, double B, double C){
        return ((-B - Math.sqrt(B*B - 4*A*C))/(2*A));
    }


    private static double getRootForLinear(double A, double B){
        return (-B)/A;
    }
}
