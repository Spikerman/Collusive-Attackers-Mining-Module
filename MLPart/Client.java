package MLPart;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/3/22
 */
public class Client {
    public static void main(String args[]) {
        TestPairGenerator tpg = new TestPairGenerator();
        InstanceGenerator ig = new InstanceGenerator();
        tpg.generateTestPair();
        ig.setTestPair(tpg.getTestPairSet());
        ig.instanceAnalysis(true, "test");

    }
}
