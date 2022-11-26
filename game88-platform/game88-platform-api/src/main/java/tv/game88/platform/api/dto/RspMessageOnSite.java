package tv.game88.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RspMessageOnSite {
    @Schema( title = "主键" )
    private Long          id;
    @Schema( title = "信息标题" )
    private String        title;
    @Schema( title = "内容" )
    private String        content;
    @Schema( title = "发布时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
}
