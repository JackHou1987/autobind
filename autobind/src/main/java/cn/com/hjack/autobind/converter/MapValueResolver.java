/**
 *
 */
package cn.com.hjack.autobind.converter;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.binder.BeanMappers;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;

import java.util.Map;



/**
 * map对象转换器
 * @author houqq
 * @date: 2025年6月16日
 */
public class MapValueResolver extends AbstractResolvableConverter {

    public static MapValueResolver instance = new MapValueResolver();

    private MapValueResolver() {

    }

    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        if (!TypeUtils.isMapClass(targetType.resolve())) {
            return DefaultResult.errorResult("source or target type can not be null");
        }
        if (source == null) {
            return DefaultResult.successResult(CastUtils.castSafe(TypeUtils.createMap(targetType.resolve())));
        }
        if (!(source instanceof Map) && !TypeUtils.isJavaBeanClass(source.getClass())) {
            return DefaultResult.errorResult("source type must be java bean or map");
        }
        if (ConvertFeature.isEnabled(config.convertFeature(), ConvertFeature.LAZY_MODE)) {
            return ResolvableConverters.getLazyLoadValueResolver().convert(source, targetType, config);
        }
        ResolvableConverter keyConverter = this.getConverter(targetType, 0);
        ResolvableConverter valueConverter = this.getConverter(targetType, 1);
        if (keyConverter == null || valueConverter == null) {
            return DefaultResult.errorResult("can not convert source to target, converter not found");
        }
        Map<Object, Object> resultMap = TypeUtils.createMap(targetType.resolve());
        Map<?, Object> mapSource = getAndConvertSource(source, targetType, config);
        TypeWrapper keyType = TypeWrappers.getAndResolveGenericType(targetType, 0);
        TypeWrapper valueType = TypeWrappers.getAndResolveGenericType(targetType, 1);
        for (Map.Entry<?, Object> entry : mapSource.entrySet()) {
            Result<Object> keyResult = keyConverter.convert(entry.getKey(), keyType, config);
            Result<Object> valueResult = valueConverter.convert(entry.getValue(), valueType, config);
            if (!keyResult.success()) {
                return DefaultResult.errorResult(CastUtils.castSafe(TypeUtils.createMap(targetType.resolve())), keyResult.resultMsg());
            }
            if (!valueResult.success()) {
                return DefaultResult.errorResult(CastUtils.castSafe(TypeUtils.createMap(targetType.resolve())), valueResult.resultMsg());
            }
            resultMap.put(keyResult.instance(), valueResult.instance());
        }
        return DefaultResult.successResult(CastUtils.castSafe(resultMap));
    }

    private Map<?, Object> getAndConvertSource(Object source, TypeWrapper targetType, ResolveConfig config) {
        if (source instanceof Map) {
            return CastUtils.castSafe(source);
        } else {
            return BeanMappers.getMapper(source.getClass(), targetType, config).beanToMap(source);
        }
    }

    private ResolvableConverter getConverter(TypeWrapper targetType, int genericIndex) {
        TypeWrapper genericType = TypeWrappers.getAndResolveGenericType(targetType, genericIndex);
        return ResolvableConverters.getConverter(genericType.resolveOrObject());
    }

}
