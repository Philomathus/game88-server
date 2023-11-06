package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ReqBoxPass {
    @Schema( title = "保险箱/提现密码", required = true )
    @NotBlank( message = "密码不能为空" )
    private String boxPass;
}
