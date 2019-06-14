package team.aster.model;

public class EmbeddingInfo {
    private String embeddingColumnName;
    private String keyColumnName;
    private String dataType;
    private String embeddingMethod;
    private String precision;
    private double upperBound;
    private double lowerBound;
    private String embeddingMessage;

    @Override
    public String toString() {
        return "EmbeddingInfo{" +
                "embeddingColumnName='" + embeddingColumnName + '\'' +
                ", keyColumnName='" + keyColumnName + '\'' +
                ", dataType='" + dataType + '\'' +
                ", embeddingMethod='" + embeddingMethod + '\'' +
                ", precision=" + precision +
                ", upperBound=" + upperBound +
                ", lowerBound=" + lowerBound +
                ", embeddingMessage='" + embeddingMessage + '\'' +
                '}';
    }

    public EmbeddingInfo(String embeddingColumnName, String keyColumnName, String dataType, String embeddingMethod, String precision, double upperBound, double lowerBound, String embeddingMessage) {
        this.embeddingColumnName = embeddingColumnName;
        this.keyColumnName = keyColumnName;
        this.dataType = dataType;
        this.embeddingMethod = embeddingMethod;
        this.precision = precision;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        this.embeddingMessage = embeddingMessage;
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

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public String getEmbeddingMessage() {
        return embeddingMessage;
    }

    public void setEmbeddingMessage(String embeddingMessage) {
        this.embeddingMessage = embeddingMessage;
    }
}
