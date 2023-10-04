package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.wallet.api.type.WalletPayMethodEnum;

import javax.validation.constraints.*;

@Data
public class ReqPayMethod {
    @Schema( title = "支付类型" )
    @NotNull( message = "支付类型不能为空" )
    private WalletPayMethodEnum methodType;

    @Schema( title = "微信实名姓名" )
    private String realName;

    @Schema( title = "支付宝账号或银行卡号" )
    private String account;

    @Schema( title = "银行ID" )
    private Long bankId;

    @Schema( title = "所在地-省" )
    @NotBlank( message = "所在地-省不能为空" )
    private String payAddrProvince;

    @Schema( title = "所在地-市" )
    @NotBlank( message = "所在地-市不能为空" )
    private String payAddrCity;

    @Schema( title = "收款码图片地址" )
    private String payPicAddr;

    @Schema( title = "资金密码" )
    @NotNull( message = "资金密码不能为空" )
    @Size( min = 6, max = 6 )
    private Long fundPassword;
}
