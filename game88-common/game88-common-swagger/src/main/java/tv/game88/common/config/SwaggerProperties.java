package tv.game88.common.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Swagger 扩展属性
 *
 * @author MengJun
 * @version 1.0.0
 */
@Setter
@Getter
@ToString( callSuper = true )
@ConfigurationProperties(
        // 配置属性前缀
        prefix = "swagger",
        //忽略无效字段
        ignoreInvalidFields = true )
public class SwaggerProperties {


    /**
     * 标题
     */
    private String title = "Swagger 3 Api Documentation";

    /**
     * 版本
     */
    private String version = "1.0.0";

    /**
     * 描述
     */
    private String description = "Spring Boot Plugin Swagger 3 Web Api Documentation";

    /**
     * 服务条款地址
     */
    private String termsOfServiceUrl = "";

    /**
     * 许可证
     */
    private String license = "Apache 2.0";

    /**
     * 许可证地址
     */
    private String licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0";

    /**
     * 名称
     */
    private String contactName = "MengJun";

    /**
     * 邮箱
     */
    private String contactEmail = "mengjun8877@gmail.com";

    /**
     * 地址
     */
    private String contactUrl = "";

    /**
     * 构建 API 信息
     *
     * @return API 信息
     */
    public Info buildApiInfo() {
        return new Info()
                .title( this.title )
                .version( this.version )
                .description( this.description )
                .contact( new Contact().email( contactEmail ).url( contactUrl ).name( contactName ) )
                .termsOfService( termsOfServiceUrl )
                .license( new License().name( license ).url( licenseUrl ) );
    }

}
