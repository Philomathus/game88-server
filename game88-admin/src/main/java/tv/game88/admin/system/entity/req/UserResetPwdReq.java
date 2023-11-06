package tv.game88.admin.system.entity.req;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class UserResetPwdReq {
    /**
     * 用户ID
     */
    @NotNull
    private Long userId;

    /**
     * 用户密码
     */
    @NotBlank
    private String password;

    /**
     * otp验证码
     */
    @NotNull
    @Range( min = 100000, max = 999999 )
    private Integer otpCode;
}
