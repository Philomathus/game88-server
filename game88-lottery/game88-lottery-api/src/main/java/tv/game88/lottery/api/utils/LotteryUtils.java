package tv.game88.lottery.api.utils;

public class LotteryUtils {
    public static int getKindId( int lotteryId ) {
        int kindId;
        if ( lotteryId == 2001 ) {
            kindId = 11;
        } else {
            kindId = lotteryId % 5;
        }
        return kindId;
    }
}
