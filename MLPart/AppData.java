package MLPart;

import java.util.Date;

/**
 * Author: Spikerman < mail4spikerman@gmail.com >
 * Created Date: 17/2/22
 */
public class AppData {
    public String appId;
    public String rankType;
    public int ranking;
    public int rankFloatNum;
    public Date date;
    public String currentVersionReleaseDate;
    public String currentVersion;
    public int userRateCountForCur;
    public int userTotalRateCount;
    public boolean hasNumDecrease = false;
    public double averageDailyRateNum;

    public double averageUserRating;
    public double averageUserRatingForCurrentVersion;
    public double delta;


    public AppData(String appId) {
        this.appId = appId;
    }

    public AppData() {
    }

    // appA.getRateDiff(appB)= dateA-dateB
    public double getRateDiff(AppData appData) {
        //若被减APP评分为0,则说明收集到的评论数还不够,等到突然显示出来记录后,会出现评分激增,此时的激增应该被排除在外
        if (this.currentVersion.equals(appData.currentVersion) && this.averageUserRatingForCurrentVersion != 0) {
            this.delta = this.averageUserRatingForCurrentVersion - appData.averageUserRatingForCurrentVersion;
            return delta;
        } else {
            //System.out.println(this.averageUserRatingForCurrentVersion + " " + appData.averageUserRatingForCurrentVersion);
            return 0;
        }

    }
}
