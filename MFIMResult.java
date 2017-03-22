import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by chenhao on 5/28/16.
 */
public class MFIMResult {

    //存储8个cluster 的所有 itemset 中的 app 总和的集合
    public static Set<String> resultAppSet = new HashSet<>();
    static BufferedWriter bufferedWriter;
    public int ccMapSize = 0;
    AlgoFPGrowth algoFPGrowth = new AlgoFPGrowth();
    AlgoApriori algoApriori = new AlgoApriori();
    DbController dbController = new DbController();
    FIMController fimController;
    Set<String> userSet = new HashSet<>();
    Set<String> testAppSet = new HashSet<>();

    public MFIMResult() {
        System.out.println("=======================  FP-Max STATS =======================");
        fimController = new FIMController(dbController);
        fimController.loadClusterMapFromDb();
        ccMapSize = fimController.appClusterMap.size();
    }


    public static void main(String args[]) {
        int clusterId = 3;
        int support = 4;
        boolean readFromTxt = true;
        boolean isSingleFile = false;
        MFIMResult result = new MFIMResult();
        String inputFile = "sourceX/result%d.txt";// resultX 内数据是除去第7个cluster后余下的8个cluster
        String outPutFile = "appList/appList%d.txt";//输出 resultAppSet 的结果
        //itemsetAppFileName unused now
//        String itemsetAppFileName = "appList/itemsetApp%d.txt";
//        itemsetAppFileName = String.format(itemsetAppFileName, support);
//        try {
//            bufferedWriter = new BufferedWriter(new FileWriter(itemsetAppFileName));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        inputFile = String.format(inputFile, clusterId);
        if (isSingleFile) {
            if (readFromTxt) {
                try {
                    result.resultAnalysis(clusterId, support);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                result.fimStart(inputFile, clusterId, support, 20);
            }
        } else {
            try {
                for (int i = 1; i <= 8; i++) {
                    result.resultAnalysis(i, support);
                }
//                outPutFile = String.format(outPutFile, support);
//                BufferedWriter bw = new BufferedWriter(new FileWriter(outPutFile));
//                for (String appId : resultAppSet) {
//                    bw.write(appId);
//                    bw.newLine();
//                }
//                bw.flush();
//                bw.close();
//                System.out.println("写入ok");
//                System.out.println("总app数" + resultAppSet.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //unused
    public void computFim(String input, int clusterId, int minAppNum, int minUserGroupSize) {
        int maxReviewAppSize = 0;
        int maxUserGroupSize = 0;
        long itemsetCount = 0;
        int userGroupSizeWithMaxReiveApp = 0;
        int appNumWithMaxUserGroup = 0;
        Integer absoluteMinUserGroupSize = Integer.MAX_VALUE;
        minUserGroupSize--;
        userSet.clear();

        try {
            algoFPGrowth.runAlgorithm(input, null, minAppNum, minUserGroupSize);
            algoFPGrowth.printStats();
            itemsetCount = algoFPGrowth.getItemsetCount();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Itemsets itemsets = algoFPGrowth.getPatterns();
        List<List<Itemset>> levels = itemsets.levels;

        int levelCount = 0;
        // for each level (a level is a set of itemsets having the same number of items)
        for (List<Itemset> level : levels) {
            // for each itemset
            for (Itemset itemset : level) {

                if (levelCount < absoluteMinUserGroupSize)
                    absoluteMinUserGroupSize = levelCount;

                for (Integer x : itemset.itemset) {
                    userSet.add(x.toString());
                }

                // get the support of this itemset
                int support = itemset.getAbsoluteSupport();
                if (support > maxReviewAppSize)
                    maxReviewAppSize = support;
            }
            levelCount++;
        }

        maxUserGroupSize = levelCount;
        int commonAppSize = commonAppResult(clusterId);
        int userCount = userSet.size();

        double appPercentage = (double) commonAppSize / (double) fimController.appClusterMap.get(clusterId).size();
        System.out.println("cluster id: " + clusterId + " itemset count: " + itemsetCount + "| user count: " + userCount + "| cover app count: " + commonAppSize
                + "| cover app percentage: " + appPercentage);

        System.out.println("minimum group size: " + absoluteMinUserGroupSize + "| max group size " + maxUserGroupSize +

                "| max co-review app amount: " + maxReviewAppSize);

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    //unused
    public void startFIM(String input, int clusterId, int minAppNum, int minUserGroupSize) {
        int maxReviewAppSize = 0;
        int maxUserGroupSize = 0;
        long itemsetCount = 0;
        int userGroupSizeWithMaxReiveApp = 0;
        int appNumWithMaxUserGroup = 0;
        userSet.clear();
        Integer absoluteMinUserGroupSize = Integer.MAX_VALUE;
        minUserGroupSize--;

        try {
            algoFPGrowth.runAlgorithm(input, null, minAppNum, minUserGroupSize);
            algoFPGrowth.printStats();
            itemsetCount = algoFPGrowth.getItemsetCount();
        } catch (Exception e) {
            e.printStackTrace();
        }


        maxReviewAppSize = algoFPGrowth.maxReviewAppSize;
        maxUserGroupSize = algoFPGrowth.maxUserGroupSize;
        userGroupSizeWithMaxReiveApp = algoFPGrowth.userGroupSizeWithMaxReiveApp;
        appNumWithMaxUserGroup = algoFPGrowth.appNumWithMaxUserGroup;

        userSet = algoFPGrowth.userSet;

        int commonAppSize = commonAppResult(clusterId);
        int userCount = userSet.size();

        double appPercentage = (double) commonAppSize / (double) fimController.appClusterMap.get(clusterId).size();
        System.out.println("cluster id: " + clusterId + " itemset count: " + itemsetCount + "| user count: " + userCount + "| cover app count: " + commonAppSize
                + "| cover app percentage: " + appPercentage);
        System.out.println("minimum group size: " + absoluteMinUserGroupSize + "| max group size " + maxUserGroupSize + " with app num: " + appNumWithMaxUserGroup);
        System.out.println("max co-review app amount: " + maxReviewAppSize + " with user group size: " + userGroupSizeWithMaxReiveApp);

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();


    }

    //从数据库中直接读取各 cluster 的数据，然后开始 MFIM
    public void fimStart(String input, int clusterId, int minAppNum, int minUserGroupSize) {
        userSet.clear();
        AlgoFPMax algoFPMax = new AlgoFPMax(dbController, fimController.appClusterMap.get(clusterId));
        try {
            algoFPMax.runAlgorithm(input, null, clusterId, minAppNum, minUserGroupSize);
            algoFPMax.printStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map itemsetMap = algoFPMax.itemsetMap;
        userSet = algoFPMax.userSet;
        int userCount = userSet.size();
        Set coverAppSet = algoFPMax.coverAppSet;
        int commonAppSize = coverAppSet.size();
        double appPercentage = (double) commonAppSize / (double) fimController.appClusterMap.get(clusterId).size();
        System.out.println(" cluster id: " + clusterId + "| total user count : " + userCount + "| cover app count : " + commonAppSize
                + "| cover app percentage : " + appPercentage);
        itemsetAnalysis(itemsetMap);
        System.out.println("===================================================");
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    //从 TXT 文件中读取最终结果，然后对结果进行解析并输出【目前正在用此方法】
    public void resultAnalysis(int clusterId, int support) throws Exception {
        userSet.clear();
        double avgItemsetSize = 0;
        int totalItemsetSize = 0;
        int maxItemsetSize = 0;
        int itemsetCount = 0;
        int coreviewAppNum = 0;

        Set<String> coverAppSet = new HashSet<>();
        String fileName = "Cluster%dSupport%d.txt";
        fileName = String.format(fileName, clusterId, support);
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while (((line = reader.readLine()) != null)) {
            String[] userAppSplited = line.split("\\|");
            String userString = userAppSplited[0];
            String appString = userAppSplited[1];
            String[] userIdArray = userString.split(" ");
            String[] appIdArray = appString.split(" ");
            for (String userId : userIdArray) {
                userSet.add(userId);
            }
            totalItemsetSize += userIdArray.length;
            if (userIdArray.length > maxItemsetSize)
                maxItemsetSize = userIdArray.length;
            for (String appId : appIdArray) {
                System.out.print(appId + " ");
                resultAppSet.add(appId);
                coverAppSet.add(appId);
            }

            System.out.println();

//            Set<Date> dateSet = dateAnalysis(userIdArray, appIdArray);
//            System.out.print("user group size : " + userIdArray.length + " date : ");
//            for (Date date : dateSet) {
//                System.out.print(date + " ");
//            }
//
//            System.out.println();

            coreviewAppNum = appIdArray.length;
            itemsetCount++;
        }


        int coverAppCount = coverAppSet.size();
        int clusterSize = fimController.appClusterMap.get(clusterId).size();
        double appPercentage = (double) coverAppCount / (double) clusterSize;
        avgItemsetSize = (double) totalItemsetSize / (double) itemsetCount;

        System.out.println("============= Analysis From Database With" + " Cluster Id: " + clusterId + " =====================");
        System.out.println("cluster id : " + clusterId);

        System.out.println("co-review app num : " + coreviewAppNum);

        System.out.println("itemset count : " + itemsetCount);

        System.out.println("avg user group size : " + avgItemsetSize);
        System.out.println("max user group size : " + maxItemsetSize);

        System.out.println("total user count : " + userSet.size());
        System.out.println("cover app count : " + coverAppCount);

        System.out.println("cluster size : " + clusterSize);
        System.out.println("cover app percentage : " + appPercentage);
        System.out.println("===================================================");
    }


    //分析日期相关
    public Set<Date> dateAnalysis(String[] userArray, String[] appArray) {
        StringBuffer sqlHead = new StringBuffer("SELECT * FROM Data.Review Where userId in ");
        StringBuffer sqlTail = new StringBuffer("and appId in");
        StringBuffer sqlTail2 = new StringBuffer("and appId =");
        StringBuffer userString = new StringBuffer("(");
        StringBuffer appString = new StringBuffer("(");
        Set<Date> dateSet = new TreeSet<>();

        for (int i = 0; i < userArray.length; i++) {
            if (i != (userArray.length - 1)) {
                String id = userArray[i].toString();
                userString.append(id + ",");
            } else {
                String id = userArray[i].toString();
                userString.append(id + ")");
            }
        }


        for (int i = 0; i < appArray.length; i++) {
            if (i != (appArray.length - 1)) {
                String id = appArray[i].toString();
                appString.append(id + ",");
            } else {
                String id = appArray[i].toString();
                appString.append(id + ")");
            }
        }

        StringBuffer sql = sqlHead.append(userString).append(sqlTail2);
        for (int i = 0; i < appArray.length; i++) {
            dateSet.clear();
            String sql2 = sql + appArray[i];
            Statement statement;
            ResultSet rs;
            try {
                statement = dbController.connection.createStatement();
                rs = statement.executeQuery(sql2.toString());
                while (rs.next()) {
                    Date reviewDate = rs.getDate("date");
                    dateSet.add(reviewDate);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("target app: " + appArray[i] + " user group size : " + userArray.length);
            System.out.print(" date : ");
            for (Date date : dateSet) {
                System.out.print(date + " ");
            }
            System.out.println();
            System.out.println("date size : " + dateSet.size());
            System.out.println("=================================");
        }
//        Statement statement;
//        ResultSet rs;
//
//        try {
//            statement = dbController.connection.createStatement();
//            rs = statement.executeQuery(sql.toString());
//            while (rs.next()) {
//                Date reviewDate = rs.getDate("date");
//                dateSet.add(reviewDate);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return dateSet;
    }


    //todo 函数功能待分析
    public void itemsetAnalysis(Map<Integer, Set<Set<String>>> itemsetMap) {
        for (Map.Entry entry : itemsetMap.entrySet()) {
            int key = (Integer) entry.getKey();
            Set value = (Set<Set<String>>) entry.getValue();
            int itemCount = value.size();
            int maxUserGroupSize = getMaxSize(value);
            System.out.println(" support count : " + key + "| itemset count : " + itemCount + "| max user group size : " + maxUserGroupSize);
        }
    }

    private int getMaxSize(Set<Set<String>> entrySet) {
        int maxSize = 0;
        for (Set<String> set : entrySet) {
            if (set.size() > maxSize)
                maxSize = set.size();
        }
        return maxSize;
    }


    //unused
    public void startFIMWithApriori(String input, int clusterId, int minAppNum, int minUserGroupSize) {
        int maxReviewAppSize = 0;
        int maxUserGroupSize = 0;
        int itemsetCount = 0;
        int userGroupSizeWithMaxReiveApp = 0;
        int appNumWithMaxUserGroup = 0;
        userSet.clear();
        Integer absoluteMinUserGroupSize = Integer.MAX_VALUE;
        minUserGroupSize--;

        try {
            algoApriori.runAlgorithm(input, null, minAppNum, minUserGroupSize);
            algoApriori.printStats();
            itemsetCount = algoApriori.totalCandidateCount;
        } catch (Exception e) {
            e.printStackTrace();
        }

        maxReviewAppSize = algoApriori.maxReviewAppSize;
        maxUserGroupSize = algoApriori.maxUserGroupSize;
        userGroupSizeWithMaxReiveApp = algoApriori.userGroupSizeWithMaxReiveApp;
        appNumWithMaxUserGroup = algoApriori.appNumWithMaxUserGroup;

        userSet = algoApriori.userSet;

        int commonAppSize = commonAppResult(clusterId);
        int userCount = userSet.size();

        double appPercentage = (double) commonAppSize / (double) fimController.appClusterMap.get(clusterId).size();
        System.out.println("cluster id: " + clusterId + " itemset count: " + itemsetCount + "| user count: " + userCount + "| cover app count: " + commonAppSize
                + "| cover app percentage: " + appPercentage);
        System.out.println("minimum group size: " + absoluteMinUserGroupSize + "| max group size " + maxUserGroupSize + " with app num: " + appNumWithMaxUserGroup);
        System.out.println("max co-review app amount: " + maxReviewAppSize + " with user group size: " + userGroupSizeWithMaxReiveApp);

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();


    }

    public int commonAppResult(int clutserId) {
        Set<String> clusterAppSet = (Set) fimController.appClusterMap.get(clutserId);
        buildUserReviewAppSet(userSet);
        Set<String> commonSet = Sets.intersection(clusterAppSet, testAppSet);
        return commonSet.size();
    }

    public String sqlGenerateForReview(Set<String> userSet) {
        StringBuffer sql = new StringBuffer("SELECT * FROM Data.Review where userId in ");
        StringBuffer range = new StringBuffer("(");
        if (userSet.size() != 0) {
            Object[] array = userSet.toArray();
            for (int i = 0; i < array.length; i++) {
                if (i != (array.length - 1)) {
                    String id = array[i].toString();
                    range.append(id + ",");
                } else {
                    String id = array[i].toString();
                    range.append(id + ")");
                }
            }
            sql.append(range);
            return sql.toString();
        } else {
            return null;
        }
    }

    public void buildUserReviewAppSet(Set<String> userSet) {
        Statement statement;
        ResultSet rs;
        String sql = sqlGenerateForReview(userSet);
        if (sql != null) {
            try {
                statement = dbController.connection.createStatement();
                rs = statement.executeQuery(sql);

                String appId;
                while (rs.next()) {
                    appId = rs.getString("appId");
                    testAppSet.add(appId);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
