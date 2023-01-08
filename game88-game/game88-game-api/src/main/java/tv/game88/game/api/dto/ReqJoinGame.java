package tv.game88.game.api.dto;

import lombok.Builder;
import lombok.Data;
import tv.game88.game.api.type.EnumGameCategory;

import java.math.BigDecimal;

@Data
@Builder
public class ReqJoinGame {
    private String des;
    private String md5;
    private String agent;
    private String linecode;
    private String apiUrl;
    private String recordUrl;
    private String kindId;

    private EnumGameCategory gameCategory;

    private String     memberId;
    private String     gameMemberId;
    private BigDecimal transferMoney;

    private String  orderId;
    private Long    platformId;
    private String  token;
    private String  ip;
    private Integer dev;

    private String gameUrl;
}
