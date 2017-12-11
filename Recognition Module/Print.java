package MLPart;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/3/26
 */
public class Print {
    public static void printEachGroupSize(Map<String, Set<String>> clusterMap) {
        Map map = clusterMap;
        Set set = map.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Set group = (Set) entry.getValue();
            System.out.println(group.size());
        }
    }

    //打印 size 大于指定值的组的各组APP数量
    public static Set<String> printEachGroupSize(Map<String, Set<String>> candidateCluster, int size) {
        Set entrySet = candidateCluster.entrySet();
        int count = 0;
        Set<String> totalApps = new HashSet<>();
        Iterator iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Set group = (Set) entry.getValue();
            if (group.size() >= size) {
                System.out.println(group.size());
                count += group.size();
                totalApps.addAll(group);
            }
        }
        System.out.println("Total count : " + count);
        System.out.println("Total distinct app : " + totalApps.size());
        return totalApps;
    }
}
