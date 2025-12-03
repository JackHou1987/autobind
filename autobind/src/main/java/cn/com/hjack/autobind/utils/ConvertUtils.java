package cn.com.hjack.autobind.utils;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.converter.EnumValueResolver;
import cn.com.hjack.autobind.converter.ResolvableConverters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * @ClassName: ConvertUtils
 * @Description: TODO
 * @author houqq
 * @date: 2025年7月10日
 *
 */
public class ConvertUtils {

    public static <T> Result<T> toObject(Object source, Class<T> targetClass) {
        return toObject(source, targetClass, null);
    }

    public static <T> Result<T> toObject(Object source, Class<T> targetClass, ResolveConfig config) {
        if (targetClass == null) {
            throw new IllegalArgumentException("source or target type can not be null");
        }
        ResolvableConverter resolver = ResolvableConverters.getConverter(targetClass);
        return convert(resolver, source, TypeWrappers.getType(targetClass), config);
    }

    public static <T> Result<T> toObject(Object source, TypeReference<T> reference, ResolveConfig config) {
        if (reference == null) {
            throw new IllegalArgumentException("source or reference type can not be null");
        }
        TypeWrapper targetType = TypeWrappers.getType(TypeReference.class, reference.getClass());
        if (targetType == null || targetType.getGeneric(0) == null) {
            throw new IllegalStateException("reference generic type can not be null");
        }
        Class<?> targetCls = targetType.getGeneric(0).resolve();
        if (targetCls == null) {
            throw new IllegalStateException("target type can not be null");
        }
        ResolvableConverter resolver = ResolvableConverters.getConverter(targetCls);
        return convert(resolver, source, targetType.getGeneric(0), config);
    }

    public static <T> Result<T> toObject(Object source, TypeReference<T> reference) {
        return toObject(source, reference, null);
    }

    public static <T> Result<Map<String, T>> toMap(Object source, TypeReference<Map<String, T>> typeReference, ResolveConfig config) {
        if (!TypeUtils.isMapClass(source.getClass())
                && !TypeUtils.isJavaBeanClass(source.getClass())) {
            return DefaultResult.errorResult(Constants.FAIL_CODE, "source type must be map or java bean");
        }
        if (typeReference != null) {
            TypeWrapper typeWrapper = TypeWrappers.getType(TypeReference.class, typeReference.getClass());
            return convert(ResolvableConverters.getConverter(typeWrapper.getGeneric(0).resolve()), source, typeWrapper.getGeneric(0), config);
        } else {
            return convert(ResolvableConverters.getConverter(Map.class), source, TypeWrappers.getType(Map.class), config);
        }
    }

    public static Result<Map<String, Object>> toMap(Object source, ResolveConfig registry) {
        return toMap(source, null, registry);
    }

    public static Result<Map<String, Object>> toMap(Object source) {
        return toMap(source, null, null);
    }

    public static <T> Result<List<T>> toList(Object source) {
        return toList(source, null);
    }

    public static <T> Result<List<T>> toList(Object source, TypeReference<List<T>> typeReference) {
        if (typeReference != null) {
            TypeWrapper typeWrapper = TypeWrappers.getType(TypeReference.class, typeReference.getClass());
            return convert(ResolvableConverters.getConverter(typeWrapper.getGeneric(0).resolve()), source, typeWrapper.getGeneric(0), ResolveConfig.defaultConfig);
        } else {
            return convert(ResolvableConverters.getConverter(List.class), source, TypeWrappers.getType(List.class), ResolveConfig.defaultConfig);
        }
    }

    public static Result<Integer> toInteger(Object source) {
        return convert(ResolvableConverters.getConverter(Integer.class), source, TypeWrappers.getType(Integer.class), ResolveConfig.defaultConfig);
    }

    public static Result<Byte> toByte(Object source) {
        return convert(ResolvableConverters.getConverter(Byte.class), source, TypeWrappers.getType(Byte.class), ResolveConfig.defaultConfig);
    }

    public static Result<Short> toShort(Object source) {
        return convert(ResolvableConverters.getConverter(Short.class), source, TypeWrappers.getType(Short.class), ResolveConfig.defaultConfig);
    }

    public static Result<Long> toLong(Object source) {
        return convert(ResolvableConverters.getConverter(Long.class), source, TypeWrappers.getType(Long.class), ResolveConfig.defaultConfig);
    }

