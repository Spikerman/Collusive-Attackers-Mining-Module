package MLPart;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/21
 */
public class Instance {
    public String type;
    public int rves;
    public int rds;
    public int rfs;
    String id1;
    String id2;

    public Instance(String id1, String id2) {
        this.id1 = id1;
        this.id2 = id2;
        rves = 0;
        rds = 0;
        rfs = 0;
        type = "normal";
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
        return (id1.hashCode() + id2.hashCode()) * 37;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Instance))
            return false;
        Instance instance = (Instance) obj;
        return (this.id1.equals(instance.id1) && this.id2.equals(instance.id2))
                || (this.id1.equals(instance.id2) && this.id2.equals(instance.id1));
    }
}
