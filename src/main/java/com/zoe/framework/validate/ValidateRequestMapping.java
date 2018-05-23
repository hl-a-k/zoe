package com.zoe.framework.validate;

import com.zoe.framework.validation.IValidator;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.*;

/**
 * Annotation for mapping HTTP requests onto specific handler
 * methods.
 *
 * <p>Specifically, {@code @ValidateRequestMapping} is a <em>composed annotation</em> that
 * acts as a shortcut for {@code @RequestMapping}.
 *
 *
 * @author Sam Brannen
 * @since 4.3
 * @see GetMapping
 * @see PostMapping
 * @see PutMapping
 * @see DeleteMapping
 * @see PatchMapping
 * @see RequestMapping
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping
public @interface ValidateRequestMapping {

    //region for validate

    /**
     * 验证规则集。
     *
     * @return 验证规则集。
     */
    Validate[] validates() default {};

    /**
     * 验证器列表，接受{@link IValidator}实现类的数组，除了级联外需要处理的额外验证
     */
    Class<? extends IValidator>[] validators() default {};

    /**
     * 作用于Spring AOP时候，用于标示分组验证，作于与属性时不起任何作用
     */
    String[] groups() default {};

    /**
     * 作用于Spring AOP时候，用于标示该参数验证是否启用failfast失败策略
     */
    boolean isFailFast() default true;

    //endregion

    /**
     * Alias for {@link RequestMapping#name}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String name() default "";

    /**
     * Alias for {@link RequestMapping#value}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] value() default {};

    /**
     * Alias for {@link RequestMapping#path}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] path() default {};

    /**
     * Alias for {@link RequestMapping#method}.
     */
    @AliasFor(annotation = RequestMapping.class)
    RequestMethod[] method() default { RequestMethod.GET, RequestMethod.POST };

    /**
     * Alias for {@link RequestMapping#params}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] params() default {};

    /**
     * Alias for {@link RequestMapping#headers}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] headers() default {};

    /**
     * Alias for {@link RequestMapping#consumes}.
     * @since 4.3.5
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] consumes() default {};

    /**
     * Alias for {@link RequestMapping#produces}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] produces() default { MediaType.APPLICATION_JSON_UTF8_VALUE };

}
