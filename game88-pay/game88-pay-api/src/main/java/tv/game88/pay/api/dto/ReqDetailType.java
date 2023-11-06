package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.page.PageDomain;
import tv.game88.pay.api.type.WithdrawRechargeType;

import jakarta.validation.constraints.NotBlank;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqDetailType extends PageDomain {
    @Schema( title = "类型" )
    @NotBlank( message = "类型不能为空" )
    private WithdrawRechargeType type;
}
