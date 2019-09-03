package team.aster.processor;

import team.aster.model.ColumnDataConstraint;
import team.aster.model.DatasetWithPK;
import team.aster.model.StoredKey;

import java.util.ArrayList;

public interface IEncoder {

    /**
     * @Description 对带主键的数据集嵌入水印
     * @author Fcat
     * @date 2019/4/9 21:04
     * @param datasetWithPK	带主键的数据集
     * @param watermarkList	已存在的水印列表，防止相似度高而碰撞
     */
    void encode(DatasetWithPK datasetWithPK, ArrayList<String> watermarkList);

    void setStoredKeyBuilder(StoredKey.Builder storedKeyBuilder);

    void setDataConstraint(ColumnDataConstraint dataConstraint);
}
