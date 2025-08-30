/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.ConvertFeature;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.validation.DefaultResult;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @ClassName: NormalValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月25日
 *
 */
public class NormalValueResolver extends AbstractTypeValueResolver {

    @Override
    protected Result<Object> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config)
            throws Exception {
        Class<?> targetClass = targetType.resolve();
        try {
            if (targetClass == UUID.class) {
                if (source == null) {
                    return DefaultResult.defaultSuccessResult(UUID.randomUUID());
                }
                if (source instanceof String) {
                    return DefaultResult.defaultSuccessResult(UUID.fromString((String) source));
                }
                if (source instanceof byte[]) {
                    return DefaultResult.defaultSuccessResult(UUID.nameUUIDFromBytes((byte[]) source));
                }
                Result<Object> childResult = convertToStr(source, config);
                if (!childResult.success()) {
                    return childResult;
                } else {
                    childResult.instance(UUID.fromString((String) childResult.instance()));
                    return childResult;
                }
            }
            if (targetClass == URL.class) {
                if (source == null) {
                    return DefaultResult.defaultSuccessResult(null);
                } else {
                    Result<Object> childResult = convertToStr(source, config);
                    if (!childResult.success()) {
                        return childResult;
                    } else {
                        childResult.instance(new URL((String) childResult.instance()));
                        return childResult;
                    }
                }
            }
            if (targetClass == URI.class) {
                if (source == null) {
                    return DefaultResult.defaultSuccessResult(null);
                } else {
                    Result<Object> childResult = convertToStr(source, config);
                    if (!childResult.success()) {
                        return childResult;
                    } else {
                        childResult.instance(URI.create((String) childResult.instance()));
                        return childResult;
                    }
                }
            }
            if (targetClass == File.class) {
                if (source == null) {
                    return DefaultResult.defaultSuccessResult(null);
                } else {
                    Result<Object> childResult = convertToStr(source, config);
                    if (!childResult.success()) {
                        return childResult;
                    } else {
                        childResult.instance(new File((String) childResult.instance()));
                        return childResult;
                    }
                }
            }
            if (targetClass == Charset.class) {
                if (source == null) {
                    return DefaultResult.defaultSuccessResult(StandardCharsets.UTF_8);
                } else {
                    Result<Object> childResult = convertToStr(source, config);
                    if (!childResult.success()) {
                        return childResult;
                    } else {
                        childResult.instance(Charset.forName((String) childResult.instance()));
                        return childResult;
                    }
                }
            }
            if (targetClass == Locale.class) {
                if (source == null) {
                    return DefaultResult.defaultSuccessResult(StandardCharsets.UTF_8);
                } else {
                    Result<Object> childResult = convertToStr(source, config);
                    if (!childResult.success()) {
                        return childResult;
                    } else {
                        String str = (String) childResult.instance();
                        String[] items = str.split("_");
                        if (items.length == 1) {
                            childResult.instance(new Locale(items[0]));
                        }
                        if (items.length == 2) {
                            childResult.instance(new Locale(items[0], items[1]));
                        }
                        childResult.instance(new Locale(items[0], items[1], items[2]));
                    }
                    return childResult;
                }
            }

            if (targetClass == ZoneId.class) {
                if (source == null) {
                    return DefaultResult.defaultSuccessResult(ZoneId.systemDefault());
                }
                if (source instanceof String) {
                    Result<Object> result = new DefaultResult<>();
                    result.setResultCode(Constants.SUCCESS_CODE);
                    result.setResultMsg(Constants.SUCCESS_MESSAGE);
                    result.instance(ZoneId.of((String) source));
                    return result;
                } else if (source instanceof TimeZone) {
                    Result<Object> result = new DefaultResult<>();
                    result.setResultCode(Constants.SUCCESS_CODE);
                    result.setResultMsg(Constants.SUCCESS_MESSAGE);
                    result.instance(((TimeZone) source).toZoneId());
                    return result;
                } else {
                    Result<Object> result = new DefaultResult<>();
                    result.setResultCode(Constants.FAIL_CODE);
                    result.setResultMsg("can not convert source to zone id type");
                    return result;
                }
            }
            if (targetClass == Class.class) {
                if (source == null) {
                    return DefaultResult.errorResult(null);
                }
                Result<Object> childResult = convertToStr(source, config);
                if (!childResult.success()) {
                    return childResult;
                } else {
                    childResult.instance(Class.forName((String) childResult.instance()));
                    return childResult;
                }
            }
            if (targetClass == Path.class) {
                if (source == null) {
                    return DefaultResult.defaultSuccessResult(null);
                } else if (source instanceof String) {
                    Result<Object> childResult = convertToStr(source, config);
                    if (!childResult.success()) {
                        return childResult;
                    } else {
                        childResult.instance(Paths.get((String) childResult.instance()));
                        return childResult;
                    }
                } else if (source instanceof URI) {
                    return DefaultResult.defaultSuccessResult(Paths.get((URI) source));
                } else {
                    return DefaultResult.defaultSuccessResult(null);
                }
            }
            if (targetClass == Boolean.class) {
                if (source instanceof String) {
                    String value = (String) source;
                    return DefaultResult.defaultSuccessResult(CastUtils.toBoolean(value));
                } else if (source instanceof Number) {
                    Number value = (Number) source;
                    return DefaultResult.defaultSuccessResult(CastUtils.toBoolean(value));
                } else {
                    return DefaultResult.defaultSuccessResult("can not convert source to Boolean, unsupport source type");
                }
            }
            if (targetClass == boolean.class) {
                if (source instanceof String) {
                    String value = (String) source;
                    return DefaultResult.defaultSuccessResult(Optional.ofNullable(CastUtils.toBoolean(value)).orElse(Boolean.FALSE));
                } else if (source instanceof Number) {
                    Number value = (Number) source;
                    return DefaultResult.defaultSuccessResult(Optional.ofNullable(CastUtils.toBoolean(value)).orElse(Boolean.FALSE));
                } else {
                    return DefaultResult.defaultSuccessResult("can not convert source to boolean, unsupport source type");
                }
            }

            if (ByteBuffer.class.isAssignableFrom(targetClass)) {
                if (source instanceof String) {
                    int value = Integer.parseInt((String) source);
                    // 堆外内存
                    if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.NATIVE_BUFFER)) {
                        return DefaultResult.defaultSuccessResult(ByteBuffer.allocate(value));
                    } else {
                        return DefaultResult.defaultSuccessResult(ByteBuffer.allocateDirect(value));
                    }
                } else if (source instanceof Number) {
                    Number value = (Number) source;
                    // 堆外内存
                    if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.NATIVE_BUFFER)) {
                        return DefaultResult.defaultSuccessResult(ByteBuffer.allocate(value.intValue()));
                    } else {
                        return DefaultResult.defaultSuccessResult(ByteBuffer.allocateDirect(value.intValue()));
                    }
                } else if (source instanceof byte[]) {
                    return DefaultResult.defaultSuccessResult(ByteBuffer.wrap((byte[]) source));
                } else if (source instanceof Byte[]) {
                    Byte[] value = (Byte[]) source;
                    return DefaultResult.defaultSuccessResult(ByteBuffer.wrap(CastUtils.toPrimByteArrayValue(value)));
                } else {
                    throw new IllegalStateException("can not convert to" + TypeUtils.getCanonicalName(targetClass) + " unsupport source type");
                }
            }
            if (targetClass == AtomicBoolean.class) {
                if (source instanceof String) {
                    String value = (String) source;
                    return DefaultResult.defaultSuccessResult(new AtomicBoolean(Optional.ofNullable(CastUtils.toBoolean(value)).orElse(Boolean.FALSE)));
                } else if (source instanceof Number) {
                    Number value = (Number) source;
                    return DefaultResult.defaultSuccessResult(new AtomicBoolean(Optional.ofNullable(CastUtils.toBoolean(value)).orElse(Boolean.FALSE)));
                } else {
                    return DefaultResult.defaultSuccessResult("can not convert source to boolean, unsupport source type");
                }
            }
            if (targetClass == TimeZone.class) {
                if (source instanceof String) {
                    String value = (String) source;
                    return DefaultResult.defaultSuccessResult(TimeZone.getTimeZone(value));
                } else if (source instanceof ZoneId) {
                    ZoneId value = (ZoneId) source;
                    return DefaultResult.defaultSuccessResult(TimeZone.getTimeZone(value));
                } else {
                    return DefaultResult.defaultSuccessResult("can not convert source to boolean, unsupport source type");
                }
            } else {
                Result<Object> childResult = convertToStr(source, config);
                if (!childResult.success()) {
                    return childResult;
                } else {
                    childResult.instance(TimeZone.getTimeZone((String) childResult.instance()));
                    return childResult;
                }
            }
        } catch (Exception e) {
            Result<Object> result = new DefaultResult<Object>();
            result.setResultCode(Constants.FAIL_CODE);
            result.setResultMsg(e.getMessage());
        }
        return null;
    }

    private Result<Object> convertToStr(Object source, ResolveConfig config) throws Exception {
        Result<Object> result = new DefaultResult<Object>();
        Result<Object> childResult = StringValueResolver.instance.resolve(source, TypeWrappers.getType(String.class), ResolveConfig.copy(config));
        if (!childResult.success()) {
            result.setResultCode(Constants.FAIL_CODE);
            result.setResultMsg(childResult.resultMsg());
            return result;
        } else {
            result.setResultCode(Constants.RESULT_CODE_KEY);
            result.setResultMsg(Constants.RESULT_MSG_KEY);
            result.instance(childResult.instance());
            return result;
        }
    }

}
