package team.aster.algorithm;

import java.util.ArrayList;
import java.util.Collections;
/**
 *
 * @author kun
 *
 */
public final class PatternSearch extends OptimizationAlgorithm{

    private double STEP_LENGTH;//起始搜索步长
    private double PRECISION = 0.001;//搜索终止时的步长精度
    private double DECAY_RATE = 0.95;//衰减速率
    private double ACCURATE = 0;//加速速度
    private int TURN_NUM = 100;//搜索回合数
    private double UPPER_BOUND;//变化上界
    private double LOWER_BOUND;//变化上界
    private ArrayList<Double> initState;//初始状态
    private ArrayList<Double> recordState;//中间状态
    private ArrayList<Double> changeRecord;//增向量
    private double ALPHA = 8;//sigmoid函数参数
    private double REF;//临时sigmoid函数参数
    static double OREF;//sigmoid隐藏函数参数
    private boolean IS_MAX;
    private double exp = -0.00001;

    private void setBound(double lower,double upper) {
        UPPER_BOUND = upper;
        LOWER_BOUND = lower;
        double larger = Math.abs(lower)>Math.abs(upper)?Math.abs(lower):Math.abs(upper);
        STEP_LENGTH = larger/2;
    }
    private void setRef(double ref) {
        REF = ref;
        OREF = ref;
    }
    private double getSigmoid(double x) {
        return (1.0-1.0/(1+Math.exp(ALPHA*(x-REF))));
    }
    private void setNextStepLength() {
        STEP_LENGTH = STEP_LENGTH * DECAY_RATE;
    }
    private boolean cmp(double x,double y) {
        if(IS_MAX) {
            return (x-y)>exp;
        }else {
            return (x-y)<exp;
        }
    }


    private double getOptimizedResult(){
        double sum=0;
        for(double i:recordState){
            sum+=getSigmoid(i);
        }
        return sum/recordState.size();
    }

    /**
     * 逐步搜索
     */
    private void stepByStep() {
        ArrayList<Double>tmp = new ArrayList<Double>(recordState);
        int len = tmp.size();
        for(int i=0;i<len;i++) {
            double valueI = tmp.get(i);
            if(cmp(getSigmoid(valueI+STEP_LENGTH),getSigmoid(valueI))&&changeRecord.get(i)<UPPER_BOUND) {
                tmp.set(i, valueI+STEP_LENGTH);
                changeRecord.set(i, changeRecord.get(i)+STEP_LENGTH);
            }else if(cmp(getSigmoid(valueI-STEP_LENGTH),getSigmoid(valueI))&&changeRecord.get(i)>LOWER_BOUND) {
                tmp.set(i, valueI-STEP_LENGTH);
                changeRecord.set(i, changeRecord.get(i)-STEP_LENGTH);
            }
        }
        recordState = tmp;
        setNextStepLength();
    }

    /**
     * 每回合搜索TURN_NUM次数
     */
    private void searchByAxis() {
        for(int i=0;i<TURN_NUM;i++) {
            stepByStep();
        }
    }

    /**
     * 加速搜索
     */
    private void searchByPattern() {
        ArrayList<Double>tmp = new ArrayList<Double>(recordState);
        ArrayList<Double>tmpChange = new ArrayList<Double>(changeRecord);
        int len = tmp.size();boolean ok=true;
        double x=0,y=0;
        for(int i=0;i<len;i++) {
            if(changeRecord.get(i)>UPPER_BOUND||changeRecord.get(i)<LOWER_BOUND){
                ok=false;
                break;
            }
            tmp.set(i, recordState.get(i)+ACCURATE*(recordState.get(i)-initState.get(i)));
            x+=getSigmoid(tmp.get(i));
            y+=getSigmoid(recordState.get(i));
            tmpChange.set(i, tmpChange.get(i)+ACCURATE*(recordState.get(i)-initState.get(i)));
        }

        if(cmp(x,y)&&ok) {
            changeRecord = tmpChange;
            initState = recordState = tmp;
        }else {
            initState = recordState;
        }
    }

    /**
     * 模式搜索
     * 分为按向量搜索
     * 加速搜索
     */
    private void search() {
        while((STEP_LENGTH-PRECISION)>exp) {
            searchByAxis();
            searchByPattern();
        }
    }

    public ArrayList<Double> getResult(){
        return this.initState;
    }




    public PatternSearch() {

    }

    /**
     * 获取最大最优化后的隐藏函数均值meanMax
     * @param colValues
     * @param ref
     * @param lower
     * @param upper
     * @return
     */
    @Override
    public double maximizeByHidingFunction(ArrayList<Double> colValues, double ref, double lower, double upper) {
        return initParams(colValues, ref, lower, upper, true);
    }

    private double initParams(ArrayList<Double> colValues, double ref, double lower, double upper, boolean b) {
        initState = recordState = colValues;
        changeRecord = new ArrayList<>(Collections.nCopies(colValues.size(), 0.0));
        this.IS_MAX = b;
        setRef(ref);
        setBound(lower,upper);
        search();
        //System.out.println(recordState);
        return getOptimizedResult();
    }

    /**
     * 获取最小最优化后的隐藏函数均值meanMin
     * @param colValues
     * @param ref
     * @param lower
     * @param upper
     * @return
     */
    @Override
    public double minimizeByHidingFunction(ArrayList<Double> colValues, double ref, double lower, double upper) {
        return initParams(colValues, ref, lower, upper, false);
    }

    @Override
    public ArrayList<Double> getModifiedColumn() {
        return initState;
    }

}
