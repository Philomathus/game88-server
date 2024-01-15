package tv.game88.core.admin.constant;

public class RecordConstants {
    public record ReqResetUserOtpSecret(Long userId, Integer otpAuthCode) {}

    public record ReqBoundOtpSecret(Integer otpAuthCode, String otpAuthKey, String otpAuthName) {}
}
