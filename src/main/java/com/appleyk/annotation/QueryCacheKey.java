package com.appleyk.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 注解 QueryCacheKey 是参数级别的注解，用来标注要查询数据的主键，会和上面的nameSpace组合做缓存的key值
 * @author yukun24@126.com
 * @blob   http://blog.csdn.net/appleyk
 * @date   2018年2月28日-下午2:01:52
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
public @interface QueryCacheKey {

}
