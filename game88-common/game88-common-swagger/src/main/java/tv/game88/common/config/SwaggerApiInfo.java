package tv.game88.common.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;

/**
 * Swagger API 信息
 *
 * @author MengJun
 * @version 1.0.0
 */
@Setter
@Getter
@ToString
public class SwaggerApiInfo {

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
    private String description = "Spring Boot Plugin Swagger 2 Web Api Documentation";

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
     * 联系人对象
     */
    private SwaggerContact contact = new SwaggerContact();

    /**
     * 构建 API 信息
     *
     * @return API 信息
     */
    public ApiInfo buildApiInfo() {
        return new ApiInfoBuilder()
                .title(this.title)
                .version(this.version)
                .description(this.description)
                .termsOfServiceUrl(this.termsOfServiceUrl)
                .license(this.license)
                .licenseUrl(this.licenseUrl)
                .contact(this.contact.buildContact())
                // .extensions()
                .build();
    }

}
