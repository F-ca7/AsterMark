package team.aster.processor;

public abstract class IEncoderImpl implements IEncoder {
    String target;
    String dbTable;

    public void setTarget(String target) {
        this.target = target;
    }

    public void setDbTable(String dbTable) {
        this.dbTable = dbTable;
    }
}
