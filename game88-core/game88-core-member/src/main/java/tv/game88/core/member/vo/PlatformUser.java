package tv.game88.core.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PlatformUser {
    @Schema( name = "会员ID" )
    private String  id;
    @Schema( name = "会员账号" )
    private String  userName;
    @Schema( name = "会员昵称" )
    private String  nickName;
    @Schema( name = "登录密码" )
    private String  password;
    @Schema( name = "会员头像" )
    private String  headImage;
    @Schema( name = "会员vip" )
    private Integer vip;
    @Schema( name = "会员状态" )
    private Integer status;
    @Schema( name = "邀请码" )
    private String  inviterCode;

}
