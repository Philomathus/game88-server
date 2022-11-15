package tv.game88.lottery.api.extents;


import com.lottery.common.dto.LocalMethod;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class ExSanChe {

    public static Map<String,Integer> weightableMap = new HashMap<>();
    //methodID:methods
    public static final Map<String, LocalMethod> methodsMap = new HashMap<>();
    //赔率
    public static final Map<String, BigDecimal> oddsMap = new HashMap<>();

    static {
        weightableMap.put("01",100);
        weightableMap.put("02",100);
        weightableMap.put("03",100);
        weightableMap.put("04",100);
        weightableMap.put("05",100);
        weightableMap.put("06",100);
        weightableMap.put("07",100);
        weightableMap.put("08",100);
        weightableMap.put("09",100);
        weightableMap.put("10",100);
    }

    public static BigDecimal  handle(String methodId,String[] officialSpl, BigDecimal chip, String betSelect){
        BigDecimal prize = BigDecimal.ZERO;
        String[] betarrs = betSelect.split("&");
        LocalMethod method = methodsMap.get(methodId);
        if(method==null){
            log.error("非法投注:"+methodId);
            return  BigDecimal.ZERO;
        }
        switch (methodsMap.get(methodId).getName()){
            case "冠军单码":
                String tarCode = officialSpl[0];
                for(String bt:betarrs){
                    if(!tarCode.equals(bt)){
                     continue;
                    }
                    prize = prize.add(chip.multiply(oddsMap.get(bt)));

                    break;

                }
                break ;
            case "冠军两面":
                String des = "";

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


                }
                break ;
            case "冠亚和":
                int total = Integer.parseInt(officialSpl[0])+Integer.parseInt(officialSpl[1]);

                for(String bt:betarrs){

                    //单双
                    des = getZongDanShuang(total);
                    if(des.equals(bt)){
                        prize = prize.add(chip.multiply(oddsMap.get(bt)));
                        continue;
                    }

                    //大小
                    des = getZongDaXiao(total);
                    if(des.equals(bt)){
                        prize = prize.add(chip.multiply(oddsMap.get(bt)));
                        continue;
                    }


                }

                break;

        }

        return  prize;

    }

    public static String  getFirstDanShuang(String first){

        if(Integer.parseInt(first)%2==0){
            return "双";
        }
        return "单";
    }

    public static String  getFirstDaXiao(String first){

        if(Integer.parseInt(first)>=6){
            return "大";
        }
        return "小";
    }

    public static String  getZongDanShuang(Integer total){
        if(total%2==0){
            return  "和双";
        }
        return  "和单";
    }


    public static String  getZongDaXiao(Integer total){
        if(total>=12){
            return "和大";
        }
        return "和小";
    }

    public static String concatBetString(Map<String,BigDecimal> betMap){
        String sp = "-";
        String betCountinfo ="";
        String bt ="01";
        betCountinfo = betCountinfo.concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="02";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="03";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="04";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="05";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="06";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="07";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="08";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="09";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="10";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="和单";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="和双";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="和大";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="和小";
        betCountinfo = betCountinfo.concat(sp).concat(bt).concat(":").concat(betMap.get(bt).toString());
        bt ="单";
        betCountinfo = betCountinfo.concat(sp).concat("A单").concat(":").concat(betMap.get(bt).toString());
        bt ="双";
        betCountinfo = betCountinfo.concat(sp).concat("A双").concat(":").concat(betMap.get(bt).toString());
        bt ="大";
        betCountinfo = betCountinfo.concat(sp).concat("A大").concat(":").concat(betMap.get(bt).toString());
        bt ="小";
        betCountinfo = betCountinfo.concat(sp).concat("A小").concat(":").concat(betMap.get(bt).toString());

        return betCountinfo;
    }

    public static BigDecimal coutPrize(List<String> list,Map<String,BigDecimal> peiMap){
        BigDecimal paijiangTotal = BigDecimal.ZERO;
        //冠军单码
        paijiangTotal = paijiangTotal.add(peiMap.get(list.get(0)));

        //冠军两面
        paijiangTotal = paijiangTotal.add(peiMap.get(getFirstDanShuang(list.get(0))));
        paijiangTotal = paijiangTotal.add(peiMap.get(getFirstDaXiao(list.get(0))));
        //冠亚和
        int total = Integer.parseInt(list.get(0))+Integer.parseInt(list.get(1));
        paijiangTotal = paijiangTotal.add(peiMap.get(getZongDanShuang(total)));
        paijiangTotal = paijiangTotal.add(peiMap.get(getZongDaXiao(total)));
        return paijiangTotal;
    }
}
