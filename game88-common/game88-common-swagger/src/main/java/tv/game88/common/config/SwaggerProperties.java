package tv.game88.common.config;

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
@ToString(callSuper = true)
@ConfigurationProperties(
        // 配置属性前缀
        prefix = "swagger",
        //忽略无效字段
        ignoreInvalidFields = true
)
public class SwaggerProperties extends SwaggerApiInfo {

    /**
     * 扫描基本包
     */
    private String baseScanPackage;

}
