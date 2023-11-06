package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.page.PageDomain;

import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqLottery extends PageDomain {
    @Schema( title = "彩票ID" )
    @NotNull(message = "输入有误")
    private Integer id = 0;
}
