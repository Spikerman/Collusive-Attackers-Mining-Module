import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by chenhao on 5/28/16.
 */
public class OutPut {
    public static void main(String args[]) {
        DbController dbController = new DbController();
        FimController fimController = new FimController(dbController);
        fimController.loadCCMapFromDb();
        fimController.buildAppReviewMap(0);
        Map appReviewMap = fimController.appReviewMap;

        try {

            OutPut outPut = new OutPut();
            outPut.BufferedWriterTest(appReviewMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void BufferedWriterTest(Map<String, TreeSet<String>> appReviewMap) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("result.txt"));
        for (Map.Entry entry : appReviewMap.entrySet()) {
            TreeSet<String> appIdSet = (TreeSet) entry.getValue();
            for (String userId : appIdSet) {
                bw.write(userId);
                bw.write(" ");
            }
            bw.newLine();
        }
        bw.flush();
        bw.close();
        System.out.println("写入ok");
    }
}
