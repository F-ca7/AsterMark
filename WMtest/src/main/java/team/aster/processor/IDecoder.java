package team.aster.processor;

import team.aster.model.DatasetWithPK;
import team.aster.model.StoredKey;

public interface IDecoder {
    /**
     * 解码调用接口
     * @param datasetWithPK
     * @return 解码得到的水印
     */
    String decode(DatasetWithPK datasetWithPK);

    /**
     * 设置秘参接口
     * @param storedKey
     */
    void setStoredKeyParams(StoredKey storedKey);

    void setEmbedColIndex(int embedColIndex);
}
