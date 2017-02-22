package MLPart;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/22
 */
public class MlDbController {
    private static final String url = "jdbc:mysql://127.0.0.1/Data";
    private static final String name = "com.mysql.jdbc.Driver";
    private static final String user = "root";
    private static final String password = "root";
    public PreparedStatement insertAppPairStmt;
    public PreparedStatement getAppPairStmt;
    public PreparedStatement getAppInfoStmt;
    public Connection connection = null;
    private String insertAppPairSQL = "insert into Data.AppPair (appA,appB,support,label) values (?,?,?,?)";
    private String getAppPairSQL = "select * from Data.AppPair";
    private String getAppInfoSQL = "select * from Data.AppInfo where appId=?";

    public MlDbController() {
        try {
            Class.forName(name);
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connect Database Success");//connection test
            insertAppPairStmt = connection.prepareStatement(insertAppPairSQL);
            getAppPairStmt = connection.prepareStatement(getAppPairSQL);
            getAppInfoStmt = connection.prepareStatement(getAppInfoSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        MlDbController mlDbController = new MlDbController();
    }
}