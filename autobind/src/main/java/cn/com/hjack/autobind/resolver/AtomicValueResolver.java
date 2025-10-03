/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.factory.TypeValueResolvers;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.validation.DefaultResult;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;


/**
 * @ClassName: AtomicValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年9月2日
 *
 */
public class AtomicValueResolver extends AbstractTypeValueResolver {

    public static AtomicValueResolver instance = new AtomicValueResolver();

    @Override
    protected Result<Object> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config)
            throws Exception {
        if (source == null) {
            return DefaultResult.defaultSuccessResult(null);
        }
        try {
            Class<?> targetClass = targetType.resolve();
            if (targetClass == AtomicIntegerArray.class) {
                Result<int[]> result = TypeValueResolvers.getResolver(int[].class).resolve(source, TypeWrappers.getType(int[].class), config);
                if (!result.success()) {
                    return DefaultResult.defaultSuccessResult(new AtomicIntegerArray(result.instance()));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicIntegerArray, unsupport source type");
                }
            } else if (targetClass == AtomicLongArray.class) {
                Result<long[]> result = TypeValueResolvers.getResolver(long[].class).resolve(source, TypeWrappers.getType(long[].class), config);
                if (!result.success()) {
                    return DefaultResult.defaultSuccessResult(new AtomicLongArray(result.instance()));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicLongArray, unsupport source type");
                }
            } else if (targetClass == AtomicReferenceArray.class) {
                Result<Object[]> result = TypeValueResolvers.getResolver(Object[].class).resolve(source, TypeWrappers.getType(Object[].class), config);
                if (!result.success()) {
                    return DefaultResult.defaultSuccessResult(new AtomicReferenceArray<>(result.instance()));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicReferenceArray, unsupport source type");
                }
            } else if (targetClass == AtomicLong.class) {
                Result<Long> result = TypeValueResolvers.getResolver(Long.class).resolve(source, TypeWrappers.getType(Long.class), config);
                if (!result.success()) {
                    return DefaultResult.defaultSuccessResult(new AtomicLong(result.instance()));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicLong, unsupport source type");
                }
            } else if (targetClass == AtomicInteger.class) {
                Result<Integer> result = TypeValueResolvers.getResolver(Integer.class).resolve(source, TypeWrappers.getType(Integer.class), config);
                if (!result.success()) {
                    return DefaultResult.defaultSuccessResult(new AtomicLong(result.instance()));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicInteger, unsupport source type");
                }
            } else if (targetClass == AtomicBoolean.class) {
                Result<Boolean> result = TypeValueResolvers.getResolver(Boolean.class).resolve(source, TypeWrappers.getType(Boolean.class), config);
                if (!result.success()) {
                    return DefaultResult.defaultSuccessResult(new AtomicBoolean(result.instance()));
                } else {
                    throw new IllegalStateException("can not convert source to AtomicBoolean, unsupport source type");
                }
            } else {
                throw new IllegalStateException("can not convert source to target, unsupport target type");
            }
        } catch (Exception e) {
            Result<Object> result = new DefaultResult<Object>();
            result.setResultCode(Constants.FAIL_CODE);
            result.setResultMsg(e.getMessage());
            return result;
        }
    }

}
