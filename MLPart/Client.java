package MLPart;

/**
 * Author: Spikerman
 * Created Date: 17/3/22
 */

//输出计算后的 Instance 记录到数据库中
public class Client {
    public static void main(String args[]) {
        TestPairGenerator tpg = new TestPairGenerator();
        InstanceGenerator ig = new InstanceGenerator();
        tpg.generateTestPair();
        ig.setTestPair(tpg.getTestPairSet());
        ig.instanceAnalysis(true, "test");
    }
}
