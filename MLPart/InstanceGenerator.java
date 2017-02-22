package MLPart;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/22
 */
public class InstanceGenerator {
    private Set<Instance> appPairSet = new HashSet<>();
    private Set<String> appIdSet = new HashSet<>();
    private MlDbController mlDbController;

    public InstanceGenerator() {
        mlDbController = new MlDbController();
    }

    public static void main(String args[]) {
        InstanceGenerator ig = new InstanceGenerator();
        ig.retrieveFromDb();
        ig.analysis();
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

    //todo 建立HashMap<String,HashMap<Date,Record>>
    public void analysis() {
        ResultSet rs;
        try {
            for (String id : appIdSet) {
                mlDbController.getAppInfoStmt.setString(1, id);
                rs = mlDbController.getAppInfoStmt.executeQuery();
                while (rs.next()) {
                    AppData appData = new AppData();
                    appData.appId = rs.getString("appId");
                    appData.rankType = rs.getString("rankType");
                    appData.currentVersion = rs.getString("currentVersion");
                    appData.currentVersionReleaseDate = rs.getString("currentVersionReleaseDate");
                    appData.userRateCountForCur = rs.getInt("userRatingCountForCurrentVersion");
                    appData.userTotalRateCount = rs.getInt("userRatingCount");
                    appData.date = DataFormat.timestampToMonthDayYear(rs.getTimestamp("date"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
