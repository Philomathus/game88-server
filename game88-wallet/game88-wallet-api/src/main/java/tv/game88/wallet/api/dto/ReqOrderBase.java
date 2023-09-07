package tv.game88.wallet.api.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReqOrderBase {
    @NotNull( message = "商户号不能为空" )
    private Long   merchantId;
    @NotBlank( message = "商户订单号不能为空" )
    @Length( min = 10, max = 50 )
    private String orderNo;
    @NotBlank( message = "签名不能为空" )
    private String sign;
}
