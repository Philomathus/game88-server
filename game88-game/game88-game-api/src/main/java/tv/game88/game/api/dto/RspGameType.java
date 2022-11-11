package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;

@Data
public class RspGameType {
    @Schema( title = "类型ID" )
    private Long   id;
    @Schema( title = "名称" )
    private String name;
    @Schema( title = "图标" )
    private String icon;

    public String getIcon() {
        if ( StringUtils.isNotBlank( icon ) && !icon.startsWith( "http" ) ) {
            return ConfigDomainCacheUtil.me.getDomainOssValue() + icon;
        }
        return icon;
    }
}
