import com.google.common.collect.Sets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by chenhao on 5/20/16.
 */
public class FIMController {

    //db: AppGroup
    //key:cluster id
    //value: app id set
    public Map<Integer, Set<String>> appClusterMap = new HashMap<>();
    public Map<String, TreeSet<String>> appReviewerMap = new HashMap<>();

    //key: cluster id
    //value: 对应的 cluster id 内的所有 APP 下的所有用户评价的 app ids
    public Map<Integer, Set<String>> testAppGroupMap = new HashMap<>();

    //key: user id
    //value: the set of apps that the user has reviewed
    public Map<String, Set<String>> userAppMap = new HashMap<>();
    public Set<String> userSet = new HashSet<>();
    public PreparedStatement selectRevewSqlStmt;
    public PreparedStatement selectRevewForAppStmt;
    DbController dbController;

    //db: user_group
    //key: cluster id
    //value: user id set
    Map<Integer, Set<String>> userGroupMap = new TreeMap<>();
    String selectUserSql = "SELECT * FROM Data.user_group;";
    String selectClusterSql = "SELECT * FROM Data.CandidateCluster;";
    String selectReviewSql = "SELECT * FROM Data.Review where userId=?; ";
    String selectReviewForApp = "SELECT count(*) FROM Data.Review where appId=?";
    String testSql = "SELECT group_id, user_id,threshold FROM Data.user_group where cluster_id=2 and threshold=9;";
    Set<String> testSet = new HashSet<>();

