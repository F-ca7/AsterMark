package team.aster.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName PartitionedDataset
 * @Description 划分好的的数据集，用于添加水印
 * @author Fcat
 * @date 2019/3/24 9:07
 */
public class PartitionedDataset {

    Map<Integer, ArrayList<ArrayList<String>>> partitionedDataset;

    public PartitionedDataset() {
        partitionedDataset = new HashMap<>();
    }

    public void addToPartition(Integer index, ArrayList<ArrayList<String>> dataRow) {
        partitionedDataset.put(index, dataRow);
    }

    public boolean containsIndex(Integer index){
        return partitionedDataset.containsKey(index);
    }

    public ArrayList<ArrayList<String>> getPartitionByIndex(Integer index){
        return partitionedDataset.get(index);
    }

    public Map<Integer, ArrayList<ArrayList<String>>> getPartitionedDataset() {
        return partitionedDataset;
    }

}


