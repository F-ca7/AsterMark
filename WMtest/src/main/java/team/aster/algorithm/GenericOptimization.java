package team.aster.algorithm;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

import java.util.ArrayList;

public final class GenericOptimization extends OptimizationAlgorithm{




    //todo 临时模拟
    public double maximizeByHidingFunction(ArrayList<Double> colValues, double ref, double lower, double upper){
        RandomData randomData = new RandomDataImpl();

        return randomData.nextGaussian(0.5,0.01);
    }

    //todo 临时模拟
    public double minimizeByHidingFunction(ArrayList<Double> colValues, double ref, double lower, double upper){
        RandomData randomData = new RandomDataImpl();

        return randomData.nextGaussian(0.2,0.02);
    }

    @Override
    public ArrayList<Double> getModifiedColumn() {
        return null;
    }


}
