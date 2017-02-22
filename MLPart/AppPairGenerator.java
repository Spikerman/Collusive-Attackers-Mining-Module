package MLPart;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/21
 */
public class AppPairGenerator {
    private Set<Instance> appPairSet = new HashSet<>();
    private MlDbController mlDbController;

    public AppPairGenerator() {
        this.mlDbController = new MlDbController();
    }

    public static void main(String args[]) {
        AppPairGenerator apg = new AppPairGenerator();
        apg.dataReader(1, 4);

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

    public AppPairGenerator dataReader(int clusterId, int support) {
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
        return this;
    }

}
