package MLPart;

import com.google.common.collect.Sets;

import java.util.*;

/**
 * Author: Spikerman
 * Created Date: 17/3/24
 */
public class ClusterGenerator {
    public Set<Set<String>> clusterSet = new HashSet<>();// set of Targeted App Cluster
    private MlDbController mlDbController = new MlDbController();
    private RemoteDbController remoteDbController = new RemoteDbController();
    private Map<String, Set<String>> appClusterMap = new HashMap<>();
    private Set<Instance> collusivePairs;

    public ClusterGenerator() {
        Classify classifier = new Classify();
        classifier.classifyInstance();
        collusivePairs = classifier.collusivePairs;
    }

    public static void main(String args[]) {
        int clusterLimit = 20;
        ClusterGenerator cb = new ClusterGenerator();
        cb.clusterMapBuild();
        System.out.println("递归合并前candidate cluster数 : " + cb.appClusterMap.size());
        cb.clusterCombine(0.6);
        System.out.println("递归合并后candidate cluster数 : " + cb.appClusterMap.size());
        System.out.println("========================= Candidate Cluster ================================ ");
        System.out.println("candidate size 限制 : " + clusterLimit);
        Print.printEachGroupSize(cb.appClusterMap, clusterLimit);
        cb.buildAppClusterSet(clusterLimit);
        //cb.exportToLocalDb();
        //cb.exportToRemoteDb();
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

    private void exportClusterToLocalDb(int clusterId, String appId) {
        try {
            mlDbController.insertCCStmt.setInt(1, clusterId);
            mlDbController.insertCCStmt.setString(2, appId);
            mlDbController.insertCCStmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportClusterToRemoteDb(int clusterId, String appId) {
        try {
            remoteDbController.insertCCStmt.setInt(1, clusterId);
            remoteDbController.insertCCStmt.setString(2, appId);
            remoteDbController.insertCCStmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportToLocalDb() {
        System.out.println("============== Export To Local Database ============");
        Iterator clusterIterator = clusterSet.iterator();
        Iterator appIdIterator;
        int clusterId = 1;
        while (clusterIterator.hasNext()) {
            Set idSet = (Set) clusterIterator.next();
            appIdIterator = idSet.iterator();
            while (appIdIterator.hasNext()) {
                String appId = (String) appIdIterator.next();
                exportClusterToLocalDb(clusterId, appId);
            }
            clusterId++;
        }
        System.out.println("============== Export End ============");
    }

    public void exportToRemoteDb() {
        System.out.println("============== Export To Remote Database ============");
        Iterator clusterIterator = clusterSet.iterator();
        Iterator appIdIterator;
        int clusterId = 1;
        while (clusterIterator.hasNext()) {
            Set idSet = (Set) clusterIterator.next();
            appIdIterator = idSet.iterator();
            while (appIdIterator.hasNext()) {
                String appId = (String) appIdIterator.next();
                exportClusterToRemoteDb(clusterId, appId);
            }
            clusterId++;
        }
        System.out.println("============== Export End ============");
    }


    public void buildAppClusterSet(int size) {
        Object[] groupArray = appClusterMap.entrySet().toArray();
        int totalCount = 0;
        for (int i = 0; i < groupArray.length; i++) {
            Map.Entry entry = (Map.Entry) groupArray[i];
            Set<String> ids = (Set) entry.getValue();
            totalCount += ids.size();
            if (ids.size() >= size)
                clusterSet.add(ids);
        }
        System.out.println("avg cluster size : " + (float) totalCount / (float) groupArray.length);
    }


}
