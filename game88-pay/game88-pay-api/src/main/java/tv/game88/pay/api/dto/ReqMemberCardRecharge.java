package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ReqMemberCardRecharge {
    @Schema( title = "存款金额" )
    @NotNull( message = "请输入存款金额" )
    private BigDecimal rechargeMoney;
    @Schema( title = "存款人姓名" )
    @NotBlank( message = "请输入存款人姓名" )
    private String     rechargeUserName;
    @Schema( title = "银行卡ID" )
    @NotBlank( message = "请选择银行卡" )
    private String     bankBaseId;
}
