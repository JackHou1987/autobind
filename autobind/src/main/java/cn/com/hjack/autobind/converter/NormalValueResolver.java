package cn.com.hjack.autobind.converter;

import cn.com.hjack.autobind.ConvertFeature;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.utils.CastUtils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;


/**
 * java常见对象类型转换器
 * @author houqq
 * @date: 2025年8月25日
 */
public class NormalValueResolver extends AbstractResolvableConverter {

    public static NormalValueResolver instance = new NormalValueResolver();

    private NormalValueResolver() {

    }

    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        Class<?> targetClass = targetType.resolve();
        try {
            if (targetClass == UUID.class) {
                if (source == null) {
                    return DefaultResult.successResult(CastUtils.castSafe(UUID.randomUUID()));
                }
                if (source instanceof String) {
                    return DefaultResult.successResult(CastUtils.castSafe(UUID.fromString((String) source)));
                }
                if (source instanceof byte[]) {
                    return DefaultResult.successResult(CastUtils.castSafe(UUID.nameUUIDFromBytes((byte[]) source)));
                }
                Result<Object> childResult = convertToString(source, config);
                if (!childResult.success()) {
                    return DefaultResult.errorResult(childResult.resultMsg());
                } else {
                    return DefaultResult.successResult(CastUtils.castSafe(UUID.fromString((String) childResult.instance())));
                }
            } else if (targetClass == URL.class) {
                if (source == null) {
                    return DefaultResult.successResult();
                } else {
                    Result<Object> childResult = convertToString(source, config);
                    if (!childResult.success()) {
                        return DefaultResult.errorResult(childResult.resultMsg());
                    } else {
                        return DefaultResult.successResult(CastUtils.castSafe(new URL((String) childResult.instance())));
                    }
                }
            } else if (targetClass == URI.class) {
                if (source == null) {
                    return DefaultResult.successResult();
                } else {
                    Result<Object> childResult = convertToString(source, config);
                    if (!childResult.success()) {
                        return DefaultResult.errorResult(childResult.resultMsg());
                    } else {
                        return DefaultResult.successResult(CastUtils.castSafe(URI.create((String) childResult.instance())));
                    }
                }
            } else if (targetClass == File.class) {
                if (source == null) {
                    return DefaultResult.successResult();
                } else {
                    Result<Object> childResult = convertToString(source, config);
                    if (!childResult.success()) {
                        return DefaultResult.errorResult(childResult.resultMsg());
                    } else {
                        return DefaultResult.successResult(CastUtils.castSafe(new File((String) childResult.instance())));
                    }
                }
            } else if (targetClass == Charset.class) {
                if (source == null) {
                    return DefaultResult.successResult(CastUtils.castSafe(StandardCharsets.UTF_8));
                } else {
                    Result<Object> childResult = convertToString(source, config);
                    if (!childResult.success()) {
                        return DefaultResult.errorResult(childResult.resultMsg());
                    } else {
                        return DefaultResult.successResult(CastUtils.castSafe(Charset.forName((String) childResult.instance())));
                    }
                }
            } else if (targetClass == Locale.class) {
                if (source == null) {
                    return DefaultResult.successResult(CastUtils.castSafe(StandardCharsets.UTF_8));
                } else {
                    Result<Object> childResult = convertToString(source, config);
                    if (!childResult.success()) {
                        return DefaultResult.errorResult(childResult.resultMsg());
                    } else {
                        String str = (String) childResult.instance();
                        String[] items = str.split("_");
                        if (items.length == 1) {
                            return DefaultResult.successResult(CastUtils.castSafe(new Locale(items[0])));
                        } else if (items.length == 2) {
                            return DefaultResult.successResult(CastUtils.castSafe(new Locale(items[0], items[1])));
                        } else {
                            return DefaultResult.successResult(CastUtils.castSafe(new Locale(items[0], items[1], items[2])));
                        }
                    }
                }
            } else if (targetClass == ZoneId.class) {
                if (source == null) {
                    return DefaultResult.successResult(CastUtils.castSafe(ZoneId.systemDefault()));
                }
                if (source instanceof String) {
                    return DefaultResult.successResult(CastUtils.castSafe(ZoneId.of((String) source)));
                } else if (source instanceof TimeZone) {
                    return DefaultResult.successResult(CastUtils.castSafe(((TimeZone) source).toZoneId()));
                } else {
                    return DefaultResult.errorResult("can not convert source to zone id type");
                }
            } else if (targetClass == Class.class) {
                if (source == null) {
                    return DefaultResult.successResult();
                }
                Result<Object> childResult = convertToString(source, config);
                if (!childResult.success()) {
                    return DefaultResult.errorResult(childResult.resultMsg());
                } else {
                    return DefaultResult.successResult(CastUtils.castSafe(Class.forName((String) childResult.instance())));
                }
            } else if (targetClass == Path.class) {
                if (source == null) {
                    return DefaultResult.successResult();
                } else if (source instanceof String) {
                    Result<Object> childResult = convertToString(source, config);
                    if (!childResult.success()) {
                        return DefaultResult.errorResult(childResult.resultMsg());
                    } else {
                        return DefaultResult.successResult(CastUtils.castSafe(Paths.get((String) childResult.instance())));
                    }
                } else if (source instanceof URI) {
                    return DefaultResult.successResult(CastUtils.castSafe(Paths.get((URI) source)));
                } else {
                    return DefaultResult.successResult();
                }
            } else if (targetClass == Boolean.class) {
                if (source == null) {
                    return DefaultResult.successResult();
                } else if (source instanceof String) {
                    return DefaultResult.successResult(CastUtils.castSafe(CastUtils.toBoolean((String) source)));
                } else if (source instanceof Number) {
                    return DefaultResult.successResult(CastUtils.castSafe(CastUtils.toBoolean((Number) source)));
                } else {
                    return DefaultResult.errorResult("can not convert source to boolean, unsupport source type");
                }
            } else if (targetClass == boolean.class) {
                if (source instanceof String) {
                    return DefaultResult.successResult(CastUtils.castSafe(Optional.ofNullable(CastUtils.toBoolean((String) source)).orElse(Boolean.FALSE)));
                } else if (source instanceof Number) {
                    return DefaultResult.successResult(CastUtils.castSafe(Optional.ofNullable(CastUtils.toBoolean((Number) source)).orElse(Boolean.FALSE)));
                } else {
                    return DefaultResult.errorResult("can not convert source to boolean, unsupport source type");
                }
            } else if (ByteBuffer.class.isAssignableFrom(targetClass)) {
                if (source == null) {
                    return DefaultResult.successResult();
                } else if (source instanceof String) {
                    // 堆外内存
                    if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.NATIVE_BUFFER)) {
                        return DefaultResult.successResult(CastUtils.castSafe(ByteBuffer.allocate(Integer.parseInt((String) source))));
                    } else {
                        return DefaultResult.successResult(CastUtils.castSafe(ByteBuffer.allocateDirect(Integer.parseInt((String) source))));
                    }
                } else if (source instanceof Number) {
                    // 堆外内存
                    if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.NATIVE_BUFFER)) {
                        return DefaultResult.successResult(CastUtils.castSafe(ByteBuffer.allocate(((Number) source).intValue())));
                    } else {
                        return DefaultResult.successResult(CastUtils.castSafe(ByteBuffer.allocateDirect(((Number) source).intValue())));
                    }
                } else if (source instanceof byte[]) {
                    return DefaultResult.successResult(CastUtils.castSafe(ByteBuffer.wrap((byte[]) source)));
                } else if (source instanceof Byte[]) {
                    return DefaultResult.successResult(CastUtils.castSafe(ByteBuffer.wrap(CastUtils.toPrimByteArrayValue((Byte[]) source))));
                } else {
                    return DefaultResult.errorResult("can not convert to ByteBuffer unsupport source type");
                }
            } else if (targetClass == TimeZone.class) {
                if (source == null) {
                    return DefaultResult.successResult();
                } else if (source instanceof String) {
                    return DefaultResult.successResult(CastUtils.castSafe(TimeZone.getTimeZone((String) source)));
                } else if (source instanceof ZoneId) {
                    return DefaultResult.successResult(CastUtils.castSafe(TimeZone.getTimeZone((ZoneId) source)));
                } else {
                    return DefaultResult.errorResult("can not convert source to TimeZone, unsupport source type");
                }
            } else if (targetClass == EnumSet.class) {
                if (source == null) {
                    return DefaultResult.successResult();
                } else if (source instanceof Class) {
                    Class<?> sourceClass = (Class<?>) source;
                    return DefaultResult.successResult(CastUtils.castSafe(EnumSet.noneOf(sourceClass.asSubclass(Enum.class))));
                } else {
                    Result<Class<?>> childResult = ResolvableConverters.getConverter(Class.class).convert(source, TypeWrappers.getType(Class.class), config);
                    if (childResult.success()) {
                        Class<?> sourceClass = childResult.instance();
                        return DefaultResult.successResult(CastUtils.castSafe(EnumSet.noneOf(sourceClass.asSubclass(Enum.class))));
                    } else {
                        return DefaultResult.errorResult("can not convert source to target, unkown source type");
                    }
                }
            } else if (targetClass == EnumMap.class) {
                if (source == null) {
                    return DefaultResult.successResult();
                } else if (source instanceof Class) {
                    Class<?> sourceClass = (Class<?>) source;
                    return DefaultResult.successResult(CastUtils.castSafe(new EnumMap<>(sourceClass.asSubclass(Enum.class))));
                } else {
                    Result<Class<?>> childResult = ResolvableConverters.getConverter(Class.class).convert(source, TypeWrappers.getType(Class.class), config);
                    if (childResult.success()) {
                        Class<?> sourceClass = childResult.instance();
                        return DefaultResult.successResult(CastUtils.castSafe(new EnumMap<>(sourceClass.asSubclass(Enum.class))));
                    } else {
                        return DefaultResult.errorResult("can not convert source to target, unkown source type");
                    }
                }
            } else if (targetClass == BitSet.class) {
                if (source == null) {
                    return CastUtils.castSafe(DefaultResult.successResult(new BitSet()));
                } else {
                    Result<byte[]> childResult = ResolvableConverters.getConverter(byte[].class).convert(source, TypeWrappers.getType(byte[].class), config);
                    if (childResult.success()) {
                        return DefaultResult.successResult(CastUtils.castSafe(BitSet.valueOf(childResult.instance())));
                    } else {
                        return DefaultResult.errorResult("can not convert source to target, unkown source type");
                    }
                }
            } else {
                return DefaultResult.errorResult("can not convert source to target, unkown target type");
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(e.getMessage());
        }
    }

    private Result<Object> convertToString(Object source, ResolveConfig config) throws Exception {
        Result<Object> result = ResolvableConverters.getConverter(String.class).convert(source, TypeWrappers.getType(String.class), config);
        if (!result.success()) {
            return DefaultResult.errorResult(result.resultMsg());
        } else {
            return DefaultResult.successResult(result.instance());
        }
    }

}
