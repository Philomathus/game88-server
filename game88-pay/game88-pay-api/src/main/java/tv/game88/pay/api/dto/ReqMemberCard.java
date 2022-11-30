package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReqMemberCard {
    @Schema( title = "姓名" )
    @NotBlank( message = "姓名为空" )
    private String  realName;
    @Schema( title = "银行账号" )
    @NotBlank( message = "银行卡号为空" )
    @Length( max = 100, message = "请输入正确的银行卡号" )
    private String  bankAccount;
    @Schema( title = "开户地址" )
    private String  bankAddress;
    @Schema( title = "银行卡ID" )
    @NotNull( message = "请选择银行类型" )
    private Integer bankId;
}