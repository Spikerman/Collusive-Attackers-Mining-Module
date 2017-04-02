import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Created by chenhao on 3/24/16.
 */
public class DbController {
    private static final String url = "jdbc:mysql://127.0.0.1/Data";
    private static final String name = "com.mysql.jdbc.Driver";
    private static final String user = "root";
    private static final String password = "root";
    public PreparedStatement insertAppPairStmt;
    public Connection connection = null;
    private String insertAppPair = "insert into Data.AppPair (appA,appB,support,label) values (?,?,?,?)";

    public DbController() {
        try {
            Class.forName(name);
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connect Database Success");//connection test
            insertAppPairStmt = connection.prepareStatement(insertAppPair);
        } catch (Exception e) {
            e.printStackTrace();


        }
    }

    public static void main(String args[]) {
        DbController dbController = new DbController();
    }
}
