package tv.game88.wallet.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

@Data
public class RspMember {
    @Schema( title = "登录令牌" )
    private String     token;
    @Schema( title = "会员ID" )
    private String     id;
    @Schema( title = "昵称" )
    private String     nickName;
    @Schema( title = "状态" )
    private Integer    status;
    @Schema( title = "手机号" )
    private String     phone;
    @Schema( title = "余额" )
    private BigDecimal amount;
    @Schema( title = "是否实名认证" )
    private Integer    isVerified;
    @Schema( title = "信用等级" )
    private Integer    creditRating;
    @Schema( title = "累积充值金额" )
    private BigDecimal totalCharge;
    @Schema( title = "累积出售金额" )
    private BigDecimal totalSale;

    @Schema( title = "登录密码", hidden = true )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String password;

    public String getPhone() {
        if ( StringUtils.isNotBlank( phone ) ) {
            return phone.substring( 0, 3 ) + "****" + phone.substring( 7 );
        }
        return phone;
    }
}
