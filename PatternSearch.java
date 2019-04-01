package src;

import java.util.ArrayList;

public class PatternSearch {
	private double STEP_LENGTH = 1;
	private double PRECISION = 0.01;
	private double DECAY_RATE = 0.1;
	private double ACCURATE = 2;
	private int TURN_NUM = 30;
	private ArrayList<Double> initState;
	private ArrayList<Double> recordState;
	private double ALPHA = 8;
	private double REF;
	private double exp = -0.00001;
	private void setRef(double mean,double var,double c) {
		REF = mean+c*var;
	}
	private double getSigmoid(double x) {
		return (1.0-1.0/(1+Math.exp(ALPHA*(x-REF))));
	}
	private void setNextStepLength() {
		STEP_LENGTH = STEP_LENGTH - DECAY_RATE;
	}
	private void stepByStep() {
		double meanBuckets = 0;
		ArrayList<Double>tmp = new ArrayList<Double>(recordState);
		int len = tmp.size();
		boolean addOp=true,delOp=true;
		for(int i=0;i<len;i++) {
			double valueI = tmp.get(i);
			if(len-i<Math.abs(meanBuckets)/STEP_LENGTH) {
				if(meanBuckets>0) addOp=false;
				else delOp=false;
			}
			if(((getSigmoid(valueI+STEP_LENGTH)-getSigmoid(valueI))<exp)&&addOp) {
				tmp.set(i, valueI+STEP_LENGTH);
				meanBuckets+=STEP_LENGTH;
			}else if((getSigmoid(valueI-STEP_LENGTH)-getSigmoid(valueI))<exp&&delOp) {
				tmp.set(i, valueI-STEP_LENGTH);
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
		ArrayList<Double>tmp = new ArrayList<Double>(recordState);
		int len = tmp.size();
		double x=0,y=0;
		for(int i=0;i<len;i++) {
			tmp.set(i, recordState.get(i)+ACCURATE*(recordState.get(i)-initState.get(i)));
			x+=getSigmoid(tmp.get(i));
			y+=getSigmoid(recordState.get(i));
		}
		if(x<y) {
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
	public PatternSearch(ArrayList<Double>data,double mean,double var,double c) {
		initState=recordState=data;
		setRef(mean, var, c);
		search();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
