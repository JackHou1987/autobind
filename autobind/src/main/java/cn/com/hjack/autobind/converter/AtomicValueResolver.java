/**
 *
 */
package cn.com.hjack.autobind.converter;

import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.utils.CastUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;


/**
 * 原子类型转换器
 * @author houqq
 * @date: 2025年9月2日
 */
public class AtomicValueResolver extends AbstractResolvableConverter {

    public static AtomicValueResolver instance = new AtomicValueResolver();

    private AtomicValueResolver() {

    }

    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        if (source == null) {
            return DefaultResult.successResult();
        }
        try {
            Class<?> targetClass = targetType.resolve();
            if (targetClass == AtomicIntegerArray.class) {
                Result<int[]> result = ResolvableConverters.getConverter(int[].class).convert(source, TypeWrappers.getType(int[].class), config);
                if (!result.success()) {
                    return DefaultResult.successResult(CastUtils.castSafe(new AtomicIntegerArray(result.instance())));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicIntegerArray, unsupport source type");
                }
            } else if (targetClass == AtomicLongArray.class) {
                Result<long[]> result = ResolvableConverters.getConverter(long[].class).convert(source, TypeWrappers.getType(long[].class), config);
                if (!result.success()) {
                    return DefaultResult.successResult(CastUtils.castSafe(new AtomicLongArray(result.instance())));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicLongArray, unsupport source type");
                }
            } else if (targetClass == AtomicReferenceArray.class) {
                Result<Object[]> result = ResolvableConverters.getConverter(Object[].class).convert(source, TypeWrappers.getType(Object[].class), config);
                if (!result.success()) {
                    return DefaultResult.successResult(CastUtils.castSafe(new AtomicReferenceArray<>(result.instance())));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicReferenceArray, unsupport source type");
                }
            } else if (targetClass == AtomicLong.class) {
                Result<Long> result = ResolvableConverters.getConverter(Long.class).convert(source, TypeWrappers.getType(Long.class), config);
                if (!result.success()) {
                    return DefaultResult.successResult(CastUtils.castSafe(new AtomicLong(result.instance())));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicLong, unsupport source type");
                }
            } else if (targetClass == AtomicInteger.class) {
                Result<Integer> result = ResolvableConverters.getConverter(Integer.class).convert(source, TypeWrappers.getType(Integer.class), config);
                if (!result.success()) {
                    return DefaultResult.successResult(CastUtils.castSafe(new AtomicLong(result.instance())));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicInteger, unsupport source type");
                }
            } else if (targetClass == AtomicBoolean.class) {
                Result<Boolean> result = ResolvableConverters.getConverter(Boolean.class).convert(source, TypeWrappers.getType(Boolean.class), config);
                if (!result.success()) {
                    return DefaultResult.successResult(CastUtils.castSafe(new AtomicBoolean(result.instance())));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicBoolean, unsupport source type");
                }
            } else {
                throw new IllegalStateException("can not convert source to target, unsupport target type");
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(e.getMessage());
        }
    }

}
