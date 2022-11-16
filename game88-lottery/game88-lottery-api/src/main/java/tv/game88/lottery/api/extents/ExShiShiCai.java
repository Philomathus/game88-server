package tv.game88.lottery.api.extents;


import com.lottery.common.dto.LocalMethod;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class ExShiShiCai {

    public static Map<String,Integer> weightableMap = new HashMap<>();
    //methodID:methods
    public static final Map<String, LocalMethod> methodsMap = new HashMap<>();
    //赔率
    public static final Map<String, BigDecimal> oddsMap = new HashMap<>();

    static {
        weightableMap.put("0",100);
        weightableMap.put("1",100);
        weightableMap.put("2",100);
        weightableMap.put("3",100);
        weightableMap.put("4",100);
        weightableMap.put("5",100);
        weightableMap.put("6",100);
        weightableMap.put("7",100);
        weightableMap.put("8",100);
        weightableMap.put("9",100);

    }

    public static BigDecimal  handle(String methodId,String[] officialSpl, BigDecimal chip, String betSelect){
        BigDecimal prize = BigDecimal.ZERO;
        String[] betarrs = betSelect.split("&");
        String des = "";
        LocalMethod method = methodsMap.get(methodId);
        if(method==null){
            log.error("非法投注:"+methodId);
            return  BigDecimal.ZERO;
        }
        switch (methodsMap.get(methodId).getName()){
            case "万位VS个位":
                String tarCode = officialSpl[0];
                String tail = officialSpl[officialSpl.length - 1];
                des = longHuHe(tarCode,tail);
                for(String bt:betarrs){
                    if(!des.equals(bt)){
                     continue;
                    }
                    prize = prize.add(chip.multiply(oddsMap.get(bt)));

                    break;

                }
                break ;
            case "万位":

                for(String bt:betarrs){

                    //单双
                    des = getFirstDanShuang(officialSpl[0]);

                    if(des.equals(bt)){
                        prize = prize.add(chip.multiply(oddsMap.get(bt)));
                        continue;
                    }

                    //大小
                    des = getFirstDaXiao(officialSpl[0]);
                    if(des.equals(bt)){
                        prize = prize.add(chip.multiply(oddsMap.get(bt)));
                        continue;
                    }
                    //质和
                    des = getFirstZhiHe(officialSpl[0]);
                    if(des.equals(bt)){
                        prize = prize.add(chip.multiply(oddsMap.get(bt)));
                        continue;
                    }


                }
                break ;
            case "佰位":

                for(String bt:betarrs){
                    if(officialSpl[2].equals(bt)){
                        prize = prize.add(chip.multiply(oddsMap.get(bt)));
                        break;
                    }

                }
                break;

        }

        return  prize;

    }

    public static String longHuHe(String head,String end){
        int com = head.compareTo(end);
        if(com>0){
           return "龙";
        }else if(com<0){
            return "虎";
        }else{
            return "和";
        }

    }

    public static String  getFirstDanShuang(String first){

        if(Integer.parseInt(first)%2==0){
            return "双";
        }
        return "单";
    }

    public static String  getFirstDaXiao(String first){

        return Integer.parseInt(first)>=5?"大":"小";

    }

    public static String  getFirstZhiHe(String first){
        boolean contains = Arrays.asList("1", "2", "3", "5", "7").contains(first);
        return contains? "质":"合";
    }

    public static String  getZongDanShuang(List<String> list){
        Integer total = list.stream().mapToInt(Integer::parseInt).sum();
        if(total%2==0){
            return  "和双";
        }
        return  "和单";
    }

    public static String  getZongDanShuang(Integer total){
        if(total%2==0){
            return  "和双";
        }
        return  "和单";
    }

    public static String  getZongDaXiao(List<String> list){
        Integer total = list.stream().mapToInt(Integer::parseInt).sum();
        if(total>=12){
            return "和大";
        }
        return "和小";
    }

    public static String  getZongDaXiao(Integer total){
        if(total>=12){
            return "和大";
        }
        return "和小";
    }

    public static String concatBetString(Map<String,BigDecimal> betMap){

        String [] keys = {"0","1","2","3","4","5","6","7","8","9","单","双","大","小","质","合","龙","虎","和"};

        String [] values = new String[keys.length];

        for (int i = 0; i < keys.length; i++) {
            values[i] = betMap.getOrDefault(keys[i], BigDecimal.ZERO).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        }

        return String.format("0:%s-1:%s-2:%s-3:%s-4:%s-5:%s-6:%s-7:%s-8:%s-9:%s-单:%s-双:%s-大:%s-小:%s-质:%s-合:%s-龙:%s-虎:%s-和:%s", values);
    }

    public static BigDecimal coutPrize(List<String> list,Map<String,BigDecimal> peiMap){
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        //第一球两面
        paijiangTotal = paijiangTotal.add(peiMap.get(getFirstDanShuang(list.get(0))));
        paijiangTotal = paijiangTotal.add(peiMap.get(getFirstDaXiao(list.get(0))));
        paijiangTotal = paijiangTotal.add(peiMap.get(getFirstZhiHe(list.get(0))));
        //佰位
        paijiangTotal = paijiangTotal.add(peiMap.get(list.get(2)));

        //第一球VS第五球
        paijiangTotal = paijiangTotal.add(peiMap.get(longHuHe(list.get(0),list.get(list.size()-1))));

        return paijiangTotal;
    }
}
