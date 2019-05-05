/*package yj;*/

/**
 * <p>Aprioritest</p>
 * @author YangJie
 * @data 2019年3月26日下午2:40:39
 * @version 1.0
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;
import java.util.regex.Pattern;

import javax.swing.text.StyledEditorKit.BoldAction;

import java.lang.StringBuffer;
// import javax.swing.text.html.HTMLDocument.Iterator;
import java.sql.Date;

import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;
/*import com.sun.org.apache.xpath.internal.operations.String;*/
import com.sun.javafx.css.CssError.StringParsingError;

/**
 * <p>
 * Apriori
 * </p>
 * <p>
 * Description: 实现Apriori算法，并用其产生符合最小支持度的频繁项集， 再根据频繁项集产生关联规则。
 * </p>
 * 
 * @author YangJie
 * @data 2018年11月8日下午3:39:59
 * @version 1.0
 */
public class Aprioritest {
    private final static int SUPPORT = 3; // 最小支持度
    private final static double CONFIDENCE = 0.6; // 最小置信度
    private final static String ITEM_SPLIT = ","; // 分隔符
    private final static String FILE_ADDRESS = "weather.nominal.txt"; // 分隔符
    private final static String CON = "->";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        Aprioritest apriori = new Aprioritest();
        Map<String, Integer> tempFrequentSetMap = new HashMap<>();
        Map<String, Integer> frequentSetMap = new HashMap<>();
        Map<String, Double> associationRules = new HashMap<>();

        ArrayList<String> listFromFile = apriori.readFile(FILE_ADDRESS);
        tempFrequentSetMap = apriori.findFrequentOneSets(listFromFile);
        /*System.out.println("-----------------------------1项集-------------------------------");
        printHashMap(tempFrequentSetMap);*/

        while (!tempFrequentSetMap.isEmpty()) {
            frequentSetMap.putAll(tempFrequentSetMap);
            tempFrequentSetMap = apriori.getCandidateSetMap(tempFrequentSetMap);
            /*System.out.println("---------------------------candidate-------------------------");
            printHashMap(tempFrequentSetMap);*/
            tempFrequentSetMap = apriori.getFrequentSetMap(listFromFile, tempFrequentSetMap);
            /*System.out.println("---------------------------frequent-------------------------");
            printHashMap(tempFrequentSetMap);*/
        }
        /*System.out.println("---------------------------FinalFrequent-------------------------");
        printHashMap(frequentSetMap);*/
        associationRules = apriori.getAssociationRules(frequentSetMap);

        System.out.println("-----------------------------频繁项集---------------------------------：");
        List<String> list = new ArrayList<String>(frequentSetMap.keySet());
        Collections.sort(list);
        for (String string : list) {
            System.out.print(string + " : " + frequentSetMap.get(string) + "\t");
        }
        System.out.println();

