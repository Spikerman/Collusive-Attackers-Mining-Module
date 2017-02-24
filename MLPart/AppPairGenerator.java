package MLPart;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/21
 */
public class AppPairGenerator {
    private Set<Instance> appPairSet = new HashSet<>();
    private MlDbController mlDbController;
    private Set<Instance> oldPairSet = new HashSet<>();// 存储数据库中已有的记录
    private Set<String> oldIdSet = new HashSet<>(); // 数据库 apppair 表中已有的 id 记录
    private Set<String> AppInfoIdSet = new HashSet<>();//从 appinfo 表中读取到的 id 记录集合

    public AppPairGenerator() {
        this.mlDbController = new MlDbController();
        retrieveRecordFromDb();
        retrieveAppIdFromDb();
    }

    public static void main(String args[]) {
        AppPairGenerator apg = new AppPairGenerator();
        apg.normalPairBuilder();
        //apg.dataReader(1, 4);

    }

    public void exportToDb(String appA, String appB, int support, String label) {
        try {
            mlDbController.insertAppPairStmt.setString(1, appA);
            mlDbController.insertAppPairStmt.setString(2, appB);
            mlDbController.insertAppPairStmt.setInt(3, support);
            mlDbController.insertAppPairStmt.setString(4, label);
            mlDbController.insertAppPairStmt.executeUpdate();
            System.out.println(appA + "  " + appB + "  " + support + "  " + label);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void exportToDb(Instance instance) {
        try {
            mlDbController.insertAppPairStmt.setString(1, instance.appA);
            mlDbController.insertAppPairStmt.setString(2, instance.appB);
            mlDbController.insertAppPairStmt.setInt(3, instance.support);
            mlDbController.insertAppPairStmt.setString(4, instance.label);
            mlDbController.insertAppPairStmt.executeUpdate();
            System.out.println(instance.appA + "  " + instance.appB + "  " + instance.support + "  " + instance.label);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void dataReader(int clusterId, int support) {
        String fileName = "Cluster%dSupport%d.txt";
        fileName = String.format(fileName, clusterId, support);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while (((line = reader.readLine()) != null)) {
                String[] userAppStr = line.split("\\|");
                String appStr = userAppStr[1];
                String[] appIdArr = appStr.split(" ");
                for (int i = 0; i < appIdArr.length; i++) {
                    for (int j = i + 1; j < appIdArr.length; j++) {
                        appPairSet.add(new Instance(appIdArr[i], appIdArr[j], support, "collusive"));
                    }
                }
            }
            System.out.println("Total Amount: " + appPairSet.size());
            for (Instance i : appPairSet) {
                exportToDb(i.appA, i.appB, i.support, i.label);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //todo 新建一个id set, random Pick 的来源改为从 oldIdSet 与 appInfo Id Set 的差集
    public void normalPairBuilder() {
        for (int i = 0; i < 1; i++) {
            String app1 = randomPickId();
            String app2 = randomPickId();
            if (app1.equals(app2))
                continue;
            Instance ins = new Instance(app1, app2);
            if (!oldPairSet.contains(ins)) {
                ins.support = 0;
                ins.label = "normal";
                exportToDb(ins);
            }
        }
    }

    public void retrieveRecordFromDb() {
        ResultSet rs;
        try {
            rs = mlDbController.getAppPairStmt.executeQuery();
            while (rs.next()) {
                Instance i = new Instance();
                i.appA = rs.getString("appA");
                i.appB = rs.getString("appB");
                i.support = rs.getInt("support");
                i.label = rs.getString("label");
                oldPairSet.add(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("existed pair size: " + oldPairSet.size());
    }

    public void retrieveAppIdFromDb() {
        ResultSet rs;
        try {
            rs = mlDbController.getAppIdStmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("appId");
                oldIdSet.add(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("符合条件 id 数： " + oldIdSet.size());
    }

    public String randomPickId() {
        int item = new Random().nextInt(oldIdSet.size());
        int i = 0;
        for (String id : oldIdSet) {
            if (i == item)
                return id;
            i++;
        }
        return null;
    }

}
