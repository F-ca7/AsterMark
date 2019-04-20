package team.aster.algorithm;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 基于词性逆序数的算法
 */
public class POSInversionAlgorithm {
    private static MaxentTagger tagger;
    private static final int VERB_NUM = 1;
    private static final int ADV_NUM = 0;
    private static ArrayList<String> noSpaceWordList = new ArrayList<String>(){{
        add("n't");add("'");add(".");add(",");add("!");add("?");add(";");}};
    private static ArrayList<String> randomAdvList  = new ArrayList<String>(){{
        add("really");
        add("actually");
        add("virtually");
    }};

    public static String encodeTextByPOS(String text, int bit) {
        PTBTokenizer<CoreLabel> ptb = new PTBTokenizer<CoreLabel>(new StringReader(text), new CoreLabelTokenFactory(), null);
        ArrayList<String> words = new ArrayList<String>();
        String word;
        while (ptb.hasNext()){
            word = ptb.next().toString();
            words.add(word);
        }
        // 分词器
        tagger = new MaxentTagger(MaxentTagger.DEFAULT_DISTRIBUTION_PATH);
        // 分句器
//      WordToSentenceProcessor wtsp = new WordToSentenceProcessor();
        List<TaggedWord> taggedSent = tagger.tagSentence(SentenceUtils.toWordList((String[])words.toArray(new String[words.size()])));
        int wordIndex=-1;
        String advWord;
        // 是否没有副词
        boolean hasNoAdv =true;
        ArrayList<Map.Entry<Integer,Integer>> sequenceList = new ArrayList<Map.Entry<Integer,Integer>>();
        // 遍历检测动词和副词
        for (TaggedWord tw : taggedSent) {
            wordIndex++;
//            System.out.println(tw.tag()+"   "+tw.word()  );
            if (tw.tag().startsWith("VB")) {
//                System.out.println("动词"+tw.word()+"  "+wordIndex);
                sequenceList.add(new ImmutablePair<Integer,Integer>(wordIndex, VERB_NUM));
            }
            if (tw.tag().startsWith("RB")) {
                advWord = tw.word().toLowerCase();
                if (advWord.equals("not")||advWord.equals("n't")){
                    continue;
                }
//                System.out.println("副词"+tw.word()+"  "+wordIndex);
                hasNoAdv = false;
                sequenceList.add(new ImmutablePair<Integer,Integer>(wordIndex, ADV_NUM));
            }

        }
        int inversionNum = calcInversionInPair(sequenceList);
        //System.out.println("逆序数为"+inversionNum);
        // 不做修改
        if (inversionNum%2==bit)
            return text;
        // 否则修改一次最相邻的动副词逆序数
        // 一次对换必定改变逆序数奇偶性
        // 把副词插在动词前或者后
        if (hasNoAdv){
            // 如果没有副词
            // 直接在第二个动词前加一个副词
            String addedAdv = getRandomAdv();
            TaggedWord wordAdv = new TaggedWord(addedAdv);
            int toInserIndex = sequenceList.size()>1?sequenceList.get(1).getKey():sequenceList.get(0).getKey();
            taggedSent.add(toInserIndex, wordAdv);
        } else {

            ArrayList<Integer> toSwapPair = findNearestPairIndex(sequenceList);
            int toSwapI1 = toSwapPair.get(0);
            int toSwapI2 = toSwapPair.get(1);
            int toSwapPOS1 = toSwapPair.get(2);
            //System.out.println("最邻近的index对 " + toSwapPair);
            if (toSwapPOS1==VERB_NUM){
                // 如果前面一个是动词
                // 把副词提到动词前
                //System.out.println("前面一个是动词");
                TaggedWord wordAdv = taggedSent.get(toSwapI2);
                // 移除原来的副词
                taggedSent.remove(toSwapI2);
                //System.out.println(taggedSent.get(toSwapPair.getKey()).word());
                taggedSent.add(toSwapI1, wordAdv);
                //System.out.println(taggedSent.get(toSwapPair.getKey()).word());
            } else {
                // 如果前面一个是副词
                // 把副词放到动词后
                //System.out.println("前面一个是副词");
                TaggedWord wordAdv = taggedSent.get(toSwapI1);
                // 移除原来的副词
                taggedSent.remove(toSwapI1);
                //System.out.println(taggedSent.get(toSwapPair.getKey()).word());
                taggedSent.add(toSwapI2, wordAdv);
                //System.out.println(taggedSent.get(toSwapPair.getKey()).word());
            }
        }
        return joinTaggedSentenceToText(taggedSent);
    }

    private static String getRandomAdv() {
        Random random = new Random();
        int i = random.nextInt(randomAdvList.size());
        return randomAdvList.get(i);
    }

