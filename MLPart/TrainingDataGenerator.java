package MLPart;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/21
 */
public class TrainingDataGenerator {
    public static Set<Instance> insSet = new HashSet<>();

    public static void main(String args[]) {
        dataReader(5, 5);
    }

    public static void dataReader(int clusterId, int support) {
        String fileName = "Cluster%dSupport%d.txt";
        fileName = String.format(fileName, clusterId, support);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            int count = 0;
            while (((line = reader.readLine()) != null)) {
                System.out.println("TAC " + count++);
                String[] userAppStr = line.split("\\|");
                String appStr = userAppStr[1];
                String[] appIdArr = appStr.split(" ");
                for (int i = 0; i < appIdArr.length; i++) {
                    for (int j = i + 1; j < appIdArr.length; j++) {
                        insSet.add(new Instance(appIdArr[i], appIdArr[j]));
                        System.out.println(appIdArr[i] + "  " + appIdArr[j]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
