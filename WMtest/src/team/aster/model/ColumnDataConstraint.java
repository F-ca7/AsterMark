package team.aster.model;

/**
 * @ClassName ColumnDataConstraint
 * @Description 描述对一列数据的约束
 * @author Fcat
 * @date 2019/4/9 21:33
 */
public class ColumnDataConstraint {
    private ConstraintType constraintType;  //该数据类型

    public ConstraintType getConstraintType() {
        return constraintType;
    }

    public double getVarLowerBound() {
        return varLowerBound;
    }

    public double getVarUpperBound() {
        return varUpperBound;
    }

    public int getPrecision() {
        return precision;
    }

    private double varLowerBound;   //变化范围下界
    private double varUpperBound;   //变化范围上界
    private int precision;      //精度约束

    public ColumnDataConstraint(ConstraintType constraintType, double varLowerBound, double varUpperBound, int precision) {
        this.constraintType = constraintType;
        this.varLowerBound = varLowerBound;
        this.varUpperBound = varUpperBound;
        precision = precision<0? 0:precision;   //精度只能大于等于0
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

