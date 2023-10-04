package tv.game88.wallet.api.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReqPayDepositOrder {
    @NotBlank
    private String s;
    @NotNull
    private Long   t;
    @Min( 5 )
    @Max( 7 )
    @NotNull
    private Long   p;
}
