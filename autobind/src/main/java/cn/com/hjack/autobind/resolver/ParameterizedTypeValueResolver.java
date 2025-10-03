/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.factory.TypeValueResolvers;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.validation.DefaultResult;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


/**
 * @ClassName: ParameterizedTypeValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年9月4日
 *
 */
public class ParameterizedTypeValueResolver extends AbstractTypeValueResolver {

    public static ParameterizedTypeValueResolver instance = new ParameterizedTypeValueResolver();

    private ParameterizedTypeValueResolver() {

    }

    @Override
    protected Result<Object> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config)
            throws Exception {
        if (targetType == null || targetType.resolve() == null) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "target type can not be null");
        }
        try {
            TypeWrapper genericType = TypeWrappers.getAndResolveGenericType(targetType, 0);
            Class<?> targetCls = targetType.resolve();
            if (targetCls == Optional.class) {
                Object convertValue = this.convertGenericTypeValue(source, genericType, targetCls, config);
                if (convertValue == null) {
                    if (source == null) {
                        return DefaultResult.defaultSuccessResult(Optional.empty());
                    } else {
                        return DefaultResult.defaultSuccessResult(Optional.ofNullable(source));
                    }
                } else {
                    return DefaultResult.defaultSuccessResult(Optional.ofNullable(convertValue));
                }
            } else if (targetCls == ThreadLocal.class) {
                Object convertValue = this.convertGenericTypeValue(source, genericType, targetCls, config);
                if (convertValue == null) {
                    if (source == null) {
                        return DefaultResult.defaultSuccessResult(new ThreadLocal<>());
                    } else {
                        ThreadLocal<Object> threadLocal = new ThreadLocal<>();
                        threadLocal.set(source);
                        return DefaultResult.defaultSuccessResult(threadLocal);
                    }
                } else {
                    ThreadLocal<Object> threadLocal = new ThreadLocal<>();
                    threadLocal.set(convertValue);
                    return DefaultResult.defaultSuccessResult(threadLocal);
                }
            } else if (targetCls == CompletableFuture.class) {
                Object convertValue = this.convertGenericTypeValue(source, genericType, targetCls, config);
                if (convertValue == null) {
                    if (source == null) {
                        return DefaultResult.defaultSuccessResult(new CompletableFuture<>());
                    } else {
                        CompletableFuture<Object> future = new CompletableFuture<>();
                        future.complete(source);
                        return DefaultResult.defaultSuccessResult(future);
                    }
                } else {
                    CompletableFuture<Object> future = new CompletableFuture<>();
                    future.complete(convertValue);
                    return DefaultResult.defaultSuccessResult(future);
                }
            } else if (targetCls == SoftReference.class) {
                Object convertValue = convertGenericTypeValue(source, genericType, targetCls, config);
                if (convertValue == null) {
                    if (source == null) {
                        return DefaultResult.defaultSuccessResult(new SoftReference<>(null));
                    } else {
                        return DefaultResult.defaultSuccessResult(new SoftReference<>(source));
                    }
                } else {
                    return DefaultResult.defaultSuccessResult(new SoftReference<>(convertValue));
                }
            } else if (targetCls == WeakReference.class) {
                Object convertValue = this.convertGenericTypeValue(source, genericType, targetCls, config);
                if (convertValue == null) {
                    if (source == null) {
                        return DefaultResult.defaultSuccessResult(new WeakReference<>(null));
                    } else {
                        return DefaultResult.defaultSuccessResult(new WeakReference<>(source));
                    }
                } else {
                    return DefaultResult.defaultSuccessResult(new WeakReference<>(convertValue));
                }
            } else if (targetCls == Stream.class) {
                if (source == null) {
                    return DefaultResult.defaultSuccessResult(Stream.empty());
                } else {
                    if (genericType == null || genericType.resolve() == null) {
                        if (source instanceof Collection) {
                            Collection<?> coll = (Collection<?>) source;
                            return DefaultResult.defaultSuccessResult(coll.stream());
                        } else {
                            return DefaultResult.defaultSuccessResult(Stream.of(source));
                        }
                    } else {
                        Class<?> genericCls = genericType.resolve();
                        TypeValueResolver resolver = TypeValueResolvers.getResolver(genericCls);
                        if (resolver == null) {
                            throw new IllegalStateException("can not convert source to target, can not find converter");
                        }
                        if (source instanceof Collection) {
                            Stream.Builder<Object> builder = Stream.builder();
                            Collection<Object> coll = (Collection<Object>) source;
                            for (Object o : coll) {
                                Result<Object> childResult = resolver.resolve(o, genericType, config);
                                if (!childResult.success()) {
                                    throw new IllegalStateException(childResult.resultMsg());
                                } else {
                                    builder.add(childResult.instance());
                                }
                            }
                            return DefaultResult.defaultSuccessResult(builder.build());
                        } else {
                            Result<Object> childResult = resolver.resolve(source, genericType, config);
                            if (!childResult.success()) {
                                throw new IllegalStateException(childResult.resultMsg());
                            } else {
                                return DefaultResult.defaultSuccessResult(Stream.of(childResult.instance()));
                            }
                        }
                    }
                }
            } else {
                throw new IllegalStateException("can not convert source to target, unsupport target type");
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, e.getMessage());
        }
    }

    private Object convertGenericTypeValue(Object source, TypeWrapper genericType, Class<?> targetCls, ResolveConfig config) throws Exception {
        if (genericType == null || genericType.resolve() == null
                || source == null) {
            return null;
        } else {
            TypeValueResolver resolver = TypeValueResolvers.getResolver(targetCls);
            if (resolver == null) {
                throw new IllegalStateException("can not convert source to target, can not find converter");
            } else {
                Result<Object> childResult = resolver.resolve(source, genericType, config);
                if (!childResult.success()) {
                    throw new IllegalStateException(childResult.resultMsg());
                } else {
                    return childResult.instance();
                }
            }
        }
    }

}
