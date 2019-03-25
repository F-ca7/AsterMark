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
    Map<String, ArrayList<String>> dataset;

    public DatasetWithPK() {
        dataset = new HashMap<>();
    }

    public void addRecord(String pk, ArrayList<String> record) {
        dataset.put(pk, record);
    }

    public Map<String, ArrayList<String>> getDataset() {
        return dataset;
    }
}


