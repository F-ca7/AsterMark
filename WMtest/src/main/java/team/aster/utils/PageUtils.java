package team.aster.utils;

import java.util.ArrayList;

public class PageUtils {
    /**
     * 根据分页获取数据集
     * @param pageSizeCount 一页有多少条数据
     * @param pageIndex     第几页的数据，从1开始
     * @return
     */
    public static ArrayList<ArrayList<String>> getPagedDataset(ArrayList<ArrayList<String>> dataset,int pageSizeCount, int pageIndex){
        int totalSize = dataset.size();
        int offset = (pageIndex-1)*pageSizeCount;
        int end = offset + pageSizeCount;
        end = Math.min(totalSize, end);

        // 左闭右开
        return new ArrayList<>(dataset.subList(offset, end));
    }


    /**
     * 判断是否还有下一页数据
     * @param pageSizeCount 一页有多少条数据
     * @param curPageIndex  当前页面
     * @return
     */
    public static boolean hasNextPage(ArrayList<ArrayList<String>> dataset, int pageSizeCount, int curPageIndex){
        int totalSize = dataset.size();
        int offset = (curPageIndex)*pageSizeCount;
        return offset<totalSize;
    }

}
