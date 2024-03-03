package tv.game88.wallet.api.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WalletHistory implements Serializable {

    String id;
    String phone;
    String nickName;
    String headImg;
    String password;
    BigInteger amount;
    String fundPassword;
    Integer isVerified;
    LocalDateTime verifiedTime;
    String realName;
    String idNumber;
    String idFrontPic;
    String idBackPic;
    Integer level;
    LocalDateTime createdTime;
    Integer status;
    String loginIp;
    String deviceId;
    Integer loginDevice;
    String phoneModel;
    LocalDate loginTime;
    String linkUrl;
    BigInteger totalCharge;
    BigInteger totalSale;
    BigInteger buyOrderNum;
    BigInteger sellOrderNum;

}
