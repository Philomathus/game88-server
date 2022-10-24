package tv.game88.core.admin.annotation;

import java.lang.annotation.*;

@Inherited
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface AccessLimit {

    /**
     * 限制周期（单位为秒）
     *
     * @return
     */
    int seconds();

    /**
     * 规定周期内限制次数
     *
     * @return
     */
    int maxCount();

}
