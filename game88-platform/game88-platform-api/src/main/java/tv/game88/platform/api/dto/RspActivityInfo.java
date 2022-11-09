package tv.game88.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RspActivityInfo {
    @Schema( title = "活动ID" )
    private Long          id;
    @Schema( title = "图标" )
    private String        icon;
    @Schema( title = "标题" )
    private String        title;
    @Schema( title = "发布时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Schema( title = "活动明细" )
    private String        content;
    @Schema( title = "false停用true启用" )
    private boolean       effect;
    @Schema( title = "0=活动详情 1=跳转链接" )
    private Integer       type;
    @Schema( title = "图标跳转链接" )
    private String        url;
    private Long          typeId;
}
