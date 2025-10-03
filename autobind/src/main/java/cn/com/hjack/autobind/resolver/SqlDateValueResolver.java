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

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;


/**
 * @ClassName: SqlDateValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年9月5日
 */
public class SqlDateValueResolver extends AbstractTypeValueResolver {

    public static SqlDateValueResolver instance = new SqlDateValueResolver();

    private SqlDateValueResolver() {

    }

    @Override
    protected Result<Object> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config)
            throws Exception {
        if (source == null) {
            return DefaultResult.defaultSuccessResult();
        }
        Class<?> targetCls = targetType.resolve();
        try {
            if (targetCls == Date.class) {
                if (source instanceof java.util.Date) {
                    return DefaultResult.defaultSuccessResult(new Date(((java.util.Date) source).getTime()));
                } else if (source instanceof Number) {
                    return DefaultResult.defaultSuccessResult(new Date(((Number) source).longValue()));
                } else if (source instanceof String) {
                    TypeValueResolver resolver = TypeValueResolvers.getResolver(java.util.Date.class);
                    if (resolver == null) {
                        throw new IllegalStateException("can not convert source to target, can not find converter");
                    }
                    Result<Date> childResult = resolver.resolve(source, TypeWrappers.getType(java.util.Date.class), config);
                    if (!childResult.success()) {
                        throw new IllegalStateException(childResult.resultMsg());
                    }
                    return DefaultResult.defaultSuccessResult(new Date(childResult.instance().getTime()));
                } else {
                    throw new IllegalStateException("can not convert source to target, unsupport source type");
                }
            } else if (targetCls == Timestamp.class) {
                if (source instanceof java.util.Date) {
                    return DefaultResult.defaultSuccessResult(new Timestamp(((java.util.Date) source).getTime()));
                } else if (source instanceof Number) {
                    return DefaultResult.defaultSuccessResult(new Timestamp(((Number) source).longValue()));
                } else if (source instanceof String) {
                    TypeValueResolver resolver = TypeValueResolvers.getResolver(java.util.Date.class);
                    if (resolver == null) {
                        throw new IllegalStateException("can not convert source to target, can not find converter");
                    }
                    Result<Date> childResult = resolver.resolve(source, TypeWrappers.getType(java.util.Date.class), config);
                    if (!childResult.success()) {
                        throw new IllegalStateException(childResult.resultMsg());
                    }
                    return DefaultResult.defaultSuccessResult(new Timestamp(childResult.instance().getTime()));
                } else {
                    throw new IllegalStateException("can not convert source to target, unsupport source type");
                }
            } else if (targetCls == Time.class) {
                if (source instanceof java.util.Date) {
                    return DefaultResult.defaultSuccessResult(new Time(((java.util.Date) source).getTime()));
                } else if (source instanceof Number) {
                    return DefaultResult.defaultSuccessResult(new Time(((Number) source).longValue()));
                } else if (source instanceof String) {
                    TypeValueResolver resolver = TypeValueResolvers.getResolver(LocalTime.class);
                    if (resolver == null) {
                        throw new IllegalStateException("can not convert source to target, can not find converter");
                    }
                    Result<LocalTime> childResult = resolver.resolve(source, TypeWrappers.getType(LocalTime.class), config);
                    if (!childResult.success()) {
                        throw new IllegalStateException(childResult.resultMsg());
                    }
                    return DefaultResult.defaultSuccessResult(Time.valueOf(childResult.instance()));
                } else {
                    throw new IllegalStateException("can not convert source to target, unsupport source type");
                }
            } else {
                throw new IllegalStateException("can not convert source to target, unsupport target type");
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, e.getMessage());
        }
    }

}
