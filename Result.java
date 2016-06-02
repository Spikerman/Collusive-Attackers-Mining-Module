import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by chenhao on 5/28/16.
 */
public class Result {

    public Map<String, Set<String>> userAppMap = new HashMap<>();
    AlgoFPGrowth algoFPGrowth = new AlgoFPGrowth();
    AlgoApriori algoApriori = new AlgoApriori();
    DbController dbController = new DbController();
    FimController fimController;


    Set<String> userSet = new HashSet<>();
    Set<String> testAppSet = new HashSet<>();

    public Result() {
        System.out.println("=======================  FP-Max STATS =======================");
        fimController = new FimController(dbController);
        fimController.loadCandidateCluster();
    }

    public static void main(String args[]) {

        int clusterId = 7;
        Result result = new Result();
        String filename = "source/result%d.txt";
        filename = String.format(filename, clusterId);
        result.startMFIM(filename, clusterId, 5, 20);

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

        double appPercentage = (double) commonAppSize / (double) fimController.appGroupMap.get(clusterId).size();
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

        double appPercentage = (double) commonAppSize / (double) fimController.appGroupMap.get(clusterId).size();
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
        AlgoFPMax algoFPMax = new AlgoFPMax(dbController, fimController.appGroupMap.get(clusterId));
        try {
            algoFPMax.runAlgorithm(input, null, minAppNum, minUserGroupSize);
            algoFPMax.printStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        itemsetCount = algoFPMax.itemsetCount;
//        maxReviewAppSize = algoFPMax.maxReviewAppSize;
//        maxUserGroupSize = algoFPMax.maxUserGroupSize;
//        userGroupSizeWithMaxReiveApp = algoFPMax.userGroupSizeWithMaxReiveApp;
//        appNumWithMaxUserGroup = algoFPMax.appNumWithMaxUserGroup;

        Map itemsetMap = algoFPMax.itemsetMap;
        userSet = algoFPMax.userSet;
        int commonAppSize = commonAppResult(clusterId);
        int userCount = userSet.size();
        double appPercentage = (double) commonAppSize / (double) fimController.appGroupMap.get(clusterId).size();


        System.out.println(" cluster id: " + clusterId + "| total user count : " + userCount + "| cover app count : " + commonAppSize
                + "| cover app percentage : " + appPercentage);
        itemsetAnalysis(itemsetMap);
        System.out.println("===================================================");

//        System.out.println("minimum group size: " + absoluteMinUserGroupSize + "| max group size " + maxUserGroupSize + " with app num: " + appNumWithMaxUserGroup);
//        System.out.println("max co-review app amount: " + maxReviewAppSize + " with user group size: " + userGroupSizeWithMaxReiveApp);

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
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

        double appPercentage = (double) commonAppSize / (double) fimController.appGroupMap.get(clusterId).size();
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

        Set<String> clusterAppSet = (Set) fimController.appGroupMap.get(clutserId);
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
