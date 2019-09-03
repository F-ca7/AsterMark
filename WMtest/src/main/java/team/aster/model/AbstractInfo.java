package team.aster.model;

public class AbstractInfo {
    private String sourceDataTable;
    private String embeddingColumnName;
    private String keyColumnName;
    private String dataType;
    private String embeddingMethod;

    public AbstractInfo(String sourceDataTable,String embeddingColumnName,String keyColumnName,String dataType,String embeddingMethod){
        this.sourceDataTable=sourceDataTable;
        this.embeddingColumnName=embeddingColumnName;
        this.keyColumnName=keyColumnName;
        this.dataType=dataType;
        this.embeddingMethod=embeddingMethod;
    }

    public String getSourceDataTable() {
        return sourceDataTable;
    }

    public void setSourceDataTable(String sourceDataTable) {
        this.sourceDataTable = sourceDataTable;
    }

    public String getEmbeddingColumnName() {
        return embeddingColumnName;
    }

    public void setEmbeddingColumnName(String embeddingColumnName) {
        this.embeddingColumnName = embeddingColumnName;
    }

    public String getKeyColumnName() {
        return keyColumnName;
    }

    public void setKeyColumnName(String keyColumnName) {
        this.keyColumnName = keyColumnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getEmbeddingMethod() {
        return embeddingMethod;
    }

    public void setEmbeddingMethod(String embeddingMethod) {
        this.embeddingMethod = embeddingMethod;
    }
}
