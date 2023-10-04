package tv.game88.wallet.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ReqPayDepositOrder {
    @NotBlank
    private String s;
    @NotNull
    private Long   t;
    @Size( min = 6, max = 6 )
    @NotNull
    private Long   p;
}
