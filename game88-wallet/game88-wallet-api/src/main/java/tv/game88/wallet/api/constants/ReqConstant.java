package tv.game88.wallet.api.constants;

import io.swagger.v3.oas.annotations.media.Schema;

public class ReqConstant {

    public record ReqSetPasswd( @Schema( title = "密码" ,requiredMode = Schema.RequiredMode.REQUIRED ) String password ,
                                @Schema( title = "confirm密码",requiredMode = Schema.RequiredMode.REQUIRED ) String confirmPassword){}

    public record ReqResetFundPasswd( @Schema( title = "资金老密码", requiredMode = Schema.RequiredMode.REQUIRED ) String fundOldPass ,
                                      @Schema( title = "资金新密码", requiredMode = Schema.RequiredMode.REQUIRED ) String fundNewPass ){}
}
