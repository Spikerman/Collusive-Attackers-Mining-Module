package MLPart;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Author: Spikerman
 * Created Date: 17/3/13
 */

//生成用于测试的应用对
public class TestPairGenerator {
    private static final int FREQUENCY = 14;
    private Set<Instance> testPairSet = new HashSet<>();
    private MlDbController mlDbController;
    private List<AppData> appDataRecordListForRank = new LinkedList<>();
    private Map<String, List<AppData>> appMapForRank = new HashMap<>();
    private Set<String> rankAppIdPool = new HashSet<>();
    private Set<String> reviewAppSet = new HashSet<>();//review table 中的 app 数

    public TestPairGenerator() {
        mlDbController = new MlDbController();
        //spaCollect();
        spaInReviewTable();
        //比较 spa 获得的应用数与数据库中 review table 中的应用数的差异 finished
    }

    public static void main(String args[]) {
        TestPairGenerator tpg = new TestPairGenerator();
        tpg.generateTestPair();
    }

    public Set<Instance> getTestPairSet() {
        return testPairSet;
    }

    //获得潜在刷榜应用 通过计算排名波动频率
    private void spaCollect() {
        getRankAppInfoFromDb();
        buildAppDataMapForRank();
        System.out.println("不包含的应用");
        int count = 0;
        for (String app : appMapForRank.keySet()) {
            if (!reviewAppSet.contains(app)) {
                System.out.println(app);
                count++;
            }
        }
        System.out.println("不包含的应用数： " + count);
    }

    //获得潜在刷榜应用 直接通过 review table
    private void spaInReviewTable() {
        getAppsFromReviewTable();
        rankAppIdPool = reviewAppSet;
    }

    public void generateTestPair() {
        List<String> appList = new ArrayList<>(rankAppIdPool);
        for (int i = 0; i < appList.size(); i++) {
            for (int j = i + 1; j < appList.size(); j++) {
                String appA = appList.get(i);
                String appB = appList.get(j);
                testPairSet.add(new Instance(appA, appB, "undefined"));
            }
        }
        System.out.println("潜在刷榜应用内的应用对总数：" + testPairSet.size());
    }

    private void getRankAppInfoFromDb() {
        String selectSql = "SELECT * FROM Data.AppInfo Where (rankType in ('topFreeFlowDown','topFreeFlowUp' ,'topPaidFlowDown' ,'topPaidFlowUp')) and date <'2016-04-13' ";
        Statement statement;
        ResultSet rs;
        try {
            statement = mlDbController.connection.createStatement();
            rs = statement.executeQuery(selectSql);
            while (rs.next()) {
                int floatNum = rs.getInt("rankFloatNum");
                if (floatNum >= 150 || floatNum <= (-150)) {
                    AppData appData = new AppData();
                    appData.appId = rs.getString("appId");
                    appData.rankType = rs.getString("rankType");
                    appData.ranking = rs.getInt("ranking");
                    appData.rankFloatNum = rs.getInt("rankFloatNum");
                    appData.date = DataFormat.timestampToMonthDayYear(rs.getTimestamp("date"));
                    appDataRecordListForRank.add((appData));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getAppsFromReviewTable() {
        ResultSet rs;
        try {
            rs = mlDbController.selectAppStmt.executeQuery();
            while (rs.next()) {
                String appid = rs.getString("appid");
                reviewAppSet.add(appid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("review table 包含应用总数: " + reviewAppSet.size());
    }

    private void buildAppDataMapForRank() {
        for (AppData appData : appDataRecordListForRank) {
            if (appMapForRank.containsKey(appData.appId)) {
                appMapForRank.get(appData.appId).add(appData);
            } else {
                List<AppData> newList = new LinkedList<>();
                newList.add(appData);
                appMapForRank.put(appData.appId, newList);
            }
        }
        System.out.println("包含 App 总数 : " + appMapForRank.size());
        Iterator iterator = appMapForRank.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String appId = entry.getKey().toString();
            List<AppData> appDataList = (List) entry.getValue();
            int frequency = rankFrequencyCount(appDataList);
            if (frequency < FREQUENCY)
                iterator.remove();
            else
                rankAppIdPool.add(appId);
        }
        System.out.println("潜在刷榜 App 数 : " + appMapForRank.size());
    }

    private int rankFrequencyCount(List<AppData> entry) {
        int unusualCount = 0;
        for (AppData appData : entry) {
            if (appData.rankFloatNum >= 150 || appData.rankFloatNum <= (-150))
                unusualCount++;
        }
        return unusualCount;
    }

}
