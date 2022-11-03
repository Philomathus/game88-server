package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReqMemberCard {
    @Schema( title = "姓名" )
    private String realName;
    @Schema( title = "银行账号" )
    private String bankAccount;
    @Schema( title = "开户地址" )
    private String bankAddress;
    @Schema( title = "银行卡ID" )
    private Long   bankId;
}