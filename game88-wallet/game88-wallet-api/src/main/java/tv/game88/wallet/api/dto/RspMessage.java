package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspMessage {
    @Schema( title = "消息ID" )
    private Long    id;
    @Schema( title = "消息标题" )
    private String  title;
    @Schema( title = "消息内容" )
    private String  content;
    @Schema( title = "是否已读" )
    private Boolean isRead;
}
