package tv.game88.common.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import springfox.documentation.service.Contact;

/**
 * Swagger 联系人
 *
 * @author MengJun
 * @version 1.0.0
 */
@Setter
@Getter
@ToString
public class SwaggerContact {

    /**
     * 名称
     */
    private String name = "MengJun";

    /**
     * 邮箱
     */
    private String email = "MengJun.jiang@qq.com";

    /**
     * 地址
     */
    private String url = "";

    /**
     * 构建联系人
     *
     * @return 返回联系人
     */
    public Contact buildContact() {
        return new Contact(this.name, this.url, this.email);
    }

}
