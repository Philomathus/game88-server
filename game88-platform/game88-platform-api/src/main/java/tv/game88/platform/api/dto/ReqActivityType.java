package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class ReqActivityType {
    @Schema( title = "类型id" )
    @NotNull( message = "类型id不能为空" )
    private Long id;
}
