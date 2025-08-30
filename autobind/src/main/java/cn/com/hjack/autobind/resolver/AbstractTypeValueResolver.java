/**
 *
 */
package cn.com.hjack.autobind.resolver;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cn.com.hjack.autobind.Converter;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.validation.DefaultResult;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringUtils;


/**
 * @ClassName: AbstractValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月16日
 *
 */
public abstract class AbstractTypeValueResolver implements TypeValueResolver {

    private Map<Class<?>, Map<Class<?>, InternalConverter<Object, ResolveConfig, Object>>> internalConverters = new HashMap<>();

    public AbstractTypeValueResolver() {

    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> Result<T> resolve(Object source, TypeWrapper targetType, ResolveConfig config) throws Exception {
        if (targetType == null || targetType.resolve() == null) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "source or target type can not be null");
        }
        if (config == null) {
            config = ResolveConfig.defaultConfig;
        }
        if (source == null) {
            if (!StringUtils.isEmpty(config.defaultValue())) {
                source = config.defaultValue();
            }
            return doResolveValue(source, targetType, config);
        } else {
            Converter<?, ?> converter = config.getCustomConverter();
            if (converter == null) {
                return doResolveValue(source, targetType, config);
            } else {
                T value = ((Converter<Object, T>) converter).convert(source);
                if (value != null) {
                    return DefaultResult.defaultSuccessResult(value);
                } else {
                    return this.doResolveValue(source, targetType, config);
                }
            }
        }
    }

    protected abstract <T> Result<T> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config) throws Exception;

    @SuppressWarnings("unchecked")
    protected <S, T> void registerInternalConverter(Class<S> sourceType, Class<T> targetType, InternalConverter<S, ResolveConfig, T> converter) {
        if (sourceType == null || targetType == null || converter == null) {
            throw new IllegalArgumentException("register fail, source type or target type or converter can not be null");
        }
        Map<Class<?>, InternalConverter<Object, ResolveConfig, Object>> targetMap = this.internalConverters.computeIfAbsent(sourceType, (key) -> {
            return new HashMap<>();
        });
        targetMap.put((Class<?>) targetType, (InternalConverter<Object, ResolveConfig, Object>) converter);
    }

    @SuppressWarnings("unchecked")
    protected <S, T> InternalConverter<S, ResolveConfig, T> getInternalConverter(Class<S> sourceType, Class<T> targetType) {
        if (sourceType == null || targetType == null) {
            return null;
        } else {
            return (InternalConverter<S, ResolveConfig, T>) Optional.ofNullable(internalConverters.get(sourceType)).orElse(new HashMap<>()).get(targetType);
        }
    }

    protected Object convertOrThrow(Object value, Class<?> targetType, ConversionService conversionService) {
        if (conversionService == null) {
            throw new IllegalStateException("can not convert, source type: " + value.getClass() + " target type: " + targetType);
        } else {
            if (!conversionService.canConvert(value.getClass(), targetType)) {
                throw new IllegalStateException("can not convert, source type: " + value.getClass() + " target type: " + targetType);
            } else {
                try {
                    return conversionService.convert(value, targetType);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    protected Object convert(Object value, Class<?> targetType, ConversionService conversionService) {
        if (value == null || conversionService == null) {
            return null;
        } else {
            if (!conversionService.canConvert(value.getClass(), targetType)) {
                return null;
            } else {
                return conversionService.convert(value, targetType);
            }
        }
    }

    @FunctionalInterface
    public interface InternalConverter<S, U, T> {

        T convert(S t, U u) throws Exception;

    }

}
