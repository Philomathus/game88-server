package tv.game88.core.member.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;

import java.util.HashMap;
import java.util.Map;

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

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String password;

    public Map<String, Object> toUserInfoMap() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put( "user_id", this.getId() );
        userMap.put( "nick_name", this.getNickName() );
        if ( StringUtils.isNotBlank( this.getHeadImg() ) ) {
            userMap.put( "head_image",
                    ConfigDomainCacheUtil.me.getDomainOssValue() + "/88lm/publicImage/head" + headImg + ".png" );
        }
        userMap.put( "user_level", this.getVip() );
        return userMap;
    }
}
