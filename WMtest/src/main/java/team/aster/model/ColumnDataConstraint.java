package team.aster.model;

/**
 * @Description 描述对一列数据的约束
 * @author Fcat
 * @date 2019/4/9 21:33
 */
public class ColumnDataConstraint {
    private ConstraintType constraintType;  //该数据类型

    private double varLowerBound;   //变化范围下界
    private double varUpperBound;   //变化范围上界
    private String precision;      //精度约束

    public double getVarLowerBound() {
        return varLowerBound;
    }

    public double getVarUpperBound() {
        return varUpperBound;
    }

    public String getPrecision() {
        return precision;
    }

    public ConstraintType getConstraintType() {
        return constraintType;
    }


    public ColumnDataConstraint(ConstraintType constraintType, double varLowerBound, double varUpperBound, String precision) {
        this.constraintType = constraintType;
        this.varLowerBound = varLowerBound;
        this.varUpperBound = varUpperBound;
//        precision = precision<0? 0:precision;   //精度只能大于等于0
        this.precision = precision;
    }

    @Override
    public String toString() {
        return "ColumnDataConstraint{" +
                "constraintType=" + constraintType +
                ", varLowerBound=" + varLowerBound +
                ", varUpperBound=" + varUpperBound +
                ", precision=" + precision +
                '}';
    }
}