    public static Result<Character> toCharacter(Object source) {
        return convert(ResolvableConverters.getConverter(Character.class), source, TypeWrappers.getType(Character.class), ResolveConfig.defaultConfig);
    }

    public static Result<Date> toDate(Object source) {
        return convert(ResolvableConverters.getConverter(Date.class), source, TypeWrappers.getType(Date.class), ResolveConfig.defaultConfig);
    }

    public static Result<LocalDate> toLocalDate(Object source) {
        return convert(ResolvableConverters.getConverter(LocalDate.class), source, TypeWrappers.getType(LocalDate.class), ResolveConfig.defaultConfig);
    }

    public static Result<LocalDateTime> toLocalDateTime(Object source) {
        return convert(ResolvableConverters.getConverter(LocalDateTime.class), source, TypeWrappers.getType(LocalDateTime.class), ResolveConfig.defaultConfig);
    }

    public static Result<LocalTime> toLocalTime(Object source) {
        return convert(ResolvableConverters.getConverter(LocalTime.class), source, TypeWrappers.getType(LocalTime.class), ResolveConfig.defaultConfig);
    }

    public static Result<Object[]> toArray(Object source) {
        return toArray(source, null);
    }

    public static <T> Result<T[]> toArray(Object source, TypeReference<T[]> typeReference) {
        if (typeReference != null) {
            TypeWrapper typeWrapper = TypeWrappers.getType(TypeReference.class, typeReference.getClass());
            return convert(ResolvableConverters.getConverter(typeWrapper.getGeneric(0).resolve()), source, typeWrapper.getGeneric(0), ResolveConfig.defaultConfig);
        } else {
            return convert(ResolvableConverters.getConverter(Object[].class), source, TypeWrappers.getType(Object[].class), ResolveConfig.defaultConfig);
        }
    }
    public static Result<String> toHex(Number number) {
        ResolvableConverter resolver = ResolvableConverters.getConverter(String.class);
        return convert(resolver, number, TypeWrappers.getType(String.class), ResolveConfig.defaultConfig.convertFeature(new ConvertFeature[] {ConvertFeature.HEX}));
    }

    public static Result<String> toBinary(Number number) {
        ResolvableConverter resolver = ResolvableConverters.getConverter(String.class);
        return convert(resolver, number, TypeWrappers.getType(String.class), ResolveConfig.defaultConfig.convertFeature(new ConvertFeature[] {ConvertFeature.BINARY}));
    }

    public static Result<String> toOct(Number number) {
        ResolvableConverter resolver = ResolvableConverters.getConverter(String.class);
        return convert(resolver, number, TypeWrappers.getType(String.class), ResolveConfig.defaultConfig.convertFeature(new ConvertFeature[] {ConvertFeature.OCT}));

    }

    public static Result<String> toString(Object object) {
        ResolvableConverter resolver = ResolvableConverters.getConverter(String.class);
        return convert(resolver, object, TypeWrappers.getType(String.class), ResolveConfig.defaultConfig);

    }

    public static Result<String> toString(Object object, ResolveConfig config) {
        ResolvableConverter resolver = ResolvableConverters.getConverter(String.class);
        return convert(resolver, object, TypeWrappers.getType(String.class), Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig));
    }

    public static <T> Result<T> toEnum(Class<T> enumClass, Object object) {
        if (enumClass == null || !enumClass.isEnum()) {
            return DefaultResult.successResult(null);
        }
        return convert(EnumValueResolver.instance, object, TypeWrappers.getType(enumClass), ResolveConfig.defaultConfig);
    }

    private static <T> Result<T> convert(ResolvableConverter resolver, Object source, TypeWrapper targetType, ResolveConfig config) {
        if (targetType == null) {
            throw new IllegalArgumentException("param can not be null");
        }
        try {
            if (resolver == null) {
                throw new IllegalArgumentException("can not convert source to target, converter not found");
            }
            Result<T> result = resolver.convert(source, targetType, Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig));
            if (!result.success()) {
                return DefaultResult.errorResult(result.instance(), Constants.FAIL_CODE, result.resultMsg());
            } else {
                return DefaultResult.successResult(result.instance());
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(Constants.FAIL_CODE, e.getMessage());
        }
    }

}
