import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by chenhao on 3/24/16.
 */
public class DbController {
    public static final String url = "jdbc:mysql://127.0.0.1/Data";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "root";
    public Connection connection = null;

    public DbController() {
        try {
            Class.forName(name);
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connect Database Success");//connection test
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        DbController dbController = new DbController();
    }
}