        System.out.println("-----------------------------关联规则---------------------------------： ");
        List<String> associationRulesList = new ArrayList<String>(associationRules.keySet());
        //Collections.sort(list);
        for (String string : associationRulesList) {
            System.out.println(string + " : " + associationRules.get(string));
        }
        System.out.print(associationRulesList.size());
    }

    /**
     * -从文件读取数据，存入List中
     * 
     * @param fileAdd
     * @return arrayList
     * @throws IOException
     */
    private ArrayList<String> readFile(String fileAdd) throws IOException {

        ArrayList<String> arrayList = new ArrayList<>();
        File file = new File(fileAdd);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                arrayList.add(tempString);
            }
            reader.close();
            return arrayList;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    /**
     * -发现频繁一项集
     * 
     * @param dataList
     * @return resultSetMap
     */
    private Map<String, Integer> findFrequentOneSets(ArrayList<String> dataList) {
        Map<String, Integer> Oneset = new HashMap<String, Integer>();
        for (String data : dataList) {
            String[] strings = data.split(ITEM_SPLIT);
            for (String str : strings) {
                if (!Oneset.containsKey(str)) {
                    Oneset.put(str, 1);
                } else {
                    Integer count = Oneset.get(str);
                    count++;
                    Oneset.put(str, count);
                }
            }
        }
        Map<String, Integer> FrequentOneSets = new HashMap<String, Integer>();
        Iterator iter = Oneset.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry) iter.next();
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (value >= SUPPORT) {
                FrequentOneSets.put(key, value);
            }
        }
        return FrequentOneSets;
    }

    /**
     * 获取所有候选频繁项集，包含连接步和剪枝步
     * 
     * @param inputMap
     * @return candidateSetMap
     */
    private Map<String, Integer> getCandidateSetMap(Map<String, Integer> inputMap) {
        /* 连接操作 */
        List<Map.Entry<String, Integer>> RankedList = new ArrayList<Map.Entry<String, Integer>>(inputMap.entrySet());
        /* 按字母顺序排序 */
        Collections.sort(RankedList, new Comparator<Map.Entry<String, Integer>>() {
            // 升序排序
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        /*
         * for (Map.Entry<String, Integer> mapping : RankedList) {
         * System.out.println(mapping.getKey() + ":" + mapping.getValue()); }
         */
        /* 连接 */
        /* List<String> tempList = new ArrayList<String>(); */
        List<String> newList = new ArrayList<String>();
        String[] data = RankedList.get(0).getKey().split(ITEM_SPLIT);
        int numberOfOneSets = data.length;// 项的个数，确定当前在对几项集进行连接
        if (numberOfOneSets == 1) {// 对频繁1项集进行连接
            for (Map.Entry<String, Integer> li1 : RankedList) {
                for (Map.Entry<String, Integer> li2 : RankedList) {
                    if (li1.getKey() != li2.getKey()) {
                        newList.add(li1.getKey() + ITEM_SPLIT + li2.getKey());
                    }
                }
            }
        } else {// 对频繁n项集进行连接
            for (Map.Entry<String, Integer> li1 : RankedList) {
                String[] data1 = li1.getKey().split(ITEM_SPLIT);
                for (Map.Entry<String, Integer> li2 : RankedList) {
                    String[] data2 = li2.getKey().split(ITEM_SPLIT);
                    boolean isFrontSetEqual = true;
                    for (int i = 0; i < numberOfOneSets - 1; ++i) {
                        if (!data1[i].equals(data2[i])) {
                            isFrontSetEqual = false;
                            break;
                        }
                    }
                    if ((isFrontSetEqual) && (li1.getKey() != li2.getKey())) {// 连接
                        StringBuffer gather = new StringBuffer("");
                        for (int i = 0; i <= data1.length - 1; ++i) {
                            if (!gather.toString().contains(data1[i])) {
                                gather.append(data1[i] + ITEM_SPLIT);
                            }
                        }
                        String[] departGather = gather.toString().split(ITEM_SPLIT);
                        for (int i = 0; i <= data2.length - 1; ++i) {
                            boolean flag = false;
                            for (int j = 0; j <= departGather.length - 1; ++j) {
                                if (departGather[j].matches(data2[i])) {
                                    flag = true;
                                }
                            }
                            if (flag == false) {
                                gather.append(data2[i] + ITEM_SPLIT);
                            }
                            /*
                             * if (!gather.toString().contains(data2[i])) { gather.append(data2[i] + ","); }
                             */
                        }
                        gather.replace(gather.length() - 1, gather.length(), "");
                        /* System.out.println(gather.toString()); */
                        newList.add(gather.toString());
                    }
                }
            }
        } // 连接成功！

        /*System.out.println("-----------------before cutting-------------------!");
        printArrayList(newList);*/

        /*
         * if(numberOfOneSets != 1) { for(String li1: tempList) { String[] data1 =
         * li1.split(","); Boolean exist = false; for() } }
         */

        /* 调试检查 */
        /* printArrayList(newList); */

        /* 剪枝操作 */
        for (String str : newList) {
            String[] newData = str.split(",");
            int isNotCutOff = 0;// 用于判断是否需要被剪枝，改参数对新项集中的每一项的每个子项与原项集是否匹配进行记录
            for (int i = 0; i <= numberOfOneSets; ++i) {
                String pattern = ".*";
                for (int j = 0; j <= numberOfOneSets && j != i; ++j) {
                    pattern += newData[j] + ".*";
                }
                for (Map.Entry<String, Integer> li : RankedList) {// 与原项集的每一项进行匹配
                    if (Pattern.matches(pattern, li.getKey())) {
                        isNotCutOff++;
                        break;
                    }
                }
            }
            if (isNotCutOff != numberOfOneSets + 1) {// 每个子项不全部匹配，则把它剪掉
                newList.remove(str);
            }
        }

        /*System.out.println("-------------------after cutting--------------------!");
        printArrayList(newList);*/

        // 去重
        List<String> newlist1 = new ArrayList<String>();
        for (String li : newList) {
            String[] newlist_split = li.split(",");
            List<String> newlist_split1 = new ArrayList<>();
            for (int i = 0; i <= newlist_split.length - 1; ++i) {
                newlist_split1.add(newlist_split[i]);
            }
            Collections.sort(newlist_split1, (a, z) -> a.compareTo(z));
            StringBuffer tmp = new StringBuffer("");
            for (String li1 : newlist_split1) {
                tmp.append(li1 + ",");
            }
            tmp.replace(tmp.length() - 1, tmp.length(), "");
            newlist1.add(tmp.toString());
        }

        for (int i = 0; i < newlist1.size() - 1; i++) {
            for (int j = newlist1.size() - 1; j > i; j--) {
                if (newlist1.get(j).equals(newlist1.get(i))) {
                    newlist1.remove(j);
                }
            }
        }

        /*System.out.println("after derepeat!");
        printArrayList(newlist1);*/

        Map<String, Integer> CandidateSetMap = new HashMap<String, Integer>();

        for (String str : newlist1) {
            CandidateSetMap.put(str, 0);
        }

        return CandidateSetMap;
    }

    /**
     * -根具候选项集获取满足最小支持度的频繁项集
     * 
     * @param inputList
     * @param inputMap
     * @return
     */
    private Map<String, Integer> getFrequentSetMap(ArrayList<String> inputList, Map<String, Integer> inputMap) {

        int flog = 0;
        List<String> list = new ArrayList<>();
        Set<String> keySet = new HashSet<>();
        keySet.addAll(inputMap.keySet());

        for (String data : inputList) {
            String[] strings = data.split(ITEM_SPLIT);
            for (int i = 0; i < strings.length; i++) {
                list.add(strings[i]);
            }

            for (String string : keySet) {
                String[] keyItem = string.split(ITEM_SPLIT);
                flog = keyItem.length;
                for (String string2 : keyItem) {
                    if (list.contains(string2)) {
                        --flog;
                    }
                }

                if (flog == 0) {
                    inputMap.put(string, inputMap.get(string) + 1);
                }
            }

            list.clear();
        }

        for (String string : keySet) {
            if (inputMap.get(string) < SUPPORT) {
                inputMap.remove(string);
            }
        }
        return inputMap;
    }

    /**
     * -根据频繁项集获取关联规则
     * 
     * @param frequentSetMap
     * @return
     */
    private Map<String, Double> getAssociationRules(Map<String, Integer> frequentSetMap) {
        Map<String, Integer> oneSets = new HashMap<String, Integer>();
        Map<String, Integer> twoSets = new HashMap<String, Integer>();
        Map<String, Integer> threeSets = new HashMap<String, Integer>();
        Map<String, Double> assosiationRules = new HashMap<String, Double>();
        for (Map.Entry<String, Integer> en : frequentSetMap.entrySet()) {//不同k频繁项集分类
            String[] data = en.getKey().split(ITEM_SPLIT);
            switch (data.length) {
            case 1:
                oneSets.put(en.getKey(), en.getValue());
                break;
            case 2:
                twoSets.put(en.getKey(), en.getValue());
                break;
            case 3:
                threeSets.put(en.getKey(), en.getValue());
                break;
            default:
                break;
            }
        }
        for(Map.Entry<String, Integer> en: twoSets.entrySet()) {
            String[] data = en.getKey().split(ITEM_SPLIT);
            for(int i = 0; i <= data.length - 1; ++i) {
                double con;
                con = (double)en.getValue()/(double)oneSets.get(data[i]);
                if(con >= CONFIDENCE) {
                    assosiationRules.put(data[i] + CON + data[data.length - i - 1], con);
                }
            }
        }
        for(Map.Entry<String, Integer> en: threeSets.entrySet()) {
            String[] data = en.getKey().split(ITEM_SPLIT);
            for(int i = 0; i <= data.length - 1; ++i) {
                double con;
                con = (double)en.getValue()/(double)oneSets.get(data[i]);
                if(con >= CONFIDENCE) {
                    StringBuffer tmp = new StringBuffer("");
                    tmp.append(data[i] + CON);
                    for(int j = 0; j <= data.length - 1; j++) {
                        if(data[j] != data[i]) {
                            tmp.append(data[j] + ITEM_SPLIT);
                        }
                    }
                    tmp.replace(tmp.length() - 1, tmp.length(), "");
                    assosiationRules.put(tmp.toString(), con);
                }
            }
        }
        for(Map.Entry<String, Integer> en: threeSets.entrySet()) {
            String[] data = en.getKey().split(ITEM_SPLIT);
            for(int i = 0; i <= data.length - 1; ++i) {
                double con;
                StringBuffer part = new StringBuffer("");
                for(int j = 0; j <=data.length - 1; ++j) {
                    if(data[j] != data[i]) {
                        part.append(data[j] + ITEM_SPLIT);
                    }
                }
                part.replace(part.length() - 1, part.length(), "");
                con = (double)en.getValue()/(double)twoSets.get(part.toString());
                if(con >= CONFIDENCE) {
                    StringBuffer tmp = new StringBuffer("");
                    tmp.append(part + CON + data[i]);
                    assosiationRules.put(tmp.toString(), con);
                }
            }
        }

        return assosiationRules;
    }

    // 用于调试检查的HashMap打印
    private static void printHashMap(Map<String, Integer> map) {
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry) iter.next();
            System.out.print(entry.getKey() + ": " + entry.getValue() + " ");
        }
        System.out.println("");
        /*
         * System.out.
         * println("-------------------------    分割线    --------------------------");
         */
    }

    // 用于调试检查的ArrayList打印
    private static void printArrayList(List<String> list) {
        for (String li : list) {
            System.out.print(li + " ");
        }
        System.out.println("");
        /*
         * System.out.
         * println("-------------------------    分割线    --------------------------");
         */
    }
}