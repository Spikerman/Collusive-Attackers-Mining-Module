package MLPart;

import java.util.Comparator;
import java.util.Date;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/22
 */
public class DateComparator implements Comparator<AppData> {
    public int compare(AppData app1, AppData app2) {
        Date date1 = app1.date;
        Date date2 = app2.date;
        if (date1.after(date2))
            return 1;
        else if (date2.after(date1))
            return -1;
        else
            return 0;
    }
}
