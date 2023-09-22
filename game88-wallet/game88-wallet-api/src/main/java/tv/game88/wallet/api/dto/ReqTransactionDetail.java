package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReqTransactionDetail {
    @Schema( title = "买单编号" )
    @NotBlank( message = "买单编号不能为空" )
    private String transDetailId;
}
