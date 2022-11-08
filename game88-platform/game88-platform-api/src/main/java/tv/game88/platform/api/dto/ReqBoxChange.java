package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ReqBoxChange {
    @Schema( title = "保险箱转入量" )
    @NotNull(message = "保险箱转入量不能为空")
    private BigDecimal addAccount;
}
