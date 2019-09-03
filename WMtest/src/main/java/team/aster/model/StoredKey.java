package team.aster.model;

/**
 * @ClassName StoredKey
 * @Description 要保存在数据库中，
 *              包括用于水印、划分数、水印长度、阈值、
 *              秘钥、秘参、划分的最小基数
 *              数据库数据表名、
 * @author Fcat
 * @date 2019/3/30 11:00
 */
public class StoredKey {
    //使用Builder模式进行构造

    private WaterMark waterMark;
    private int partitionCount;
    private int wmLength;
    private double threshold;
    private double secretKey;
    private String secretCode;
    private int minLength;
    private String dbTable;
    //共享数据对象方
    private String target;

    @Override
    public String toString() {
        return "StoredKey{" +
                "waterMark=" + waterMark +
                ", partitionCount=" + partitionCount +
                ", wmLength=" + wmLength +
                ", threshold=" + threshold +
                ", secretKey=" + secretKey +
                ", secretCode='" + secretCode + '\'' +
                ", minLength=" + minLength +
                ", dbTable='" + dbTable + '\'' +
                ", target='" + target + '\'' +
                '}';
    }

    private StoredKey(Builder builder){
        this.waterMark = builder.waterMark;
        this.partitionCount = builder.partitionCount;
        this.wmLength = builder.wmLength;
        this.threshold = builder.threshold;
        this.secretKey = builder.secretKey;
        this.secretCode = builder.secretCode;
        this.minLength = builder.minLength;
        this.dbTable = builder.dbTable;
        this.target = builder.target;
    }


    public static class Builder{
        private WaterMark waterMark;
        private int partitionCount;
        private int wmLength;

        public WaterMark getWaterMark() {
            return waterMark;
        }

        public int getPartitionCount() {
            return partitionCount;
        }

        public int getWmLength() {
            return wmLength;
        }

        public double getThreshold() {
            return threshold;
        }

        public double getSecretKey() {
            return secretKey;
        }

        public String getSecretCode() {
            return secretCode;
        }

        public int getMinLength() {
            return minLength;
        }

        public String getDbTable() {
            return dbTable;
        }

        public String getTarget() {
            return target;
        }

        private double threshold;
        private double secretKey;
        private String secretCode;
        private int minLength;
        private String dbTable;
        private String target;

        public Builder setTarget(String target) {
            this.target = target;
            return this;
        }

        public Builder setWaterMark(WaterMark waterMark) {
            this.waterMark = waterMark;
            return this;
        }

        public Builder setPartitionCount(int partitionCount) {
            this.partitionCount = partitionCount;
            return this;
        }

        public Builder setWmLength(int wmLength) {
            this.wmLength = wmLength;
            return this;
        }

        public Builder setThreshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder setSecretKey(double secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder setSecretCode(String secretCode) {
            this.secretCode = secretCode;
            return this;
        }

        public Builder setMinLength(int minLength) {
            this.minLength = minLength;
            return this;
        }

        public Builder setDbTable(String dbTable) {
            this.dbTable = dbTable;
            return this;
        }

        public StoredKey build(){
            return new StoredKey(this);
        }
    }





    public WaterMark getWaterMark() {
        return waterMark;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public int getWmLength() {
        return wmLength;
    }

    public double getThreshold() {
        return threshold;
    }

    public double getSecretKey() {
        return secretKey;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public int getMinLength() {
        return minLength;
    }

    public String getDbTable() {
        return dbTable;
    }

    public String getTarget() {
        return target;
    }
}
