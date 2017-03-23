package MLPart;

import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.util.*;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/22
 */

//输出 Instance 到数据库中，每个 Instance 包含应用的 ID 以及各相似度下的对应值
public class InstanceGenerator {
    private int adjustDayDiff = 5;
    private Set<Instance> instanceSet = new HashSet<>();
    private Set<String> appIdSet = new HashSet<>();
    private Map<String, List<AppData>> appMap = new HashMap<>();
    private Map<String, Map<Date, Double>> rateRecordMap = new HashMap<>();//日期对应评分变化值
    private Map<String, Map<Date, Integer>> reviewRecordMap = new HashMap<>();//日期对应当日评论数量变化值
    private Map<String, Map<Date, Integer>> rankingRecordMap = new HashMap<>();
    private Map<String, Double> avgReviewMap = new HashMap<>();
    private MlDbController mlDbController;

    public InstanceGenerator() {
        mlDbController = new MlDbController();
    }

    public static void main(String args[]) {
        InstanceGenerator ig = new InstanceGenerator();
        ig.getTrainPairFromDb();
        ig.instanceAnalysis(false, "");
        System.out.println();
    }

    public Set<Instance> getInstances() {
        return instanceSet;
    }

    //retrieve training app pair record from AppPair Scheme
    private void getTrainPairFromDb() {
        ResultSet rs;
        try {
            rs = mlDbController.getAppPairStmt.executeQuery();
            while (rs.next()) {
                Instance i = new Instance();
                i.appA = rs.getString("appA");
                i.appB = rs.getString("appB");
                i.support = rs.getInt("support");
                i.label = rs.getString("label");
                instanceSet.add(i);
                appIdSet.add(i.appA);
                appIdSet.add(i.appB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(instanceSet.size());
    }

    public void setTestPair(Set<Instance> entry) {
        for (Instance ins : entry) {
            instanceSet.add(ins);
            appIdSet.add(ins.appA);
            appIdSet.add(ins.appB);
        }
    }


    private void appMapBuilder() {
        ResultSet rs;
        try {
            for (String id : appIdSet) {
                mlDbController.getAppInfoStmt.setString(1, id);
                rs = mlDbController.getAppInfoStmt.executeQuery();
                List<AppData> list = new ArrayList<>();
                Set<Date> dateSet = new HashSet<>();
                while (rs.next()) {
                    AppData appData = new AppData();
                    appData.appId = rs.getString("appId");
                    appData.rankType = rs.getString("rankType");
                    appData.rankFloatNum = rs.getInt("rankFloatNum");
                    appData.currentVersion = rs.getString("currentVersion");
                    appData.currentVersionReleaseDate = rs.getString("currentVersionReleaseDate");
                    appData.userRateCountForCur = rs.getInt("userRatingCountForCurrentVersion");
                    appData.userTotalRateCount = rs.getInt("userRatingCount");
                    appData.averageUserRating = Double.parseDouble(rs.getString("averageUserRating"));
                    appData.averageUserRatingForCurrentVersion = Double.parseDouble(rs.getString("averageUserRatingForCurrentVersion"));
                    appData.date = DataFormat.timestampToMonthDayYear(rs.getTimestamp("date"));

                    if (!dateSet.contains(appData.date)) {
                        list.add(appData);
                    } else {
                        if (!appData.rankType.equals("update")) {
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).date.equals(appData.date)) {
                                    list.remove(i);
                                }
                            }
                            list.add(appData);
                        }
                    }
                    dateSet.add(appData.date);
                }
                appMap.put(id, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //
    private void recordMapBuilder() {
        Iterator iterator = appMap.entrySet().iterator();
        DateComparator dateComparator = new DateComparator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String appId = (String) entry.getKey();
            List<AppData> appList = (List) entry.getValue();
            appList.sort(dateComparator);
            Map<Date, Double> rateDiffRecordMap = new TreeMap<>();
            Map<Date, Integer> reviewDiffRecordMap = new TreeMap<>();
            Map<Date, Integer> rankingTypeRecordMap = new TreeMap<>();
            int totalReviewAmount = 0;
            Set<Date> dateSet = new HashSet<>();
            for (int i = 1; i < appList.size(); i++) {
                dateSet.add(appList.get(i).date);// todo 暂时记录真实的 date 数
                AppData nextDayAppData = appList.get(i);
                AppData curDayAppData = appList.get(i - 1);
                double rateDiff = nextDayAppData.getRateDiff(curDayAppData);
                int reviewDiff;
                if (nextDayAppData.userTotalRateCount - curDayAppData.userTotalRateCount > 0)
                    reviewDiff = nextDayAppData.userTotalRateCount - curDayAppData.userTotalRateCount;
                else
                    reviewDiff = 0;
                totalReviewAmount += reviewDiff;
                reviewDiffRecordMap.put(nextDayAppData.date, reviewDiff);
                rateDiffRecordMap.put(nextDayAppData.date, rateDiff);
            }

            //ranking
            for (AppData ad : appList) {
                rankingTypeRecordMap.put(ad.date, ad.rankFloatNum);
            }

            double avgNum = (double) totalReviewAmount / (double) dateSet.size();
            avgReviewMap.put(appId, avgNum);
            reviewRecordMap.put(appId, reviewDiffRecordMap);
            rateRecordMap.put(appId, rateDiffRecordMap);
            rankingRecordMap.put(appId, rankingTypeRecordMap);
        }
    }

    //计算各Instance中的相似度值
    public void instanceAnalysis(boolean needExport, String db) {
        appMapBuilder();
        recordMapBuilder();
        for (Instance ins : instanceSet) {
            int rds = 0;
            int rves = 0;
            int rfs = 0;
            String appA = ins.appA;
            String appB = ins.appB;
            Map<Date, Double> rateDateMapA = rateRecordMap.get(appA);
            Map<Date, Double> rateDateMapB = rateRecordMap.get(appB);
            Map<Date, Integer> reviewDateMapA = reviewRecordMap.get(appA);
            Map<Date, Integer> reviewDateMapB = reviewRecordMap.get(appB);
            Map<Date, Integer> rankingDateMapA = rankingRecordMap.get(appA);
            Map<Date, Integer> rankingDateMapB = rankingRecordMap.get(appB);
            Set<Date> dateSetA = rateDateMapA.keySet();
            Set<Date> dateSetB = rateDateMapB.keySet();
            Set<Date> shareDateSet = (Set) Sets.intersection(dateSetA, dateSetB);
            for (Date date : shareDateSet) {
                //rating
                Double rateDiffA = rateDateMapA.get(date);
                Double rateDiffB = rateDateMapB.get(date);
                if (rateDiffA * rateDiffB > 0) {
                    rds++;
                } else {
                    rds += approxEquals(rateDateMapA, rateDateMapB, date);
                }
                //review
                int reviewDiffA = reviewDateMapA.get(date);
                int reviewDiffB = reviewDateMapB.get(date);
                double avgNumA = avgReviewMap.get(appA);
                double avgNumB = avgReviewMap.get(appB);
                if (reviewDiffA > avgNumA && reviewDiffB > avgNumB)
                    rves++;

            }
            //ranking
            Set<Date> rdateSetA = rankingDateMapA.keySet();
            Set<Date> rdateSetB = rankingDateMapB.keySet();
            Set<Date> shareDateSet2 = (Set) Sets.intersection(rdateSetA, rdateSetB);
            for (Date date : shareDateSet2) {
                int rankFloatA = rankingDateMapA.get(date);
                int rankFloatB = rankingDateMapB.get(date);
                if (rankFloatA * rankFloatB > 0)
                    rfs++;
            }
            ins.rds = rds;
            ins.rfs = rfs;
            ins.rves = rves;
            if (needExport) {
                if (db.equals("train")) {
                    exportTrainInsToDb(ins);
                } else if (db.equals("test")) {
                    exportTestInsToDb(ins);
                } else
                    System.out.println("wrong database");
            }
            System.out.println(ins.label + "  " + appA + "  " + appB + "  " + rds + "  " + rves + "  " + rfs);
        }
    }

    private void exportTrainInsToDb(Instance instance) {
        try {
            mlDbController.insertTrainInsStmt.setInt(1, instance.rves);
            mlDbController.insertTrainInsStmt.setInt(2, instance.rds);
            mlDbController.insertTrainInsStmt.setInt(3, instance.rfs);
            mlDbController.insertTrainInsStmt.setString(4, instance.label);
            mlDbController.insertTrainInsStmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportTestInsToDb(Instance instance) {
        try {
            mlDbController.insertTestInsStmt.setInt(1, instance.rves);
            mlDbController.insertTestInsStmt.setInt(2, instance.rds);
            mlDbController.insertTestInsStmt.setInt(3, instance.rfs);
            mlDbController.insertTestInsStmt.setString(4, instance.appA);
            mlDbController.insertTestInsStmt.setString(5, instance.appB);
            mlDbController.insertTestInsStmt.setString(6, instance.label);
            mlDbController.insertTestInsStmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
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
