package tv.game88.wallet.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PlatformUser {
    @Schema( title = "会员ID" )
    private String  id;
    @Schema( title = "会员昵称" )
    private String  nickName;
    @Schema( title = "会员状态" )
    private Integer status;
    @Schema( title = "是否实名认证" )
    private Integer isVerified;
    @Schema( title = "用户等级" )
    private Integer level;

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String password;
}