package team.aster.algorithm;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

public abstract class OptimizationAlgorithm {

    public abstract double maximizeByHidingFunction(ArrayList<Double> colValues, double ref, double lower, double upper);
    public abstract double minimizeByHidingFunction(ArrayList<Double> colValues, double ref, double lower, double upper);
    public abstract ArrayList<Double> getModifiedColumn();


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
        double varianceSqrt = Math.sqrt(stats.getVariance());
        double ref = mean + secretKey*varianceSqrt;
        double sum = 0;
        for (double i:colValues){
            sum += getSigmoid(i, ref);
        }
        return sum/(double)colValues.size();
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
     * @Description 计算最优化的阈值T,为了防止精度丢失导致的巨大误差，此处，阈值计算约为meanMax与meanMin的均值
     * @author Fcat
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

        System.out.printf("min均值：%f, 方差：%f%n", minMean, minVar);
        System.out.printf("max均值：%f, 方差：%f%n", maxMean, maxVar);

        return (minMean+maxMean)/2;

    }


    /**
     * @Description 返回一元二次方程较小的根
     * @author Fcat
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
