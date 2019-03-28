package team.aster.processor;

import team.aster.algorithm.GenericOptimization;
import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;

import java.util.ArrayList;
import java.util.Map;

public class OptimEncoder implements IEncoder {

    ArrayList<Double> minList = new ArrayList<>();
    ArrayList<Double> maxList = new ArrayList<>();
    //先只对一列进行嵌入水印，这里是最后一列FLATLOSE 转让盈亏(已扣税)
    //但是这里还是不太科学
    final int COLINDEX = 13;
    double threshold;

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
    public void encode(DatasetWithPK datasetWithPK) {
        int partitionCount = 10;
        System.out.println(this.toString()+"开始工作");

        String secretKey = SecretCodeGenerator.getSecreteCode(10);
        PartitionedDataset partitionedDataset = Divider.divide(partitionCount, datasetWithPK, secretKey);
        System.out.printf("预期划分数为%d，实际划分数为%d", partitionCount, partitionedDataset.getPartitionedDataset().keySet().size());
        //todo 水印生成器
        int[] watermark = {1,0,0,1,0,0,1,1};
        encodeAllBits(partitionedDataset, watermark);
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
     * @return void
     */
    private void encodeAllBits(PartitionedDataset partitionedDataset, int[] watermark){
        System.out.println("开始嵌入水印所有位");
        Map<Integer, ArrayList<ArrayList<String>>> datasetWithIndex = partitionedDataset.getPartitionedDataset();
        int wmLength = watermark.length;
        datasetWithIndex.forEach((k,v)->{
            int index = k%wmLength;
            System.out.printf("正在处理第%d个划分...\n嵌入水印位为第%d位\n", k, index);
            encodeSingleBit(v, index, watermark[index]);
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

        System.out.printf("正在对第%d个字段嵌入水印的第%d位: %d\n", COLINDEX+1, bitIndex, bit);
        ArrayList<Double> colValues = new ArrayList<>();
        for(ArrayList<String> row: partition){
            double value = Double.valueOf(row.get(COLINDEX));
            //System.out.printf("字段值为%f\n", value);
            colValues.add(value);
        }
        switch (bit){
            case 0:
                minList.add(GenericOptimization.minimizeByHidingFunction(colValues));
                break;
            case 1:
                maxList.add(GenericOptimization.maximizeByHidingFunction(colValues));
                break;
            default:
                System.out.println("水印出错！");
                break;
        }
    }


}

