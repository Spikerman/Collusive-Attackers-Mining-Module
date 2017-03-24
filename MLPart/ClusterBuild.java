package MLPart;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: Spikerman
 * Created Date: 17/3/24
 */
public class ClusterBuild {
    private Map<String, Set<String>> appClusterMap = new HashMap<>();
    private Set<Instance> collusivePairs;

    public ClusterBuild() {
        Classify classifier = new Classify();
        classifier.classifyInstance();
        collusivePairs = classifier.collusivePairs;
    }

    public static void main(String args[]) {
        ClusterBuild cb = new ClusterBuild();
        cb.clusterMapBuild();
        System.out.print("haha");
    }

    public void clusterMapBuild() {
        for (Instance ins : collusivePairs) {
            String appA = ins.appA;
            String appB = ins.appB;
            if (appClusterMap.containsKey(appA)) {
                appClusterMap.get(appA).add(appB);
            } else {
                Set<String> cluster = new HashSet<>();
                cluster.add(appA);
                cluster.add(appB);
                appClusterMap.put(appA, cluster);
            }
            if (appClusterMap.containsKey(appB)) {
                appClusterMap.get(appA).add(appA);
            } else {
                Set<String> cluster = new HashSet<>();
                cluster.add(appA);
                cluster.add(appB);
                appClusterMap.put(appB, cluster);
            }
        }
    }

    public void clusterCombine(double rate) {
        boolean hasDuplicateSet = false;
        Object[] outerIdSet = appClusterMap.keySet().toArray();
        Object[] innerIdSet = appClusterMap.keySet().toArray();

        for (int i = 0; i < outerIdSet.length; i++) {
            for (int j = i + 1; j < innerIdSet.length; j++) {
                String outerId = outerIdSet[i].toString();
                String innerId = innerIdSet[j].toString();
                Set<String> outerSet;
                Set<String> innerSet;
                if (appClusterMap.containsKey(outerId) && appClusterMap.containsKey(innerId)) {
                    outerSet = appClusterMap.get(outerId);
                    innerSet = appClusterMap.get(innerId);

                    int outerGroupSize = outerSet.size();
                    int innerGroupSize = innerSet.size();

                    if (outerSet.containsAll(innerSet)
                            || innerSet.containsAll(outerSet)
                            || enableCombine(innerSet, outerSet, rate)) {
                        if (outerGroupSize > innerGroupSize) {
                            outerSet.addAll(innerSet);
                            appClusterMap.remove(innerId);

                        } else {
                            innerSet.addAll(outerSet);
                            appClusterMap.remove(outerId);
                        }
                        hasDuplicateSet = true;
                    }
                }
            }
        }
        if (hasDuplicateSet)
            clusterCombine(rate);
    }

    private boolean enableCombine(Set<String> setA, Set<String> setB, double rate) {
        Set<String> unionSet = Sets.union(setA, setB);
        Set<String> intersectionSet = Sets.intersection(setA, setB);
        double unionSize = unionSet.size();
        double intersectionSize = intersectionSet.size();
        return (intersectionSize / unionSize) >= rate;
    }


}
