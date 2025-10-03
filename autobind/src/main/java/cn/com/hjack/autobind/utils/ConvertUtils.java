/**
 *
 */
package cn.com.hjack.autobind.utils;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.factory.TypeValueResolvers;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.resolver.EnumValueResolver;
import cn.com.hjack.autobind.validation.DefaultResult;

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

    public static <T> Result<T> toObject(Object source, Class<T> targetType) {
        return toObject(source, targetType, null);
    }


    public static <T> Result<T> toObject(Object source, Class<T> targetType, ResolveConfig config) {
        if (targetType == null) {
            throw new IllegalArgumentException("source or target type can not be null");
        }

        TypeValueResolver resolver = TypeValueResolvers.getResolver(targetType);

        if (resolver != null) {
            return convert(resolver, source, TypeWrappers.getType(targetType), config);
        }

        throw new IllegalStateException("can not convert source to target");
    }

    public static <T> Result<T> toObject(Object source, TypeReference<T> reference, ResolveConfig config) {
        if (reference == null) {
            throw new IllegalArgumentException("source or reference type can not be null");
        }

        TypeWrapper targetType = TypeWrappers.getType(TypeReference.class, reference.getClass());
        if (targetType.getGeneric(0) == null) {
            throw new IllegalStateException("reference generic type can not be null");
        }
        Class<?> targetCls = targetType.getGeneric(0).resolve();
        if (targetCls == null) {
            throw new IllegalStateException("target type can not be null");
        }

        TypeValueResolver resolver = TypeValueResolvers.getResolver(targetCls);

        if (resolver != null) {
            return convert(resolver, source, targetType.getGeneric(0), config);
        }

        throw new IllegalStateException("can not convert source to target");
    }

    public static <T> Result<T> toObject(Object source, TypeReference<T> reference) {
        return toObject(source, reference, null);
    }

    public static <T> Result<T> toJavaBean(Object source, Class<T> targetType, ResolveConfig config) {
        if (targetType == null) {
            throw new IllegalArgumentException("source or target type can not be null");
        }

        if (!TypeUtils.isJavaBeanClass(targetType)) {
            throw new IllegalArgumentException("target type is not java bean");
        }

        TypeWrapper typeWrapper = TypeWrappers.getType(targetType);

        TypeValueResolver resolver = TypeValueResolvers.getResolver(targetType);

        if (resolver != null) {
            return convert(resolver, source, typeWrapper, config);
        }

        throw new IllegalStateException("can not convert source to target");
    }

    public static <T> T toJavaBean(Object source, Class<T> targetType) {
        return toJavaBean(source, targetType);
    }


    public static <T> Result<Map<String, T>> toMap(Object source, TypeReference<Map<String, T>> typeReference, ResolveConfig config) {
        if (!TypeUtils.isMapClass(source.getClass())
                && !TypeUtils.isJavaBeanClass(source.getClass())) {
            throw new IllegalArgumentException("source type must be map or java bean");
        }

        if (typeReference != null) {
            TypeWrapper typeWrapper = TypeWrappers.getType(TypeReference.class, typeReference.getClass());
            return convert(TypeValueResolvers.getResolver(typeWrapper.getGeneric(0).resolve()), source, typeWrapper.getGeneric(0), config);
        } else {
            return convert(TypeValueResolvers.getResolver(Map.class), source, TypeWrappers.getType(Map.class), config);
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
            return convert(TypeValueResolvers.getResolver(typeWrapper.getGeneric(0).resolve()), source, typeWrapper.getGeneric(0), ResolveConfig.defaultConfig);
        } else {
            return convert(TypeValueResolvers.getResolver(List.class), source, TypeWrappers.getType(List.class), ResolveConfig.defaultConfig);
        }
    }

    public static Result<Integer> toInteger(Object source) {
        return convert(TypeValueResolvers.getResolver(Integer.class), source, TypeWrappers.getType(Integer.class), ResolveConfig.defaultConfig);
    }

    public static Result<Byte> toByte(Object source) {
        return convert(TypeValueResolvers.getResolver(Byte.class), source, TypeWrappers.getType(Byte.class), ResolveConfig.defaultConfig);
    }

    public static Result<Short> toShort(Object source) {
        return convert(TypeValueResolvers.getResolver(Short.class), source, TypeWrappers.getType(Short.class), ResolveConfig.defaultConfig);
    }

    public static Result<Long> toLong(Object source) {
        return convert(TypeValueResolvers.getResolver(Long.class), source, TypeWrappers.getType(Long.class), ResolveConfig.defaultConfig);
    }

    public static Result<Character> toCharacter(Object source) {
        return convert(TypeValueResolvers.getResolver(Character.class), source, TypeWrappers.getType(Character.class), ResolveConfig.defaultConfig);
    }

    public static Result<Date> toDate(Object source) {
        return convert(TypeValueResolvers.getResolver(Date.class), source, TypeWrappers.getType(Date.class), ResolveConfig.defaultConfig);
    }
    public static Result<LocalDate> toLocalDate(Object source) {
        return convert(TypeValueResolvers.getResolver(LocalDate.class), source, TypeWrappers.getType(LocalDate.class), ResolveConfig.defaultConfig);
    }

    public static Result<LocalDateTime> toLocalDateTime(Object source) {
        return convert(TypeValueResolvers.getResolver(LocalDateTime.class), source, TypeWrappers.getType(LocalDateTime.class), ResolveConfig.defaultConfig);
    }

    public static Result<LocalTime> toLocalTime(Object source) {
        return convert(TypeValueResolvers.getResolver(LocalTime.class), source, TypeWrappers.getType(LocalTime.class), ResolveConfig.defaultConfig);
    }

    public static Result<Object[]> toArray(Object source) {
        return toArray(source, null);
    }

    public static <T> Result<T[]> toArray(Object source, TypeReference<T[]> typeReference) {
        if (typeReference != null) {
            TypeWrapper typeWrapper = TypeWrappers.getType(TypeReference.class, typeReference.getClass());
            return convert(TypeValueResolvers.getResolver(typeWrapper.getGeneric(0).resolve()), source, typeWrapper.getGeneric(0), ResolveConfig.defaultConfig);
        } else {
            return convert(TypeValueResolvers.getResolver(Object[].class), source, TypeWrappers.getType(Object[].class), ResolveConfig.defaultConfig);
        }
    }

    public static Result<String> toHex(Number number) {
        TypeValueResolver resolver = TypeValueResolvers.getResolver(String.class);
        return convert(resolver, number, TypeWrappers.getType(String.class), ResolveConfig.defaultConfig.convertFeature(new ConvertFeature[] {ConvertFeature.HEX}));
    }

    public static Result<String> toBinary(Number number) {
        TypeValueResolver resolver = TypeValueResolvers.getResolver(String.class);
        return convert(resolver, number, TypeWrappers.getType(String.class), ResolveConfig.defaultConfig.convertFeature(new ConvertFeature[] {ConvertFeature.BINARY}));
    }

    public static Result<String> toOct(Number number) {
        TypeValueResolver resolver = TypeValueResolvers.getResolver(String.class);
        return convert(resolver, number, TypeWrappers.getType(String.class), ResolveConfig.defaultConfig.convertFeature(new ConvertFeature[] {ConvertFeature.OCT}));

    }

    public static Result<String> toString(Object object) {
        TypeValueResolver resolver = TypeValueResolvers.getResolver(String.class);
        return convert(resolver, object, TypeWrappers.getType(String.class), ResolveConfig.defaultConfig);

    }

    public static Result<String> toString(Object object, ResolveConfig config) {
        TypeValueResolver resolver = TypeValueResolvers.getResolver(String.class);
        return convert(resolver, object, TypeWrappers.getType(String.class), Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig));

    }

    public static <T> Result<T> toEnum(Class<T> enumClass, Object object) {
        if (enumClass == null || !enumClass.isEnum()) {
            return DefaultResult.defaultSuccessResult(null);
        }
        return convert(EnumValueResolver.instance, object, TypeWrappers.getType(enumClass), ResolveConfig.defaultConfig);

    }

    private static <T> Result<T> convert(TypeValueResolver resolver, Object source, TypeWrapper targetType, ResolveConfig config) {
        if (resolver == null || targetType == null) {
            throw new IllegalArgumentException("param can not be null");
        }
        try {
            Result<T> result = resolver.resolve(source, targetType, Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig));
            if (result == null) {
                return DefaultResult.errorResult(null, Constants.FAIL_CODE, "convert error");
            } else if (!result.success() || result.instance() == null) {
                return DefaultResult.errorResult(result.instance(), Constants.FAIL_CODE, result.resultMsg());
            } else {
                return DefaultResult.defaultSuccessResult(result.instance());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
