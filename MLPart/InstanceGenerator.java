package MLPart;

import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.util.*;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/22
 */
public class InstanceGenerator {
    public static int adjustDayDiff = 5;
    private Set<Instance> appPairSet = new HashSet<>();
    private Set<String> appIdSet = new HashSet<>();
    private Map<String, List<AppData>> appMap = new HashMap<>();
    private Map<String, Map<Date, Double>> rateRecordMap = new HashMap<>();
    private Map<String, Map<Date, Integer>> reviewRecordMap = new HashMap<>();
    private Map<String, Double> avgReviewMap = new HashMap<>();
    private MlDbController mlDbController;

    public InstanceGenerator() {
        mlDbController = new MlDbController();
    }

    public static void main(String args[]) {
        InstanceGenerator ig = new InstanceGenerator();
        ig.retrieveFromDb();
        ig.appMapConstruct();
        ig.rateRecordMapBuilder();
        ig.analysis();
        System.out.println();
    }

    //retrieve app pair record from AppPair Scheme
    public void retrieveFromDb() {
        ResultSet rs;
        try {
            rs = mlDbController.getAppPairStmt.executeQuery();
            while (rs.next()) {
                Instance i = new Instance();
                i.appA = rs.getString("appA");
                i.appB = rs.getString("appB");
                i.support = rs.getInt("support");
                i.label = rs.getString("label");
                appPairSet.add(i);
                appIdSet.add(i.appA);
                appIdSet.add(i.appB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(appPairSet.size());
    }

    //todo 建立HashMap<String,List<AppData>>
    public void appMapConstruct() {
        ResultSet rs;
        try {
            for (String id : appIdSet) {
                mlDbController.getAppInfoStmt.setString(1, id);
                rs = mlDbController.getAppInfoStmt.executeQuery();
                List<AppData> list = new ArrayList<>();
                while (rs.next()) {
                    AppData appData = new AppData();
                    appData.appId = rs.getString("appId");
                    appData.rankType = rs.getString("rankType");
                    appData.currentVersion = rs.getString("currentVersion");
                    appData.currentVersionReleaseDate = rs.getString("currentVersionReleaseDate");
                    appData.userRateCountForCur = rs.getInt("userRatingCountForCurrentVersion");
                    appData.userTotalRateCount = rs.getInt("userRatingCount");
                    appData.averageUserRating = Double.parseDouble(rs.getString("averageUserRating"));
                    appData.averageUserRatingForCurrentVersion = Double.parseDouble(rs.getString("averageUserRatingForCurrentVersion"));
                    appData.date = DataFormat.timestampToMonthDayYear(rs.getTimestamp("date"));
                    list.add(appData);
                }
                appMap.put(id, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //todo List中存在多条同一天的记录的处理 --》用Set存储AppData,并对其中的date做 hash 和 equal 处理
    public void rateRecordMapBuilder() {
        Iterator iterator = appMap.entrySet().iterator();
        DateComparator dateComparator = new DateComparator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String appId = (String) entry.getKey();
            List<AppData> appList = (List) entry.getValue();
            Collections.sort(appList, dateComparator);
            HashMap<Date, Double> rateDiffRecordMap = new HashMap<>();
            HashMap<Date, Integer> reviewDiffRecordMap = new HashMap<>();
            int totalDiffAmount = 0;
            Set<Date> dateSet = new HashSet<>();
            for (int i = 1; i < appList.size(); i++) {
                dateSet.add(appList.get(i).date);//todo 暂时记录真实的 date 数
                AppData nextDayAppData = appList.get(i);
                AppData curDayAppData = appList.get(i - 1);
                double rateDiff = nextDayAppData.getRateDiff(curDayAppData);
                int reviewDiff;
                if (nextDayAppData.userTotalRateCount - curDayAppData.userTotalRateCount > 0)
                    reviewDiff = nextDayAppData.userTotalRateCount - curDayAppData.userTotalRateCount;
                else
                    reviewDiff = 0;
                totalDiffAmount += reviewDiff;

                reviewDiffRecordMap.put(nextDayAppData.date, reviewDiff);
                rateDiffRecordMap.put(nextDayAppData.date, rateDiff);
            }

            double avgNum = (double) totalDiffAmount / (double) dateSet.size();
            avgReviewMap.put(appId, avgNum);
            reviewRecordMap.put(appId, reviewDiffRecordMap);
            rateRecordMap.put(appId, rateDiffRecordMap);
        }
    }

    public void analysis() {
        for (Instance ins : appPairSet) {
            int rds = 0;
            int rves = 0;
            String appA = ins.appA;
            String appB = ins.appB;
            Map<Date, Double> rateMapA = rateRecordMap.get(appA);
            Map<Date, Double> rateMapB = rateRecordMap.get(appB);
            Map<Date, Integer> reviewMapA = reviewRecordMap.get(appA);
            Map<Date, Integer> reviewMapB = reviewRecordMap.get(appB);
            Set<Date> dateSetA = rateMapA.keySet();
            Set<Date> dateSetB = rateMapB.keySet();
            Set<Date> shareDateSet = (Set) Sets.intersection(dateSetA, dateSetB);
            for (Date date : shareDateSet) {
                Double rateDiffA = rateMapA.get(date);
                Double rateDiffB = rateMapB.get(date);

                if (rateDiffA * rateDiffB > 0) {
                    rds++;
                } else {
                    rds += approxEquals(rateMapA, rateMapB, date);
                }

                int reviewDiffA = reviewMapA.get(date);
                int reviewDiffB = reviewMapB.get(date);
                double avgNumA = avgReviewMap.get(appA);
                double avgNumB = avgReviewMap.get(appB);
                if (reviewDiffA > avgNumA && reviewDiffB > avgNumB)
                    rves++;
            }
            if (rds != 0 || rves != 0)
                System.out.println(appA + "  " + appB + "  " + rds + "  " + rves);
        }
    }

    private int approxEquals(Map<Date, Double> outerMap, Map<Date, Double> innerMap, Date date) {
        Double outerRateDiff;
        Double innerRateDiff;
        Double innerRateDiff2;
        int count = 0;
        for (int i = 1; i < adjustDayDiff; i++) {
            outerRateDiff = outerMap.get(date);
            innerRateDiff = innerMap.get(DataFormat.adjustDay(date, i));
            innerRateDiff2 = innerMap.get(DataFormat.adjustDay(date, -i));
            if ((innerRateDiff != null && innerRateDiff * outerRateDiff > 0)
                    || innerRateDiff2 != null && innerRateDiff2 * outerRateDiff > 0) {
                return ++count;
            }
        }
        return count;
    }


}
