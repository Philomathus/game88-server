package tv.game88.game.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReqMemberGameData {
    /**
     * 选择日期
     */
    private String[] selectDate;

    private String startTime;

    private String endTime;

    private String tableLast;

    /**
     * 账号
     */
    private String account;

    /**
     * 代理编号
     */
    private String agent;

    /**
     * 子平台名称
     */
    private String sonPlatformName;

    private String gameId;

    private Integer platformId;

    private List<String> platformIds = new ArrayList<>();
    private List<String> agents      = new ArrayList<>();
}
