package tv.game88.wallet.api.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
