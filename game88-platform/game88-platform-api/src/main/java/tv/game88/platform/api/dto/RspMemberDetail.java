package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import tv.game88.core.member.dto.RspMember;


@Getter
@Setter
public class RspMemberDetail extends RspMember {
    @Schema( title = "手机号" )
    private String phone;
}
