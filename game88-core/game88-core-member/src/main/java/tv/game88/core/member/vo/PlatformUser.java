package tv.game88.core.member.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PlatformUser {
    @ApiModelProperty( value = "会员ID" )
    private String  id;
    @ApiModelProperty( value = "会员账号" )
    private String  userName;
    @ApiModelProperty( value = "会员昵称" )
    private String  nickName;
    @ApiModelProperty( value = "登录密码" )
    private String  password;
    @ApiModelProperty( value = "会员头像" )
    private String  headImage;
    @ApiModelProperty( value = "会员vip" )
    private Integer vip;
    @ApiModelProperty( value = "会员状态" )
    private Integer status;
    @ApiModelProperty( value = "邀请码" )
    private String  inviterCode;

}
