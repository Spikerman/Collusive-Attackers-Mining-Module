/**
 * Created by chenhao on 5/28/16.
 */
public class Client {
    public static void main(String args[]) {
        AlgoFPGrowth algoFPGrowth = new AlgoFPGrowth();
        String output = "output.txt";
        String input = "result.txt";

        try {
            algoFPGrowth.runAlgorithm(input, output, 5, 20);
            algoFPGrowth.printStats();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Itemsets itemsets = algoFPGrowth.getPatterns();
    }
}
