package tv.game88.pay.api.dto;

import lombok.Data;

@Data
public class RspMemberInfoWithdraw {
    private String remark;
    private String id;

    private String phone;

    private String vip;
    private String loginTime;
    private String regTime;

    private String accountNow;
    private String codeTotal;
    private String codeNow;

    private String channelCode;
    private String registerType;
    private String loginIp;
    private String ipaddress;
    //线下充值金额
    private String rechargemoney;
    //线下充值金额
    private String usdtrechargemoney;
    //线上金额(一月)
    private String submoney;
    //手动增加金额
    private String rgIncome;
    //平台赠送金额
    private String zsIncome;
    //充值总的金额
    private String totalincom;
    //会员提现次数
    private String wCount;
    //会员提现金额
    private String wSum;
    //彩票异常投注次数
    private String gcount;
    //彩票总投注笔数
    private String gtcount;

    //游戏名称
    private String classTwoname;
    //游戏投注
    private String touZhu;
    //游戏投注盈利
    private String yingLi;
}
