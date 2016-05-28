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
    DbController dbController = new DbController();
    FimController fimController;
    String output = "output.txt";
    String input = "result.txt";

    Set<String> userSet = new HashSet<>();
    Set<String> testAppSet = new HashSet<>();

    public Result() {
        fimController = new FimController(dbController);
        fimController.loadCandidateCluster();
    }

    public static void main(String args[]) {
        Result result = new Result();
        result.computFim();
        result.commonAppResult();
    }

    public void computFim() {
        try {
            algoFPGrowth.runAlgorithm(input, null, 5, 20);
            algoFPGrowth.printStats();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Itemsets itemsets = algoFPGrowth.getPatterns();
        List<List<Itemset>> levels = itemsets.levels;

        int patternCount = 0;
        int levelCount = 0;
        // for each level (a level is a set of itemsets having the same number of items)
        for (List<Itemset> level : levels) {
            // print how many items are contained in this level
            //System.out.println("  L" + levelCount + " ");
            // for each itemset
            for (Itemset itemset : level) {
//				Arrays.sort(itemset.getItems());
                // print the itemset
                //System.out.print("  pattern " + patternCount + ":  ");

                //itemset.print();

                for (Integer x : itemset.itemset) {
                    userSet.add(x.toString());
                }

                // print the support of this itemset
                //System.out.print("support :  " + itemset.getAbsoluteSupport());
//						+ itemset.getRelativeSupportAsString(nbObject));
                //patternCount++;
                //System.out.println("");
            }
            //levelCount++;
        }

        System.out.println(userSet.size());

    }

    public void commonAppResult() {

        Set<String> clusterAppSet = (Set) fimController.appGroupMap.get(0);
        buildUserReviewAppSet(userSet);
        Set<String> commonSet = Sets.intersection(clusterAppSet, testAppSet);
        System.out.println(commonSet.size());
    }

    public String sqlGenerateForReview(Set<String> userSet) {
        StringBuffer sql = new StringBuffer("SELECT * FROM Data.Review where userId in ");
        StringBuffer range = new StringBuffer("(");
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
    }

    public void buildUserReviewAppSet(Set<String> userSet) {

        userAppMap.clear();
        Statement statement;
        ResultSet rs;
        String sql = sqlGenerateForReview(userSet);

        try {
            statement = dbController.connection.createStatement();
            System.out.println("start user data fetch...");
            rs = statement.executeQuery(sql);
            System.out.println("end user data fetch...");

            String userId;
            String appId;
            while (rs.next()) {
                userId = rs.getString("userId");
                appId = rs.getString("appId");
                testAppSet.add(appId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
