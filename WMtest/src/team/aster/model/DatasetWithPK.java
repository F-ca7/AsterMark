package team.aster.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName DatasetWithPK
 * @Description 带有主键的数据集，用于划分
 * @author Fcat
 * @date 2019/3/24 9:08
 */
public class DatasetWithPK {
    private Map<String, ArrayList<String>> dataset;

    public DatasetWithPK() {
        dataset = new HashMap<>();
    }

    public void addRecord(String pk, ArrayList<String> record) {
//        if (dataset.containsKey(pk)){
//            System.out.println("已存在主键！"+pk);
//        }
        dataset.put(pk, record);
    }

    public Map<String, ArrayList<String>> getDataset() {
        return dataset;
    }
}


