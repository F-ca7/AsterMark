package team.aster.model;

import java.io.*;
import java.util.*;

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

    /**
     * @Description 导出数据集到csv文件
     * @author Fcat
     * @date 2019/4/9 9:20
     * @param file 导出到的文件
     * @return boolean 导出是否成功
     */
    // todo 该方法要测试文件是否存在，覆盖输出的问题
    // todo 该异常是否应该向上层抛出
    public boolean exportToCSV(File file){
        BufferedWriter bufferedWriter = null;
        boolean isSuccess = false;
        try {
             bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file)));
            String rowData = null;
            //不为空才进行输出操作
            if (!dataset.isEmpty()){
                for(ArrayList<String> row: dataset.values()){
                    rowData = String.join(",", row);
                    bufferedWriter.append(rowData).append("\r\n");
                }
            }
            isSuccess = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            isSuccess = false;
        } catch (IOException e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            if (bufferedWriter != null){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

    public void randomDelete(double delPercent){
        int delSize = (int)(delPercent*dataset.size());
        Random random = new Random();
        System.out.printf("从dataset随机删除%d条%n", delSize);
        int count = 0;
        int randNum;
        while (count<delSize){
            for (Iterator<Map.Entry<String, ArrayList<String>>> it = dataset.entrySet().iterator();
            it.hasNext();){
                it.next();
                randNum = random.nextInt(10);
                if (randNum<4 && count<delSize){
                    it.remove();
                    count++;
                }
            }
        }
        System.out.println("现在还剩" + dataset.size());
    }

    public Map<String, ArrayList<String>> getDataset() {
        return dataset;
    }


}


