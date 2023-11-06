package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.StringUtils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ReqMemberCardRecharge {
    @Schema( title = "存款金额", required = true )
    @NotNull( message = "请输入存款金额" )
    private BigDecimal rechargeMoney;
    @Schema( title = "存款人姓名", required = true )
    @NotBlank( message = "请输入存款人姓名" )
    private String     rechargeUserName;
    @Schema( title = "银行卡ID", required = true )
    @NotBlank( message = "请选择银行卡" )
    private String     bankBaseId;

    @Schema( title = "会员请求IP" )
    private String ip;

    public String getIp() {
        if ( StringUtils.isBlank( ip ) ) {
            return ServletUtil.getIp();
        }
        return ip;
    }
}
