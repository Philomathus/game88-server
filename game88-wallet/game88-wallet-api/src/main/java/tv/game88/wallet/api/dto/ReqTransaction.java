package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ReqTransaction {
    @Schema( title = "挂单编号" )
    @NotBlank( message = "挂单编号不能为空" )
    private String transactionId;
}
