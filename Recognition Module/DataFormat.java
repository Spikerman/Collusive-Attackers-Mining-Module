package MLPart;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/22
 */
public class DataFormat {
    private static final SimpleDateFormat monthDayYearFormatter = new SimpleDateFormat("MMMMM dd, yyyy");

    public static Date timestampToMonthDayYear(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        } else {
            return new Date(timestamp.getYear(), timestamp.getMonth(), timestamp.getDate());
        }
    }

    //取指定日期想前或向后几天的Date对象,正数往后推,负数往前移动
    public static Date adjustDay(Date date, int num) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, num);
        return calendar.getTime();
    }
}
