package MLPart;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Author: Spikerman
 * Created Date: 17/2/21
 */

//生成用于训练的应用对，各应用对已明确标记是否为 collusive
public class TrainPairGenerator {
    private Set<Instance> appPairSet = new HashSet<>();
    private MlDbController mlDbController;
    private Set<Instance> dbPairRecord = new HashSet<>();// 存储数据库中已有的记录
    private Set<String> idsInAppPair = new HashSet<>(); // 数据库 apppair 表中已有的 id 记录
    private Set<String> idsInAppInfo = new HashSet<>();//从 appinfo 表中读取到的 id 记录集合
    private Set<String> allIdSet = new HashSet<>();

    public TrainPairGenerator() {
        this.mlDbController = new MlDbController();
        getPairFromAppPair();
        getIdFromAppInfo();
        System.out.println(idsInAppInfo.size() + "  " + idsInAppPair.size());
        allIdSet.addAll(idsInAppInfo);
        allIdSet.addAll(idsInAppPair);
        System.out.println(allIdSet.size());
    }

    public static void main(String args[]) {
        TrainPairGenerator apg = new TrainPairGenerator();
        apg.normalPairBuilder();

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


    public void normalPairBuilder() {
        for (int i = 0; i < 600; i++) {
            String app1 = randomPickId();
            String app2 = randomPickId();
            if (app1.equals(app2))
                continue;
            Instance ins = new Instance(app1, app2);
            if (!dbPairRecord.contains(ins)) {
                ins.support = 0;
                ins.label = "normal";
                exportToDb(ins);
            }
        }
    }

    //从 apppair 表获取所有记录
    public void getPairFromAppPair() {
        ResultSet rs;
        try {
            rs = mlDbController.getAppPairStmt.executeQuery();
            while (rs.next()) {
                Instance i = new Instance();
                i.appA = rs.getString("appA");
                i.appB = rs.getString("appB");
                i.support = rs.getInt("support");
                i.label = rs.getString("label");
                dbPairRecord.add(i);
                idsInAppPair.add(i.appA);
                idsInAppPair.add(i.appB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("existed pair size: " + dbPairRecord.size());
    }

    //从 appinfo 表获取所有 id 的集合
    public void getIdFromAppInfo() {
        ResultSet rs;
        try {
            rs = mlDbController.getAppIdStmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("appId");
                idsInAppInfo.add(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("符合条件 id 数： " + idsInAppInfo.size());
    }

    public String randomPickId() {
        int item = new Random().nextInt(allIdSet.size());
        int i = 0;
        for (String id : allIdSet) {
            if (i == item)
                return id;
            i++;
        }
        return null;
    }

}
