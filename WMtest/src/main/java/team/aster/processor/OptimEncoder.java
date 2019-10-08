package team.aster.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.aster.algorithm.Divider;
import team.aster.algorithm.OptimizationAlgorithm;
import team.aster.algorithm.PatternSearch;
import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;
import team.aster.model.WaterMark;
import team.aster.model.WatermarkException;

import java.util.ArrayList;
import java.util.Map;

/**
 * 基于最优化算法的水印编码器
 */
public class OptimEncoder extends IEncoderNumericImpl {
    private static Logger logger = LoggerFactory.getLogger(OptimEncoder.class);

    private ArrayList<Double> minList = new ArrayList<>();
    private ArrayList<Double> maxList = new ArrayList<>();

    private int COL_INDEX;
    private int PK_INDEX;
    private int PARTITION_COUNT;


    public ArrayList<Double> getMinList() {
        return minList;
    }

    public ArrayList<Double> getMaxList() {
        return maxList;
    }



    @Override
    public void encode(DatasetWithPK datasetWithPK, ArrayList<String> watermarkList) throws WatermarkException {
        logger.debug("{} 开始工作", this.toString());
        // 根据元组数设置划分数
        PARTITION_COUNT = datasetWithPK.getDataset().size()/100;
        //根据数据库表名生成secretCode
        String secreteCode = SecretCodeGenerator.getSecretCode(storedKeyBuilder.getDbTable());
        logger.debug("secreteCode为 {}", secreteCode);
        //对datasetWithPK进行划分
        PartitionedDataset partitionedDataset = Divider.divide(PARTITION_COUNT, datasetWithPK, secreteCode);

        logger.debug("预期划分数为{}，实际划分数为{}", PARTITION_COUNT, partitionedDataset.getPartitionedDataset().keySet().size());

        //生成水印
        WaterMark waterMark = WaterMarkGenerator.getWaterMark(watermarkList);
        //嵌入水印所有位
        assert waterMark != null;
        double threshold = encodeAllBits(partitionedDataset, waterMark.getBinary(), storedKeyBuilder.getSecretKey());

        // 打印maxList和minList
        logger.debug("maxList: {}", maxList);
        logger.debug("minList: {}", minList);

        // 补充完秘钥信息
        completeStoredKey(PARTITION_COUNT,secreteCode, threshold, waterMark);



        //更新数据
        Map<String, ArrayList<String>> ds = datasetWithPK.getDataset();
        //先清空原来的datasetWithPK
        ds.clear();
        //把partitionedDataset更新回datasetWithPK，map的key为主键值
        for(ArrayList<ArrayList<String>> rowSet: partitionedDataset.getPartitionedDataset().values()){
            for (ArrayList<String> row : rowSet){
                ds.put(row.get(PK_INDEX), row);
            }
        }
    }

    @Override
    public void setPKIndex(int pkIndex) {
        PK_INDEX = pkIndex;
    }

    @Override
    public void setEmbedColIndex(int embedColIndex) {
        COL_INDEX = embedColIndex;
    }

    /**
     * @Description 对划分好的数据集嵌入水印，直接修改划分里的数据集
     * @author Fang
     * @date 2019/3/24 16:47
     * @param partitionedDataset    整个划分好的数据集
     * @param watermark	    要嵌入的水印串
     */
    private double encodeAllBits(PartitionedDataset partitionedDataset, ArrayList<Integer> watermark, double secretKey){
        logger.info("开始嵌入水印所有位");
        Map<Integer, ArrayList<ArrayList<String>>> datasetWithIndex = partitionedDataset.getPartitionedDataset();
        final int wmLength = watermark.size();


        ArrayList<Double> all = new ArrayList<>();
        datasetWithIndex.forEach((k,v)->{
            for(ArrayList<String> row: v){
                // 只取一列数据
                double value = Double.valueOf(row.get(COL_INDEX));
                all.add(value);
            }
        });
        all.sort(Double::compareTo);

        int start = ((int)(20+secretKey*10))*all.size()/100;
        int end = ((int)(80-secretKey*10))*all.size()/100;
        double mean = 0d;
        ArrayList<Double> cutCol = new ArrayList<>();
        for(int i=start;i<end;i++){
            cutCol.add(all.get(i));
            mean+=all.get(i)/(end-start);
        }
        PatternSearch.OREF = mean;


        datasetWithIndex.forEach((k,v)->{
            int index = k%wmLength;
            encodeSingleBit(v, secretKey, watermark.get(index));
        });
        double threshold = OptimizationAlgorithm.calcOptimizedThreshold(minList, maxList);
        logger.debug("阈值为: {}", threshold);
        return threshold;
    }



    /**
     * @Description 对水印的一个bit嵌入一个划分当中，直接对划分进行修改
     * @author Fang
     * @date 2019/3/24 1:18
     * @param partition	 一个划分
     * @param secretKey	水印对应的bit位
     * @return void
     */
    private void encodeSingleBit(ArrayList<ArrayList<String>> partition, double secretKey, int bit){
        //System.out.printf("正在对第%d个字段嵌入水印的第%d位: %d%n", COL_INDEX +1, bitIndex, bit);
        ArrayList<Double> colValues = new ArrayList<>();


        for(ArrayList<String> row: partition){
            // 只取一列数据
            double value = Double.valueOf(row.get(COL_INDEX));
            colValues.add(value);
        }
        OptimizationAlgorithm optimization = new PatternSearch();
        double tmp;

        double varLowerBound = dataConstraint.getVarLowerBound();
        double varUpperBound = dataConstraint.getVarUpperBound();
        // 该bit是0则将hiding函数最小化，是1则最大化
        switch (bit){
            case 0:
                tmp = optimization.minimizeByHidingFunction(colValues,
                        OptimizationAlgorithm.getRefValue(colValues, secretKey), varLowerBound, varUpperBound);
                minList.add(tmp);
                break;
            case 1:
                tmp = optimization.maximizeByHidingFunction(colValues,
                        OptimizationAlgorithm.getRefValue(colValues, secretKey), varLowerBound, varUpperBound);
                maxList.add(tmp);
                break;
            default:
                logger.error("水印出错! ");
                break;
        }

        ArrayList<Double> modifiedCol = optimization.getModifiedColumn();

        // 精度约束
        String precision = dataConstraint.getPrecision();
        int len;
        if (precision.startsWith("0")){
            // 小数点后几位
            len = precision.length()-2;
        } else {
            len = 0;
        }
        // 格式化输出的占位符设置
        String placeholder = "%.0f";
        switch (dataConstraint.getConstraintType()){
            case INTEGER:
                break;
            case DOUBLE:
                placeholder = "%."+len+"f";
                break;
            default:
                break;
        }

        // 写回partition
        int rowIndex = 0;
        String resultStr;
        for(ArrayList<String> row: partition){
            resultStr = String.format(placeholder, modifiedCol.get(rowIndex));

            //System.out.println("将原来的"+row.get(COL_INDEX)+"改为"+resultStr);
            row.set(COL_INDEX, resultStr);
            rowIndex++;
        }
    }



    @Override
    public String toString() {
        return "Optimization based Encoder";
    }


}

