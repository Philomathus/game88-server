package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.page.PageDomain;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqMemberRecommend extends PageDomain {
    @Schema( title = "邀请码" )
    @NotBlank( message = "邀请码不能为空" )
    private String code;
}
