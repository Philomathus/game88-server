package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ReqMemberCardWithdraw {
    @Schema( title = "提现金额" )
    @NotNull( message = "请输入提现金额" )
    private BigDecimal withdrawMoney = BigDecimal.ZERO;
    @Schema( title = "提款密码" )
    @NotBlank( message = "请输入提款密码" )
    private String     withdrawalPass;
    @Schema( title = "会员绑定银行卡ID" )
    @NotNull( message = "请选择提现卡" )
    private Long       memberCardId;
}