    public FIMController(DbController dbController) {
        this.dbController = dbController;
        try {
            selectRevewSqlStmt = dbController.connection.prepareStatement(selectReviewSql);
            selectRevewForAppStmt = dbController.connection.prepareStatement(selectReviewForApp);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void printAppReviewMap() {
        List<Integer> array = new ArrayList<>();
        for (Map.Entry entry : appReviewerMap.entrySet()) {
            String key = entry.getKey().toString();
            int x = ((Set) entry.getValue()).size();
            array.add(x);
        }
        Collections.sort(array);
        for (int x : array) {
            System.out.println(x);
        }
    }


    public void executeFimTest() {
        Statement statement;
        ResultSet rs;
        try {
            statement = dbController.connection.createStatement();
            rs = statement.executeQuery(testSql);
            String userId;
            while (rs.next()) {
                userId = rs.getString("user_id");
                testSet.add(userId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Set<String> appSet;
        Set<String> tmpSet;
        Object[] array = testSet.toArray();
        for (int i = 0; i < array.length; i++) {
            String userId = array[i].toString();
            appSet = getReviewApps(userId);
            for (String appId : appSet) {
                insertToAppReviewerMap(appId, userId);
            }
        }
    }


    //构造user map, key值为对应的组数, 结果为得到的collusive attackers的集合
    public void loadUserGroup() {
        Statement statement;
        ResultSet rs;
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start user train fetch...");
            rs = statement.executeQuery(selectUserSql);
            System.out.println("end user train fetch...");
            int clusterId;
            String userId;
            while (rs.next()) {
                clusterId = Integer.parseInt(rs.getString("cluster_id"));
                userId = rs.getString("user_id");
                userSet.add(userId);
                if (userGroupMap.containsKey(clusterId)) {
                    userGroupMap.get(clusterId).add(userId);
                } else {
                    Set newUserGroup = new HashSet<>();
                    newUserGroup.add(userId);
                    userGroupMap.put(clusterId, newUserGroup);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        buildUserAppMap();
    }

    public void buildUserAppMap() {
        Statement statement;
        ResultSet rs;
        String sql = sqlGenerateForReview();
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start user train fetch...");
            rs = statement.executeQuery(sql);
            System.out.println("end user train fetch...");
            String userId;
            String appId;
            while (rs.next()) {
                userId = rs.getString("userId");
                appId = rs.getString("appId");
                insertToUserAppMap(userId, appId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void buildAppReviewMap(int cluster) {
        appReviewerMap.clear();
        Statement statement;
        ResultSet rs;
        Set<String> appSet = appClusterMap.get(cluster);
        String sql = sqlGenerateForAppGroupReview(appSet);
        try {
            statement = dbController.connection.createStatement();
            rs = statement.executeQuery(sql);
            String userId;
            String appId;
            while (rs.next()) {
                userId = rs.getString("userId");
                appId = rs.getString("appId");
                insertToAppReviewerMap(appId, userId);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertToAppReviewerMap(String appId, String userId) {
        if (appReviewerMap.containsKey(appId)) {
            appReviewerMap.get(appId).add(userId);
        } else {
            TreeSet<String> newIdSet = new TreeSet<>();
            newIdSet.add(userId);
            appReviewerMap.put(appId, newIdSet);
        }
    }

    //构造app group map, key为对应的组, value为组内的app数
    public void loadClusterMapFromDb() {
        Statement statement;
        ResultSet rs;
        try {
            statement = dbController.connection.createStatement();
            System.out.println(" start cluster train fetch...");
            rs = statement.executeQuery(selectClusterSql);
            System.out.println(" end cluster train fetch...");
            String appId;
            int groupId;
            while (rs.next()) {
                groupId = rs.getInt("clusterId");
                appId = rs.getString("appId");
                insertToAppCluster(groupId, appId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void insertToAppCluster(Integer groupId, String appId) {
        if (appClusterMap.containsKey(groupId)) {
            appClusterMap.get(groupId).add(appId);
        } else {
            TreeSet<String> newIdSet = new TreeSet<>();
            newIdSet.add(appId);
            appClusterMap.put(groupId, newIdSet);
        }
    }

    private void insertToUserAppMap(String userId, String appId) {
        if (userAppMap.containsKey(userId)) {
            userAppMap.get(userId).add(appId);
        } else {
            Set<String> newIdSet = new HashSet<>();
            newIdSet.add(appId);
            userAppMap.put(userId, newIdSet);
        }

    }


    private void insertToTestAppMap(Integer clusterId, Set<String> appIdSet) {
        if (testAppGroupMap.containsKey(clusterId)) {
            testAppGroupMap.get(clusterId).addAll(appIdSet);
        } else {
            Set<String> newIdSet = new HashSet<>();
            newIdSet.addAll(appIdSet);
            testAppGroupMap.put(clusterId, newIdSet);
        }

    }


    public void buildTestAppGroupMap() {
        for (Map.Entry entry : userGroupMap.entrySet()) {
            int clusterId = (Integer) entry.getKey();
            Set<String> userGroup = (Set) entry.getValue();
            for (String userId : userGroup) {
                Set<String> reviewApps = userAppMap.get(userId);
                insertToTestAppMap(clusterId, reviewApps);
            }
        }
    }

    public Set<String> getReviewApps(String userId) {
        Set<String> appSet = new HashSet<>();
        ResultSet rs;
        try {
            selectRevewSqlStmt.setString(1, userId);
            rs = selectRevewSqlStmt.executeQuery();
            while (rs.next()) {
                String appId = rs.getString("appId");
                appSet.add(appId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appSet;
    }


    public void groupCompareTest() {

        Set<Integer> clusterIdSet = testAppGroupMap.keySet();
        for (int key : clusterIdSet) {
            Set<String> appGroup = appClusterMap.get(key);
            Set<String> reviewAppGroup = testAppGroupMap.get(key);
            Set<String> commonAppSet = Sets.intersection(appGroup, reviewAppGroup);
            Set<String> userGroup = userGroupMap.get(key);

            double x = commonAppSet.size();
            double y = appGroup.size();
            double z = x / y;
            System.out.println("TAC id: " + key + " user size: " + userGroup.size() + " Cover Apps size: " + x + " Cover Apps Percentage: " + z);
        }
    }

    public String sqlGenerateForReview() {
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

    public String sqlGenerateForAppGroupReview(Set<String> set) {
        StringBuffer sql = new StringBuffer("SELECT * FROM Data.Review where appId in ");
        StringBuffer range = new StringBuffer("(");
        Object[] array = set.toArray();
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


    public String sqlGenerateForApp(Set<String> cluster) {
        StringBuffer sql = new StringBuffer("SELECT count(*) as amount FROM Data.Review where appId in ");

        StringBuffer range = new StringBuffer("(");

        Object[] array = cluster.toArray();

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

    public void countClusterReviewAmount() {
        Statement statement;
        ResultSet rs;
        for (Map.Entry entry : appClusterMap.entrySet()) {
            Set<String> cluster = (Set) entry.getValue();
            int tacId = (Integer) entry.getKey();
            String sql = sqlGenerateForApp(cluster);
            try {
                statement = dbController.connection.createStatement();
                rs = statement.executeQuery(sql);
                while (rs.next()) {
                    int amount = rs.getInt("amount");
                    System.out.println(tacId + ":  " + amount);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
