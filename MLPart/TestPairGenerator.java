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

    public TestPairGenerator() {
        mlDbController = new MlDbController();
        spaCollect();
    }

    public static void main(String args[]) {
        TestPairGenerator tpg = new TestPairGenerator();
        tpg.generateTestPair();
    }

    public Set<Instance> getTestPairSet() {
        return testPairSet;
    }

    //获得潜在刷榜应用
    private void spaCollect() {
        getRankAppInfoFromDb();
        buildAppDataMapForRank();
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
        System.out.println("总数：" + testPairSet.size());
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
        System.out.println("数据库 App 数 : " + appMapForRank.size());
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
