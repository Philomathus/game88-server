package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReqMemberCardCancel {
    @Schema( title = "银行卡id" )
    private Long cardId;
}
