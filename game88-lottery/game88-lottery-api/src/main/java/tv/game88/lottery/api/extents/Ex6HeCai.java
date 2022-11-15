package tv.game88.lottery.api.extents;

import com.lottery.common.dto.LocalMethod;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Ex6HeCai {
    //methodID:methods
    public static final Map<String, LocalMethod> methodsMap = new HashMap<>();
    //赔率
    public static final Map<String, BigDecimal> oddsMap = new HashMap<>();
    public static Map<String, Integer> weightableMap = new HashMap<>();

    static {
        for (int i = 1; i <= 49; i++) {
            weightableMap.put(String.format("%02d", i), 100);
        }
    }

    public static BigDecimal handle(String methodId, String[] officialSpl, BigDecimal chip, String betSelect) {
        BigDecimal prize = BigDecimal.ZERO;
        String tarCode = officialSpl[officialSpl.length - 1];
        String[] betarrs = betSelect.split("&");
        String des = "";
        LocalMethod method = methodsMap.get(methodId);
        if (method == null) {
            log.error("非法投注:" + methodId);
            return BigDecimal.ZERO;
        }
        switch (method.getName()) {
            case "特码两面":
                for (String bt : betarrs) {
                    //单双
                    des = getDanShuang(tarCode);
                    if (des.equals(bt)) {
                        prize = prize.add(chip.multiply(oddsMap.get(bt)));
                        continue;
                    }
                    //大小
                    des = getDaXiao(tarCode);
                    if (des.equals(bt)) {
                        prize = prize.add(chip.multiply(oddsMap.get(bt)));
                        continue;
                    }
                }
                break;
            case "特码生肖":
                for (String bt : betarrs) {
                    //单双
                    des = getShengXiao(tarCode);
                    if (des.equals(bt)) {
                        prize = prize.add(chip.multiply(oddsMap.get(bt)));
                    }
                }
                break;
            case "特码色波":
                for (String bt : betarrs) {

                    des = getColor(tarCode);
                    if (des.equals(bt)) {
                        prize = prize.add(chip.multiply(oddsMap.get(bt)));
                    }
                }
                break;
        }
        return prize;
    }

    public static String getDanShuang(String first) {
        if (Integer.parseInt(first) % 2 == 0) {
            return "双";
        }
        return "单";
    }

    public static String getDaXiao(String first) {
        if (Integer.parseInt(first) >= 25) {
            return "大";
        }
        return "小";
    }

    public static String getColor(String code) {
        switch (code) {
            case "01":
            case "02":
            case "07":
            case "08":
            case "12":
            case "13":
            case "18":
            case "19":
            case "23":
            case "24":
            case "29":
            case "30":
            case "34":
            case "35":
            case "40":
            case "45":
            case "46":
                return "红";
            case "03":
            case "04":
            case "09":
            case "10":
            case "14":
            case "15":
            case "20":
            case "25":
            case "26":
            case "31":
            case "36":
            case "37":
            case "41":
            case "42":
            case "47":
            case "48":
                return "蓝";
        }
        return "绿";
    }

    public static void main(String[] args) {
        System.out.println( getShengXiao("32") );
    }

    public static String getShengXiao(String code) {
        switch (Integer.parseInt(code)) {
            case 1:
            case 13:
            case 25:
            case 37:
            case 49:
                return "虎";
            case 2:
            case 14:
            case 26:
            case 38:
                return "牛";
            case 3:
            case 15:
            case 27:
            case 39:
                return "鼠";
            case 4:
            case 16:
            case 28:
            case 40:
                return "猪";
            case 5:
            case 17:
            case 29:
            case 41:
                return "狗";
            case 6:
            case 18:
            case 30:
            case 42:
                return "鸡";
            case 7:
            case 19:
            case 31:
            case 43:
                return "猴";
            case 8:
            case 20:
            case 32:
            case 44:
                return "羊";
            case 9:
            case 21:
            case 33:
            case 45:
                return "马";
            case 10:
            case 22:
            case 34:
            case 46:
                return "蛇";
            case 11:
            case 23:
            case 35:
            case 47:
                return "龙";
            case 12:
            case 24:
            case 36:
            case 48:
                return "兔";
        }
        return "猪";

    }

    public static String concatBetString(Map<String, BigDecimal> betMap) {
        String sp = "-";
        String betCountinfo = "";
        String bt = "鼠";
        betCountinfo = betCountinfo.concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "牛";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "虎";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "兔";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "龙";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "蛇";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "马";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "羊";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "猴";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "鸡";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "狗";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "猪";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "大";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "小";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "单";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "双";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "红";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "绿";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt = "蓝";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());

        return betCountinfo;
    }

    public static BigDecimal coutPrize(List<String> list, Map<String, BigDecimal> peiMap) {
        String tarCode = list.get(list.size() - 1);
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        paijiangTotal = paijiangTotal.add(peiMap.get(getDanShuang(tarCode)));
        paijiangTotal = paijiangTotal.add(peiMap.get(getDaXiao(tarCode)));

        paijiangTotal = paijiangTotal.add(peiMap.get(getShengXiao(tarCode)));

        paijiangTotal = paijiangTotal.add(peiMap.get(getColor(tarCode)));

        return paijiangTotal;
    }
}
