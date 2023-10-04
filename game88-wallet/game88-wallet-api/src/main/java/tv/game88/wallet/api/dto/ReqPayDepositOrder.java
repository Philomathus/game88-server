package tv.game88.wallet.api.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReqPayDepositOrder {
    @NotBlank
    private String s;
    @NotNull
    private Long   t;
    @Length( min = 6, max = 6 )
    @NotNull
    private String p;
}
