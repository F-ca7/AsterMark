package team.aster.processor;

import team.aster.algorithm.GenericOptimization;
import team.aster.algorithm.OptimizationAlgorithm;
import team.aster.algorithm.PatternSearch;
import team.aster.database.SecretKeyDbController;
import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;
import team.aster.model.StoredKey;
import team.aster.model.WaterMark;
import team.aster.utils.Constants;

import java.util.ArrayList;
import java.util.Map;

public class OptimEncoder implements IEncoder {
    private ArrayList<Double> minList = new ArrayList<>();
    private ArrayList<Double> maxList = new ArrayList<>();

    //先只对一列进行嵌入水印，这里是最后一列FLATLOSE 转让盈亏(已扣税)
    //但是这里还是不太科学
    private static final int COL_INDEX = Constants.EmbedDbInfo.EMBED_COL_INDEX-1;
    private static final int MIN_PART_LENGTH = 10;
    private static final double SECRET_KEY = 0.1;
    private final int PARTITION_COUNT = Constants.EmbedDbInfo.PARTITION_COUNT;
    private double threshold;



    public ArrayList<Double> getMinList() {
        return minList;
    }

    public ArrayList<Double> getMaxList() {
        return maxList;
    }

    public double getThreshold() {
        return threshold;
    }

    @Override
    public void encode(DatasetWithPK datasetWithPK, ArrayList<String> watermarkList) {
        System.out.println(this.toString()+"开始工作");

        String secreteCode = SecretCodeGenerator.getSecreteCode(10);
        //对datasetWithPK进行划分
        PartitionedDataset partitionedDataset = Divider.divide(PARTITION_COUNT, datasetWithPK, secreteCode);

        System.out.printf("预期划分数为%d，实际划分数为%d%n", PARTITION_COUNT, partitionedDataset.getPartitionedDataset().keySet().size());


        //生成水印
        WaterMark waterMark = WaterMarkGenerator.getWaterMark(watermarkList);

        assert waterMark != null;
        encodeAllBits(partitionedDataset, waterMark.getBinary());

        //打印maxList和minList
        System.out.println("maxList:");
        System.out.println(maxList);
        System.out.println("minList:");
        System.out.println(minList);

        //保存水印信息
        //todo 是否不应该交给它来保存秘钥信息
        //TODO 此处逻辑有问题他，dbtable和target不应在这里
        StoredKey storedKey = new StoredKey.Builder()
                .setDbTable("wm_exp::transaction_2013").setMinLength(MIN_PART_LENGTH)
                .setSecretKey(SECRET_KEY).setThreshold(threshold)
                .setTarget("Tencent").setPartitionCount(PARTITION_COUNT)
                .setWaterMark(waterMark).setWmLength(waterMark.getLength())
                .setSecretCode(secreteCode)
                .build();
        SecretKeyDbController.getInstance().saveStoredKeysToDB(storedKey);

        //更新数据
        Map<String, ArrayList<String>> ds = datasetWithPK.getDataset();
        //先清空原来的datasetWithPK
        ds.clear();
        //把partitionedDataset更新回datasetWithPK，主键为每行的第一列(id)
        for(ArrayList<ArrayList<String>> rowSet: partitionedDataset.getPartitionedDataset().values()){
            for (ArrayList<String> row : rowSet){
                ds.put(row.get(0), row);
            }
        }
    }



    @Override
    public String toString() {
        return "Optimization based Encoder";
    }

    /**
     * @Description 对划分好的数据集嵌入水印，直接修改划分里的数据集
     * @author Fcat
     * @date 2019/3/24 16:47
     * @param partitionedDataset    整个划分好的数据集
     * @param watermark	    要水印串
     */
    private void encodeAllBits(PartitionedDataset partitionedDataset, ArrayList<Integer> watermark){
        System.out.println("开始嵌入水印所有位");
        Map<Integer, ArrayList<ArrayList<String>>> datasetWithIndex = partitionedDataset.getPartitionedDataset();
        int wmLength = watermark.size();
        datasetWithIndex.forEach((k,v)->{
            int index = k%wmLength;
            System.out.printf("正在处理第%d个划分...\n嵌入水印位为第%d位\n", k, index);
            encodeSingleBit(v, index, watermark.get(index));
        });
        //保存阈值T
        threshold = GenericOptimization.calcOptimizedThreshold(minList, maxList);
        System.out.println("阈值为：" + threshold);
    }



    /**
     * @Description 对水印的一个bit嵌入一个划分当中，直接对划分进行修改
     * @author Fcat
     * @date 2019/3/24 1:18
     * @param partition	 一个划分
     * @param bitIndex	水印对应的bit位
     * @return void
     */
    private void encodeSingleBit(ArrayList<ArrayList<String>> partition, int bitIndex, int bit){
        System.out.printf("正在对第%d个字段嵌入水印的第%d位: %d%n", COL_INDEX +1, bitIndex, bit);
        ArrayList<Double> colValues = new ArrayList<>();
        for(ArrayList<String> row: partition){
            double value = Double.valueOf(row.get(COL_INDEX));
            //System.out.printf("字段值为%f\n", value);
            colValues.add(value);
        }
        OptimizationAlgorithm optimization = new PatternSearch();
        double tmp;
        switch (bit){
            case 0:
                tmp = optimization.minimizeByHidingFunction(colValues,
                        OptimizationAlgorithm.getHidingValue(colValues, Constants.StoredKeyDbInfo.SECRET_KEY), -300,300);
                minList.add(tmp);
                break;
            case 1:
                tmp = optimization.maximizeByHidingFunction(colValues,
                        OptimizationAlgorithm.getHidingValue(colValues, Constants.StoredKeyDbInfo.SECRET_KEY), -300,300);
                maxList.add(tmp);
                break;
            default:
                System.out.println("水印出错！");
                break;
        }

        ArrayList<Double> modifiedCol = optimization.getModifiedColumn();

        //写回partition
        int rowIndex = 0;
        String resultStr;
        for(ArrayList<String> row: partition){
            resultStr = String.format("%.2f", modifiedCol.get(rowIndex));
            System.out.println("将原来的"+row.get(COL_INDEX)+"改为"+resultStr);
            row.set(COL_INDEX, resultStr);
            rowIndex++;
        }

    }


}

