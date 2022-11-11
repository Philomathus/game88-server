package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;

@Data
public class RspGameInfo {
    @Schema( title = "系统编号" )
    private Long    id;
    @Schema( title = "游戏名称" )
    private String  name;
    @Schema( title = "图标" )
    private String  icon;
    @Schema( title = "是否维护" )
    private Boolean maintain;
    @Schema( title = "是否推荐" )
    private Boolean recommend;
    @Schema( title = "是否大图标" )
    private Boolean largeIcon;

    public String getIcon() {
        if ( StringUtils.isNotBlank( icon ) && !icon.startsWith( "http" ) ) {
            return ConfigDomainCacheUtil.me.getDomainOssValue() + icon;
        }
        return icon;
    }
}
