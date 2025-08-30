package cn.com.hjack.autobind.utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cn.com.hjack.autobind.TypeReference;
import cn.com.hjack.autobind.factory.ConversionServiceProvider;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.validation.DefaultResult;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.factory.TypeValueResolvers;
import org.springframework.core.convert.ConversionService;


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
        if (source == null || targetType == null) {
            throw new IllegalArgumentException("source or target type can not be null");
        }

        TypeValueResolver resolver = TypeValueResolvers.getResolver(targetType);

        if (resolver != null) {
            return convert(resolver, source, TypeWrappers.getType(targetType), config);
        }

        ConversionService conversionService = ConversionServiceProvider.getConversionService(config);

        if (conversionService.canConvert(source.getClass(), targetType)) {
            return DefaultResult.defaultSuccessResult(conversionService.convert(source, targetType));
        }

        return DefaultResult.errorResult(null, Constants.FAIL_CODE, "can not convert source to target");
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> toObject(Object source, TypeReference<T> reference, ResolveConfig config) {
        if (source == null || reference == null) {
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

        ConversionService conversionService = ConversionServiceProvider.getConversionService(config);

        if (conversionService.canConvert(source.getClass(), targetType.resolve())) {
            return (Result<T>) DefaultResult.defaultSuccessResult(conversionService.convert(source, targetType.resolve()));
        }

        return DefaultResult.errorResult(null, Constants.FAIL_CODE, "can not convert source to target");
    }

    public static <T> Result<T> toObject(Object source, TypeReference<T> reference) {
        return toObject(source, reference, null);
    }

    public static <T> Result<T> toJavaBean(Object source, Class<T> targetType, ResolveConfig config) {
        if (source == null || targetType == null) {
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
        if (source == null) {
            throw new IllegalArgumentException("source can not be null");
        }
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

    public static <T> Result<List<T>> toList(Object source, ResolveConfig registry) {
        return toList(source, null, registry);
    }

    public static <T> Result<List<T>> toList(Object source) {
        return toList(source, null, null);
    }

    public static Date toDate(Object source) {
        return null;
    }

    public static <T> Result<List<T>> toList(Object source, TypeReference<List<T>> typeReference, ResolveConfig config) {
        if (source == null) {
            throw new IllegalArgumentException("source can not be null");
        }
        if (!TypeUtils.isCollectionClass(source.getClass())
                && !TypeUtils.isArrayClass(source.getClass())) {
            throw new IllegalArgumentException("source type must be collection or error");
        }
        if (typeReference != null) {
            TypeWrapper typeWrapper = TypeWrappers.getType(TypeReference.class, typeReference.getClass());
            return convert(TypeValueResolvers.getResolver(typeWrapper.getGeneric(0).resolve()), source, typeWrapper.getGeneric(0), config);
        } else {
            return convert(TypeValueResolvers.getResolver(List.class), source, TypeWrappers.getType(List.class), config);
        }

    }

    private static <T> Result<T> convert(TypeValueResolver resolver, Object source, TypeWrapper targetType, ResolveConfig config) {
        if (resolver == null || source == null || targetType == null) {
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
