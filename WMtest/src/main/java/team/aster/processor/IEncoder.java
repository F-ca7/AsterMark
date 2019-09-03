package team.aster.processor;

import team.aster.model.ColumnDataConstraint;
import team.aster.model.DatasetWithPK;
import team.aster.model.StoredKey;
import team.aster.model.WatermarkException;

import java.util.ArrayList;

public interface IEncoder {

    /**
     * @Description 对带主键的数据集嵌入水印
     * @author Fcat
     * @date 2019/4/9 21:04
     * @param datasetWithPK	带主键的数据集
     * @param watermarkList	已存在的水印列表，防止相似度高而碰撞
     */
    void encode(DatasetWithPK datasetWithPK, ArrayList<String> watermarkList) throws WatermarkException;

    // 获取嵌入后得到的密参信息
    // 用于存回数据库
    StoredKey getStoredKey();

    void setPKIndex(int pkIndex);
    void setEmbedColIndex(int embedColIndex);

    void setStoredKeyBuilder(StoredKey.Builder storedKeyBuilder);
    void setDataConstraint(ColumnDataConstraint dataConstraint);
}