    private static boolean isDirectlyConcatWord(String word){
        for (String mWord:noSpaceWordList){
            if (word.startsWith(mWord))
                return true;
        }
        return false;
    }


    private static String joinTaggedSentenceToText(List<TaggedWord> taggedSent){
        StringBuilder stringBuilder = new StringBuilder();
        String concatWord, concatNextWord;
        for (int i=0; i<taggedSent.size()-1; i++){
            concatWord = taggedSent.get(i).word();
            concatNextWord = taggedSent.get(i+1).word();
            stringBuilder.append(concatWord);
            // 判断要不要加空格
            if (!isDirectlyConcatWord(concatNextWord)){
                stringBuilder.append(" ");
            }
        }
        stringBuilder.append(taggedSent.get(taggedSent.size()-1).word());
        return stringBuilder.toString();
    }

    /**
     * 找到最相邻的动副词
     * @return 最邻近的第一个index
     */
    private static ArrayList<Integer> findNearestPairIndex(ArrayList<Map.Entry<Integer,Integer>> wordIndexList){
        ArrayList<Integer> indexList = new ArrayList<Integer>();
        int pairIndex = 0, nextPairIndex=0,dist = Integer.MAX_VALUE;
        int n = wordIndexList.size();
        int tmpDist;
        int curPOS = VERB_NUM;
        Map.Entry<Integer,Integer> curPair;
        Map.Entry<Integer,Integer> nextPair;
        for (int i=0; i<n-1; i++){
            curPair = wordIndexList.get(i);
            nextPair = wordIndexList.get(i+1);
            // 相邻相同词性跳过
            if (curPair.getValue().equals(nextPair.getValue())){
                continue;
            }
            //获取当前词性
            curPOS = curPair.getValue();
            tmpDist = nextPair.getKey()-curPair.getKey();
            if (tmpDist < dist){
                dist = tmpDist;
                pairIndex = curPair.getKey();
                nextPairIndex = nextPair.getKey();
            }
            if (dist==1)
                break;
        }
        indexList.add(pairIndex);
        indexList.add(nextPairIndex);
        indexList.add(curPOS);
        return indexList;
    }


    /**
     * 逆序数为奇数水印位为1,
     * 逆序数为偶数水印位为0
     * @param text 一段文本，可以有多个句子
     * @return 对应水印位
     */
    public static int detectBitInSingleText(String text){
        int inversion = getInversionInText(text);
        return inversion%2;
    }

    private static int getInversionInText(String text) {
        PTBTokenizer<CoreLabel> ptb = new PTBTokenizer<CoreLabel>(new StringReader(text), new CoreLabelTokenFactory(), null);
        ArrayList<String> words = new ArrayList<String>();
        String word;
        while (ptb.hasNext()){
            word = ptb.next().toString();
            words.add(word);
        }
        tagger = new MaxentTagger(MaxentTagger.DEFAULT_DISTRIBUTION_PATH);
        List<TaggedWord> taggedSent = tagger.tagSentence(SentenceUtils.toWordList((String[])words.toArray(new String[words.size()])));
        String advWord;
        int wordIndex=-1;
        ArrayList<Integer> sequenceList = new ArrayList<Integer>();
        for (TaggedWord tw : taggedSent) {
            wordIndex++;
            //System.out.println(tw.tag()+"   "+tw.word()  + "   "+wordIndex);
            if (tw.tag().startsWith("VB")) {
                //System.out.println("动词"+tw.word()+"  "+wordIndex);
                sequenceList.add(VERB_NUM);
            }

            if (tw.tag().startsWith("RB")) {
                advWord = tw.word().toLowerCase();
                // 过滤否定词
                if (advWord.equals("not")||advWord.equals("n't")){
                    continue;
                }
                //System.out.println("副词"+tw.word()+"  "+wordIndex);
                sequenceList.add(ADV_NUM);
            }
        }
        //System.out.println(sequenceList);
        int inversionNum = calcInversion(sequenceList);
        //System.out.println("逆序数为 "+inversionNum);
        return inversionNum;
    }


    private static int calcInversionInPair(ArrayList<Map.Entry<Integer,Integer>> arr){
        int n = arr.size();
        int count = 0;
        for(int i = 0; i < n; i++){
            for(int j = i; j < n; j++)
            {
                if(arr.get(i).getValue()> arr.get(j).getValue())
                    count++;
            }
        }
        return count;
    }

    /**
     * 计算逆序数
     * @param arr
     * @return 逆序数值
     */
    private static int calcInversion(ArrayList<Integer> arr){
        int n = arr.size();
        int count = 0;
        for(int i = 0; i < n; i++){
            for(int j = i; j < n; j++)
            {
                if(arr.get(i)> arr.get(j))
                    count++;
            }
        }
        return count;
    }

}
