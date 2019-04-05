package team.aster.algorithm;

import java.util.ArrayList;
import java.util.Collections;
/**
 *
 * @author kun
 *
 */
public final class PatternSearch extends OptimizationAlgorithm{
    private double STEP_LENGTH = 60;
    private double PRECISION = 0.01;
    private double DECAY_RATE = 0.9;
    private double ACCURATE = 2;
    private int TURN_NUM = 500;
    private double UPPER_BOUND;
    private double LOWER_BOUND;
    private ArrayList<Double> initState;
    private ArrayList<Double> recordState;
    private ArrayList<Double> changeRecord;
    private double ALPHA = 8;
    private double REF;
    private boolean IS_MAX;
    private double exp = -0.00001;
    private void setBound(double lower,double upper) {
        UPPER_BOUND=upper;
        LOWER_BOUND=lower;
    }
    private void setRef(double ref) {
        REF = ref;
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
    private void stepByStep() {
        double meanBuckets = 0.0;
        ArrayList<Double>tmp = new ArrayList<Double>(recordState);
        int len = tmp.size();
        boolean addOp=true,delOp=true;
        for(int i=0;i<len;i++) {
            double valueI = tmp.get(i);
            if(len-i<=Math.abs(meanBuckets)/STEP_LENGTH) {
                if(meanBuckets>0) addOp=false;
                else delOp=false;
            }
            if(cmp(getSigmoid(valueI+STEP_LENGTH),getSigmoid(valueI))&&addOp&&changeRecord.get(i)<=UPPER_BOUND) {
                tmp.set(i, valueI+STEP_LENGTH);
                changeRecord.set(i, changeRecord.get(i)+STEP_LENGTH);
                meanBuckets+=STEP_LENGTH;
            }else if(cmp(getSigmoid(valueI-STEP_LENGTH),getSigmoid(valueI))&&delOp&&changeRecord.get(i)>=LOWER_BOUND) {
                tmp.set(i, valueI-STEP_LENGTH);
                changeRecord.set(i, changeRecord.get(i)+STEP_LENGTH);
                meanBuckets-=STEP_LENGTH;
            }
        }
        recordState = tmp;
        setNextStepLength();
    }
    private void searchByAxis() {
        for(int i=0;i<TURN_NUM;i++) {
            stepByStep();
        }
    }
    private void searchByPattern() {
        double meanBuckets = 0;
        ArrayList<Double>tmp = new ArrayList<Double>(recordState);
        ArrayList<Double>tmpChange = new ArrayList<Double>(changeRecord);
        int len = tmp.size();
        double x=0,y=0;
        for(int i=0;i<len;i++) {
            meanBuckets += ACCURATE*(recordState.get(i)-initState.get(i));
            tmp.set(i, recordState.get(i)+ACCURATE*(recordState.get(i)-initState.get(i)));
            x+=getSigmoid(tmp.get(i));
            y+=getSigmoid(recordState.get(i));
            tmpChange.set(i, tmpChange.get(i)+ACCURATE*(recordState.get(i)-initState.get(i)));
        }

        if(cmp(x,y)&&(meanBuckets-PRECISION<exp)) {
            changeRecord = tmpChange;
            initState = recordState = tmp;
        }else {
            initState = recordState;
        }
    }
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
        System.out.println(recordState);
        return getOptimizedResult();
    }

    @Override
    public double minimizeByHidingFunction(ArrayList<Double> colValues, double ref, double lower, double upper) {
        return initParams(colValues, ref, lower, upper, false);
    }

    @Override
    public ArrayList<Double> getModifiedColumn() {
        return initState;
    }

//    public static void main(String[] args) {
//
//        ArrayList<Double> test = new ArrayList<Double>();
//        test.add(1000.0);
//        test.add(1200.0);
//        test.add(1700.0);
//        test.add(1800.0);
//        PatternSearch p = new PatternSearch(test, 1464.6, -300, 300, true);
//        ArrayList<Double> list = p.getResult();
//        double sum=0;
//        for(double i:list) {
//            sum+=i;
//        }
//        System.out.println(list);
//        System.out.println(sum/4);
//    }

}
