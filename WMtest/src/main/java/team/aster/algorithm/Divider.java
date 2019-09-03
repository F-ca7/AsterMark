package team.aster.algorithm;

import team.aster.model.DatasetWithPK;
import team.aster.model.PartitionedDataset;

import java.util.ArrayList;

/****
 *
 * @author kun
 * 对数据集进行划分
 */
public class Divider {
    private static int M = 65535;//字符串Hashcode分布范围

    /****
     * 返回数字绝对值
     * @param num
     * @return
     */
    private static int abs(int num) {
        return num>=0?num:-num;
    }

    /****
     * BKDRHash算法，对字符数组进行散列，结果取绝对值
     * @param s 字符数组
     * @return 字符数组对应的Hashcode，非负
     */
    private static int BKDRHash(char[] s)
    {
        int seed=131 ;// 31 131 1313 13131 131313 etc..
        int hash=0;
        int len = s.length;
        for(int i=0;i<len;i++) {
            hash=hash*seed+s[i];
        }
        return abs(hash % M);
    }

    /****
     * 根据公式H(Ks||H(p.r||Ks))，计算获取Mac值
     * @param primaryKey 主键
     * @param keyCode 密钥
     * @return Mac值
     */
    private static int getMac(String primaryKey, String keyCode) {
        char[] stageOne = (primaryKey+keyCode).toCharArray();
        String tmp = keyCode+String.valueOf(BKDRHash(stageOne));
        int result = BKDRHash(tmp.toCharArray());
        return result;
    }

    /****
     * 数据集划分
     * @param m 分组数
     * @param data 数据集，Map类型，key为数据主键，value为主键对应列数据（类型为ArrayList）
     * @param secretCode 密钥
     * @return 划分后的数据集map，Map类型，key为划分集合下标，value为ArrayList，包含该集合下所有列数据（类型为ArrayList）
     */
    public static PartitionedDataset divide(int m, DatasetWithPK data, String secretCode){
        PartitionedDataset partitionedDataset = new PartitionedDataset();
        for(String key:data.getDataset().keySet()) {
            int mac = getMac(key,secretCode);
            int index = mac%m;
            ArrayList<String> value = data.getDataset().get(key);
            ArrayList<ArrayList<String>> tmp;
            if(partitionedDataset.containsIndex(index)) {
                tmp = partitionedDataset.getPartitionByIndex(index);
                tmp.add(value);
            }else {
                tmp = new ArrayList<>();
                tmp.add(value);
                partitionedDataset.addToPartition(index, tmp);
            }
        }
        return partitionedDataset;
    }

}
