package MLPart;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/21
 */
public class Instance {
    public String label;
    public int rves;
    public int rds;
    public int rfs;
    public int support;
    String appA;
    String appB;

    public Instance(String appA, String appB) {
        this.appA = appA;
        this.appB = appB;
        rves = 0;
        rds = 0;
        rfs = 0;
        label = "normal";
    }

    public Instance(String id1, String appB, int support, String label) {
        this.label = label;
        this.support = support;
        this.appA = id1;
        this.appB = appB;
        rves = 0;
        rds = 0;
        rfs = 0;
    }

    public Instance(String appA, String appB, String label) {
        this.label = label;
        this.appA = appA;
        this.appB = appB;
    }

    public Instance() {
    }

    public static void main(String args[]) {
        Set<Instance> set = new HashSet<>();
        Instance i1 = new Instance("abs", "kjl");
        Instance i2 = new Instance("kjl", "abs");
        set.add(i1);
        set.add(i2);
        System.out.println(set.size());
    }

    public int hashCode() {
        return (appA.hashCode() + appB.hashCode()) * 37;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Instance))
            return false;
        Instance instance = (Instance) obj;
        return (this.appA.equals(instance.appA) && this.appB.equals(instance.appB))
                || (this.appA.equals(instance.appB) && this.appB.equals(instance.appA));
    }
}
