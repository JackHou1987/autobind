/**
 *
 */
package cn.com.hjack.autobind.converter;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.utils.CastUtils;
import com.google.common.base.Strings;


/**
 * @ClassName: AbstractResolvableConverter
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月16日
 */
public abstract class AbstractResolvableConverter implements ResolvableConverter {

    private Map<Class<?>, Map<Class<?>, InternalConverter<Object, ResolveConfig, Object>>> internalConverters = new HashMap<>();

    public AbstractResolvableConverter() {

    }

    @Override
    public <T> Result<T> convert(Object source, TypeWrapper targetType, ResolveConfig config) {
        if (targetType == null || targetType.resolve() == null) {
            return DefaultResult.errorResult("target type can not be null");
        }
        if (config == null) {
            config = ResolveConfig.defaultConfig;
        }
        if (source == null) {
            if (!Strings.isNullOrEmpty(config.defaultValue())) {
                source = config.defaultValue();
            }
            return doConvert(source, targetType, config);
        } else {
            if (config.getCustomConverter() == null) {
                Converter<Object, T> configConverter = CastUtils.castSafe(ConverterProvider.getConfigConverter(source.getClass(), targetType.resolve()));
                if (configConverter != null) {
                    return DefaultResult.successResult(configConverter.convert(source));
                } else {
                    return doConvert(source, targetType, config);
                }
            } else {
                Converter<Object, T> customConverter = CastUtils.castSafe(config.getCustomConverter());
                return DefaultResult.successResult(customConverter.convert(source));
            }
        }
    }

    protected abstract <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config);

    protected <S, T> void registerInternalConverter(Class<S> sourceClass, Class<T> targetClass, InternalConverter<S, ResolveConfig, T> converter) {
        if (sourceClass == null || targetClass == null || converter == null) {
            throw new IllegalArgumentException("register internal converter error, source type or target type or converter can not be null");
        }
        Map<Class<?>, InternalConverter<Object, ResolveConfig, Object>> targetMap = this.internalConverters.computeIfAbsent(sourceClass, (key) -> {
            return new HashMap<>();
        });
        targetMap.put(targetClass, CastUtils.castSafe(converter));
    }

    protected <S, T> InternalConverter<S, ResolveConfig, T> getInternalConverter(Class<S> sourceType, Class<T> targetType) {
        if (sourceType == null || targetType == null) {
            return null;
        } else {
            return CastUtils.castSafe(Optional.ofNullable(internalConverters.get(sourceType)).orElse(new HashMap<>()).get(targetType));
        }
    }

    @FunctionalInterface
    public interface InternalConverter<S, U, T> {
        T convert(S t, U u) throws Exception;

    }

}
