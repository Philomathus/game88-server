package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.core.member.dto.RspMemberCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RspWithdrawBank {
    @Schema( title = "银行卡信息" )
    private List<RspMemberCard>      memberCardList;
    @Schema( title = "自助体现信息" )
    private RspMemberWithdrawLogInfo rspWithdrawInfo;
    @Schema( title = "特殊银行信息" )
    private Map<String, Long>     specialBankInfoMap = new HashMap<>();
}
