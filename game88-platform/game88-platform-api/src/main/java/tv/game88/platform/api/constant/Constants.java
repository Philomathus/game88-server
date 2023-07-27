package tv.game88.platform.api.constant;


import org.apache.http.client.HttpClient;
import tv.game88.platform.api.util.YiDunHttpClient;

/**
 *
 * @author rajesh
 */
public abstract class Constants {

    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    public static HttpClient HTTPCLIENT = YiDunHttpClient.createHttpClient(100, 20, 10000, 2000, 2000);

    public static final String URI_SEND_FINGERPRINT = "https://fp-query.dun.163.com/v1/device/query";

    /**
     * SECRET_ID 和 SECRET_KEY 是产品密钥。可以登录易盾官网找到自己的凭证信息。请妥善保管，避免泄露。
     * BUSINESS_ID 为易盾官网申请的应用id
     */
    public static final String SECRET_ID = "6f64efb319ed8e8d610528d97d1a6ad9";
    public static final String SECRET_KEY = "0c79fa62b6a0e0ea31f3dffac81e4c5a";
    public static final String BUSINESS_ID = "ad5a5dec912d76e91c51497feb249aca";

    private Constants() {
        throw new RuntimeException( "Constants.class can't be instantiated" );
    }

}
