package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Phone {
    @Schema( title = "手机号" )
    private String phone;
}
