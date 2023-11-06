package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

@Data
public class Phone {
    @Schema( title = "手机号" )
    @NotBlank
    @Length( max = 11, min = 11 )
    private String phone;
}
