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
        System.out.println("均值为"+mean);
        System.out.println("标准差为"+varianceSqrt);
        return mean + secretKey*varianceSqrt;
    }

    /**
     * @Description 计算最优化的阈值T
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

        double minSize = (double) minList.size();
        double maxSize = (double) maxList.size();

        //System.out.printf("min均值：%f, 方差：%f%n", minMean, minVar);
        //System.out.printf("max均值：%f, 方差：%f%n", maxMean, maxVar);
        //bit为0的概率
        double p0 = minSize/(minSize+maxSize);
        //bit为0的概率
        double p1 = maxSize/(minSize+maxSize);

        //下面的A, B, C都是二次方程的系数，已经先对方程做了化简
//        double A = (minVar - maxVar)/2;
//        double B = minMean*maxVar - maxMean*minVar;
//        double tmpC = (p0*Math.sqrt(maxVar))/(p1*Math.sqrt(minVar));
//        double C = minVar*maxVar*Math.log(tmpC) + (maxMean*maxMean*minVar - minMean*minMean*maxVar)/2;

        //判断二次项系数是否为0
//        if (Math.abs(minVar - maxVar)<0.01){
//            return (minMean+maxMean)/2;
//        }else {
//            return getSmallerRootForQuad(A, B, C);
//        }
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
