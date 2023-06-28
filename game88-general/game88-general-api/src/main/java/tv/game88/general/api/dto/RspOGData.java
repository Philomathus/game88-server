package tv.game88.general.api.dto;

import lombok.Data;

@Data
public class RspOGData {
    private String id; // 注单ID
    private String membername; // 会员ID
    private String gamename; // 游戏名称
    private String bettingcode; // 注单号
    private String bettingdate;// 下注时间
    private String gameid; // 桌台号
    private String roundno; // 局号
    private Object game_information; // 游戏结果详情
    private String playerCards; //
    private String bankerCards;
    private String result; // 结果
    private String bet; // 下注区域
    private String winloseresult; // 输赢结果
    private String bettingamount; // 下注金额
    private String validbet; // 有效投注
    private String winloseamount; // 输赢金额
    private String balance; // 余额
    private String currency; // 货币
}
