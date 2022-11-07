package tv.game88.core.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PlatformUser {
    @Schema( title = "会员ID" )
    private String  id;
    @Schema( title = "会员昵称" )
    private String  nickName;
    @Schema( title = "会员头像" )
    private String  headImg;
    @Schema( title = "会员vip" )
    private Integer vip;
    @Schema( title = "会员状态" )
    private Integer status;
    @Schema( title = "注册类型", description = "0游客 1会员" )
    private Integer registerType;
}
