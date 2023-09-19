package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReqTransaction {
    @Schema( title = "挂单编号" )
    @NotBlank
    private String transactionId;
}
