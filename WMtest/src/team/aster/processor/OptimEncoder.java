package team.aster.processor;

import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;

import java.util.ArrayList;
import java.util.Map;

public class OptimEncoder implements IEncoder {
    @Override
    public void encode(DatasetWithPK datasetWithPK) {
        int partitionCount = 10;
        System.out.println(this.toString()+"开始工作");

        String secretKey = SecretCodeGenerator.getSecreteCode(10);
        PartitionedDataset partitionedDataset = Divider.divide(partitionCount, datasetWithPK, secretKey);
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
            encodeSingleBit(v, index);
        });
    }



    /**
     * @Description 对水印的一个bit嵌入一个划分当中，直接对划分进行修改
     * @author Fcat
     * @date 2019/3/24 1:18
     * @param partition	 一个划分
     * @param bitIndex	水印对应的bit位
     * @return void
     */
    private void encodeSingleBit(ArrayList<ArrayList<String>> partition, int bitIndex){

    }
}

