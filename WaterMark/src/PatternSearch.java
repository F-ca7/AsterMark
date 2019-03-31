package src;

import java.util.ArrayList;

public class PatternSearch {
	private double STEP_LENGTH = 1;
	private double PRECISION = 0.01;
	private double DECAY_RATE = 0.1;
	private double ACCURATE = 2;
	private ArrayList<Float> initState;
	private double ALPHA = 8;
	private double REF;
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
		ArrayList<Float>tmp = new ArrayList<Float>(initState);
		int len = tmp.size();
		for(int i=0;i<len;i++) {
			tmp.set(i, element)
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
