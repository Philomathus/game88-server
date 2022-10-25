package tv.game88.common.security.annotation;

import java.lang.annotation.*;

/**
 * 匿名访问不鉴权注解
 *
 * @author mengJun
 */
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface Anonymous {}
