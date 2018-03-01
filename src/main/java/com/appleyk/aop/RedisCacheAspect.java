package com.appleyk.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.appleyk.annotation.DeleteCache;
import com.appleyk.annotation.QueryCache;
import com.appleyk.annotation.QueryCacheKey;
import com.appleyk.result.CacheNameSpace;

@Aspect
@Component

/**
 * 利用AOP配合注解，实现redis缓存的写入和删除
 * @author yukun24@126.com
 * @blob   http://blog.csdn.net/appleyk
 * @date   2018年3月1日-下午1:35:30
 */
public class RedisCacheAspect {

	static Logger logger = LoggerFactory.getLogger(ControllerInterceptor.class);

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 是否开启redis缓存，将查询的结果写入value
	 */
	@Value("${spring.redis.cache.on}")
	private boolean isOn;

	/**
	 * 定义拦截规则：拦截所有@QueryCache注解的方法 -- 查询。
	 */
	@Pointcut("@annotation(com.appleyk.annotation.QueryCache)")
	public void queryCachePointcut() {
	}

	/**
	 * 拦截器具体实现
	 * 
	 * @param point
	 * @return
	 * @throws Throwable
	 */
	@Around("queryCachePointcut()")
	public Object InterceptorByQuery(ProceedingJoinPoint point) throws Throwable {
		long beginTime = System.currentTimeMillis();
		if (!isOn) {
			// 如果不开启redis缓存的话，直接走原方法进行查询
			Object object = point.proceed();
			return object;
		}

		System.err.println("AOP 缓存切面处理 >>>> start ");
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod(); // 获取被拦截的方法
		// 拿到方法上标注的注解的namespace的值
		CacheNameSpace cacheType = method.getAnnotation(QueryCache.class).nameSpace();
		String key = null;
		int i = 0;

		// 循环所有的参数
		for (Object value : point.getArgs()) {

			MethodParameter methodParam = new SynthesizingMethodParameter(method, i);
			Annotation[] paramAnns = methodParam.getParameterAnnotations();
			// 循环参数上所有的注解
			for (Annotation paramAnn : paramAnns) {
				if (paramAnn instanceof QueryCacheKey) { //
					QueryCacheKey requestParam = (QueryCacheKey) paramAnn;
					key = cacheType.name() + "_" + value; // 取到QueryCacheKey的标识参数的值
				}
			}
			i++;
		}

		/**
		 * 如果没有参数的话，设置Key值
		 */
		if (key == null) {
			key = cacheType.name();
		}

		// 获取不到key值，抛异常
		if (StringUtils.isEmpty(key))
			throw new Exception("缓存key值不存在");

		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		boolean hasKey = redisTemplate.hasKey(key);
		if (hasKey) {

			// 缓存中获取到数据，直接返回。
			Object object = operations.get(key);
			System.err.println("本次查询缓存命中,从缓存中获取到数据 >>>> key = " + key);
			System.err.println("AOP 缓存切面处理 >>>> end 耗时：" + (System.currentTimeMillis() - beginTime) + "ms");
			return object;
		}

		// 缓存中没有数据，调用原始方法查询数据库
		Object object = point.proceed();

		operations.set(key, object, 30, TimeUnit.SECONDS); // 设置超时时间30s

		System.err.println("本次查询缓存未命中,DB取到数据并存入缓存 >>>> key =" + key);
		System.err.println("AOP 缓存切面处理 >>>> end 耗时：" + (System.currentTimeMillis() - beginTime) + "ms");
		// redisTemplate.delete(key);
		return object;

	}

	/**
	 * 定义拦截规则：拦截所有@DeleteCache注解的方法 -- 用于修改表数据时，删除redis缓存中的key值。
	 * 也可以使用切面表达式execution(* com.appleyk.*..*(..)) 切和更新数据相关的方法
	 */
	@Pointcut("@annotation(com.appleyk.annotation.DeleteCache)")
	public void deleteCachePointcut() {
	}

	/**
	 * 拦截器具体实现
	 * 
	 * @param point
	 * @return
	 * @throws Throwable
	 */
	@Around("deleteCachePointcut()")
	public Object InterceptorBySave(ProceedingJoinPoint point) throws Throwable {

		long beginTime = System.currentTimeMillis();
		if (!isOn) {
			// 如果不开启redis缓存的话，直接走原方法进行查询
			Object object = point.proceed();
			return object;
		}
		System.err.println("AOP 缓存切面处理 【清除key】>>>> start ");
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod(); // 获取被拦截的方法

		// 拿到方法上标注的注解的namespace的值
		CacheNameSpace cacheType = method.getAnnotation(DeleteCache.class).nameSpace();

		String key = cacheType.name();
		ValueOperations<String, Object> operations = redisTemplate.opsForValue();
		boolean hasKey = redisTemplate.hasKey(key);
		if (hasKey) {

			System.err.println("key存在，执行删除 >>>> key = " + key);
			/**
			 * 删除key
			 */
			redisTemplate.delete(key);
		}
		System.err.println("AOP 缓存切面处理 【清除key】>>>> end 耗时：" + (System.currentTimeMillis() - beginTime) + "ms");
		Object object = point.proceed();
		return object;

	}

}
