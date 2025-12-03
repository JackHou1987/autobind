/**
 *
 */
package cn.com.hjack.autobind.converter;

import cn.com.hjack.autobind.ResolvableConverter;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.utils.CastUtils;

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
public class SqlDateValueResolver extends AbstractResolvableConverter {

    public static SqlDateValueResolver instance = new SqlDateValueResolver();

    private SqlDateValueResolver() {

    }

    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        if (source == null) {
            return DefaultResult.successResult();
        }
        Class<?> targetCls = targetType.resolve();
        try {
            if (targetCls == Date.class) {
                if (source instanceof java.util.Date) {
                    return DefaultResult.successResult(CastUtils.castSafe(new Date(((java.util.Date) source).getTime())));
                } else if (source instanceof Number) {
                    return DefaultResult.successResult(CastUtils.castSafe(new Date(((Number) source).longValue())));
                } else if (source instanceof String) {
                    ResolvableConverter resolver = ResolvableConverters.getConverter(java.util.Date.class);
                    if (resolver == null) {
                        throw new IllegalStateException("can not convert source to target, can not find converter");
                    }
                    Result<Date> childResult = resolver.convert(source, TypeWrappers.getType(java.util.Date.class), config);
                    if (!childResult.success()) {
                        throw new IllegalStateException(childResult.resultMsg());
                    }
                    return DefaultResult.successResult(CastUtils.castSafe(new Date(childResult.instance().getTime())));
                } else {
                    throw new IllegalStateException("can not convert source to target, unsupport source type");
                }
            } else if (targetCls == Timestamp.class) {
                if (source instanceof java.util.Date) {
                    return DefaultResult.successResult(CastUtils.castSafe(new Timestamp(((java.util.Date) source).getTime())));
                } else if (source instanceof Number) {
                    return DefaultResult.successResult(CastUtils.castSafe(new Timestamp(((Number) source).longValue())));
                } else if (source instanceof String) {
                    ResolvableConverter resolver = ResolvableConverters.getConverter(java.util.Date.class);
                    if (resolver == null) {
                        throw new IllegalStateException("can not convert source to target, can not find converter");
                    }
                    Result<Date> childResult = resolver.convert(source, TypeWrappers.getType(java.util.Date.class), config);
                    if (!childResult.success()) {
                        throw new IllegalStateException(childResult.resultMsg());
                    }
                    return DefaultResult.successResult(CastUtils.castSafe(new Timestamp(childResult.instance().getTime())));
                } else {
                    throw new IllegalStateException("can not convert source to target, unsupport source type");
                }
            } else if (targetCls == Time.class) {
                if (source instanceof java.util.Date) {
                    return DefaultResult.successResult(CastUtils.castSafe(new Time(((java.util.Date) source).getTime())));
                } else if (source instanceof Number) {
                    return DefaultResult.successResult(CastUtils.castSafe(new Time(((Number) source).longValue())));
                } else if (source instanceof String) {
                    ResolvableConverter resolver = ResolvableConverters.getConverter(LocalTime.class);
                    if (resolver == null) {
                        throw new IllegalStateException("can not convert source to target, can not find converter");
                    }
                    Result<LocalTime> childResult = resolver.convert(source, TypeWrappers.getType(LocalTime.class), config);
                    if (!childResult.success()) {
                        throw new IllegalStateException(childResult.resultMsg());
                    }
                    return DefaultResult.successResult(CastUtils.castSafe(Time.valueOf(childResult.instance())));
                } else {
                    throw new IllegalStateException("can not convert source to target, unsupport source type");
                }
            } else {
                throw new IllegalStateException("can not convert source to target, unsupport target type");
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(e.getMessage());
        }
    }

}
