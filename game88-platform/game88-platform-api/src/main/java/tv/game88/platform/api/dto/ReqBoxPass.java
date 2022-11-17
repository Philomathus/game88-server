package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReqBoxPass {
    @Schema( title = "保险箱/提现密码", requiredMode = Schema.RequiredMode.REQUIRED )
    @NotBlank( message = "密码不能为空" )
    private String boxPass;
}
