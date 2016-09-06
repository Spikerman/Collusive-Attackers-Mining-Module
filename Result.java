import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by chenhao on 5/28/16.
 */
public class Result {

    //存储最后8个cluster里面所有符合条件的app
    public static Set<String> resultAppSet = new HashSet<>();
    static BufferedWriter bufferedWriter;
    public int ccMapSize = 0;
    AlgoFPGrowth algoFPGrowth = new AlgoFPGrowth();
    AlgoApriori algoApriori = new AlgoApriori();
    DbController dbController = new DbController();
    FimController fimController;
    Set<String> userSet = new HashSet<>();
    Set<String> testAppSet = new HashSet<>();

    public Result() {
        System.out.println("=======================  FP-Max STATS =======================");
        fimController = new FimController(dbController);
        fimController.loadCCMapFromDb();
        ccMapSize = fimController.candidateClusterMap.size();
    }


    public static void main(String args[]) {

        int clusterId = 3;
        int support = 5;
        boolean readFromTxt = true;
        boolean isSingleFile = true;
        Result result = new Result();
        String filename = "sourceX/result%d.txt";
        String resultFileName = "appList/appList%d.txt";
        String itemsetAppFileName = "appList/itemsetApp%d.txt";

        itemsetAppFileName = String.format(itemsetAppFileName, support);
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(itemsetAppFileName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        filename = String.format(filename, clusterId);

        if (isSingleFile) {
            if (readFromTxt) {
                try {
                    result.analysisMFIMFromFile(clusterId, support);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                result.startMFIM(filename, clusterId, support, 20);
            }
        } else {
            try {
                for (int i = 1; i <= 8; i++) {
                    result.analysisMFIMFromFile(i, support);
                }
                resultFileName = String.format(resultFileName, support);
                BufferedWriter bw = new BufferedWriter(new FileWriter(resultFileName));
                for (String appId : resultAppSet) {
                    bw.write(appId);
                    bw.newLine();
                }
                bw.flush();
                bw.close();
                System.out.println("写入ok");
                System.out.println("总app数" + resultAppSet.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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

        double appPercentage = (double) commonAppSize / (double) fimController.candidateClusterMap.get(clusterId).size();
        System.out.println("cluster id: " + clusterId + " itemset count: " + itemsetCount + "| user count: " + userCount + "| cover app count: " + commonAppSize
                + "| cover app percentage: " + appPercentage);

        System.out.println("minimum group size: " + absoluteMinUserGroupSize + "| max group size " + maxUserGroupSize +

                "| max co-review app amount: " + maxReviewAppSize);

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

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

        double appPercentage = (double) commonAppSize / (double) fimController.candidateClusterMap.get(clusterId).size();
        System.out.println("cluster id: " + clusterId + " itemset count: " + itemsetCount + "| user count: " + userCount + "| cover app count: " + commonAppSize
                + "| cover app percentage: " + appPercentage);
        System.out.println("minimum group size: " + absoluteMinUserGroupSize + "| max group size " + maxUserGroupSize + " with app num: " + appNumWithMaxUserGroup);
        System.out.println("max co-review app amount: " + maxReviewAppSize + " with user group size: " + userGroupSizeWithMaxReiveApp);

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();


    }

    public void startMFIM(String input, int clusterId, int minAppNum, int minUserGroupSize) {
        userSet.clear();
        AlgoFPMax algoFPMax = new AlgoFPMax(dbController, fimController.candidateClusterMap.get(clusterId));
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
        double appPercentage = (double) commonAppSize / (double) fimController.candidateClusterMap.get(clusterId).size();

        System.out.println(" cluster id: " + clusterId + "| total user count : " + userCount + "| cover app count : " + commonAppSize
                + "| cover app percentage : " + appPercentage);

        itemsetAnalysis(itemsetMap);
        System.out.println("===================================================");


        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
    }

    public void analysisMFIMFromFile(int clusterId, int support) throws Exception {

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
        int clusterSize = fimController.candidateClusterMap.get(clusterId).size();
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

        double appPercentage = (double) commonAppSize / (double) fimController.candidateClusterMap.get(clusterId).size();
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

        Set<String> clusterAppSet = (Set) fimController.candidateClusterMap.get(clutserId);
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
