package cn.com.hjack.autobind.converter;

import cn.com.hjack.autobind.ResolvableConverter;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.utils.CastUtils;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


/**
 * 参数化类型转换器，常见的有
 * <ol>
 * <li>Optional
 * <li>ThreadLocal
 * <li>CompletableFuture
 * <li>SoftReference
 * <li>WeakReference
 * <li>Stream
 * <li>待补充
 * </ol> 
 * @author houqq
 * @date: 2025年9月4日
 */
public class ParameterizedTypeValueResolver extends AbstractResolvableConverter {

    public static ParameterizedTypeValueResolver instance = new ParameterizedTypeValueResolver();

    private ParameterizedTypeValueResolver() {

    }
    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        try {
            TypeWrapper genericType = TypeWrappers.getAndResolveGenericType(targetType, 0);
            Class<?> targetClass = targetType.resolve();
            if (targetClass == Optional.class) {
                Result<Object> result = convertGenericTypeValue(source, genericType, targetClass, config);
                if (!result.success()) {
                    return DefaultResult.errorResult(CastUtils.castSafe(Optional.empty()), result.resultMsg());
                } else {
                    return DefaultResult.successResult(CastUtils.castSafe(Optional.ofNullable(result.instance())));
                }
            } else if (targetClass == ThreadLocal.class) {
                Result<Object> result = convertGenericTypeValue(source, genericType, targetClass, config);
                if (!result.success()) {
                    return DefaultResult.errorResult(CastUtils.castSafe(new ThreadLocal<>()), result.resultMsg());
                } else {
                    ThreadLocal<Object> threadLocal = new ThreadLocal<>();
                    threadLocal.set(result.instance());
                    return DefaultResult.successResult(CastUtils.castSafe(threadLocal));
                }
            } else if (targetClass == CompletableFuture.class) {
                Result<Object> result = convertGenericTypeValue(source, genericType, targetClass, config);
                if (!result.success()) {
                    return DefaultResult.errorResult(CastUtils.castSafe(new CompletableFuture<>()), result.resultMsg());
                } else {
                    CompletableFuture<Object> future = new CompletableFuture<>();
                    future.complete(result.instance());
                    return DefaultResult.successResult(CastUtils.castSafe(future));
                }
            } else if (targetClass == SoftReference.class) {
                Result<Object> result = convertGenericTypeValue(source, genericType, targetClass, config);
                if (!result.success()) {
                    return DefaultResult.errorResult(CastUtils.castSafe(new SoftReference<>(null)), result.resultMsg());
                } else {
                    return DefaultResult.successResult(CastUtils.castSafe(new SoftReference<>(result.instance())));
                }
            } else if (targetClass == WeakReference.class) {
                Result<Object> result = convertGenericTypeValue(source, genericType, targetClass, config);
                if (!result.success()) {
                    return DefaultResult.errorResult(CastUtils.castSafe(new WeakReference<>(null)), result.resultMsg());
                } else {
                    return DefaultResult.successResult(CastUtils.castSafe(new WeakReference<>(result.instance())));
                }
            } else if (targetClass == Stream.class) {
                if (source == null) {
                    return DefaultResult.successResult(CastUtils.castSafe(Stream.empty()));
                } else {
                    if (genericType == null || genericType.resolve() == null) {
                        if (source instanceof Collection) {
                            Collection<?> coll = (Collection<?>) source;
                            return DefaultResult.successResult(CastUtils.castSafe(coll.stream()));
                        } else {
                            return DefaultResult.successResult(CastUtils.castSafe(Stream.of(source)));
                        }
                    } else {
                        Class<?> genericClass = genericType.resolve();
                        ResolvableConverter converter = ResolvableConverters.getConverter(genericClass);
                        if (converter == null) {
                            return DefaultResult.errorResult("can not convert source to target, can not find converter");
                        }
                        if (source instanceof Collection) {
                            Stream.Builder<Object> builder = Stream.builder();
                            Collection<Object> coll = CastUtils.castSafe(source);
                            for (Object o : coll) {
                                Result<Object> childResult = converter.convert(o, genericType, config);
                                if (!childResult.success()) {
                                    throw new IllegalStateException(childResult.resultMsg());
                                } else {
                                    builder.add(childResult.instance());
                                }
                            }
                            return DefaultResult.successResult(CastUtils.castSafe(builder.build()));
                        } else {
                            Result<Object> childResult = converter.convert(source, genericType, config);
                            if (!childResult.success()) {
                                throw new IllegalStateException(childResult.resultMsg());
                            } else {
                                return DefaultResult.successResult(CastUtils.castSafe(Stream.of(childResult.instance())));
                            }
                        }
                    }
                }
            } else {
                return DefaultResult.errorResult("can not convert source to target, unsupport target type");
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(e.getMessage());
        }
    }
    private Result<Object> convertGenericTypeValue(Object source, TypeWrapper genericType, Class<?> targetClass, ResolveConfig config) throws Exception {
        ResolvableConverter converter = ResolvableConverters.getConverter(targetClass);
        if (converter == null) {
            return DefaultResult.errorResult("can not convert source to target, can not find converter");
        } else {
            return converter.convert(source, genericType, config);
        }
    }

}
