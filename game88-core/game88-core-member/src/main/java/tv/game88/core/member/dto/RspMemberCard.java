package tv.game88.core.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspMemberCard {
    @Schema( title = "编号" )
    private Long    id;
    @Schema( title = "银行账号" )
    private String  bankAccount;
    @Schema( title = "开户名" )
    private String  realName;
    @Schema( title = "开户地址" )
    private String  bankAddress;
    @Schema( title = "是否默认" )
    private Boolean dv;
    @Schema( title = "银行图标地址" )
    private String  bankIcon;
    @Schema( title = "银行名称" )
    private String  bankName;
    @Schema( title = "银行编码" )
    private String  bankCode;
}
