/**
 *
 */
package cn.com.hjack.autobind.resolver;

import java.util.Map;

import cn.com.hjack.autobind.factory.TypeValueResolvers;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.factory.ConversionServiceProvider;
import cn.com.hjack.autobind.ConvertFeature;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.mapper.BeanMapper;
import cn.com.hjack.autobind.validation.DefaultResult;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.generator.ObjectGenerator;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;
import org.springframework.core.convert.ConversionService;


/**
 * @ClassName: MapValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月16日
 *
 */
public class MapValueResolver extends AbstractTypeValueResolver {

    public static MapValueResolver instance = new MapValueResolver();

    private MapValueResolver() {

    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> Result<T> doResolveValue(Object object, TypeWrapper targetType, ResolveConfig config) throws Exception {
        if (object == null || targetType == null
                || !TypeUtils.isMapClass(targetType.resolve())) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "source or target type can not be null");
        }
        if (!(object instanceof Map) && !TypeUtils.isJavaBeanClass(object.getClass())) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "source type must be java bean or map");
        }
        if (ConvertFeature.isEnabled(config.convertFeature(), ConvertFeature.LAZY_MODE)) {
            return ProxyValueResolver.instance.resolve(object, targetType, config);
        }
        if (config.fastMode() && TypeUtils.isJavaBeanClass(object.getClass())) {
            TypeValueResolver resolver = ObjectGenerator.instance.generateResolver(object.getClass(), targetType);
            if (resolver == null) {
                return DefaultResult.errorResult(null, Constants.FAIL_CODE, "can not create proxy class");
            } else {
                return resolver.resolve(object, targetType, config);
            }
        }
        Map<?, Object> source;
        if (TypeUtils.isMapClass(object.getClass())) {
            source = (Map<?, Object>) object;
        } else {
            if (ConvertFeature.isEnabled(config.convertFeature(), ConvertFeature.BEAN_TO_MAP_PROXY_DISABLE)) {
                source = BeanMapper.beanToMap(object.getClass(), object, config.mapConvertFeature());
            } else {
                source = ObjectGenerator.instance.generateJavaBeanToMapProxy(object.getClass(), object);
            }
        }
        TypeWrapper keyType = TypeWrappers.getAndResolveGenericType(targetType, 0);
        Class<?> keyClass = keyType.resolveOrObject();
        TypeValueResolver keyResolver = TypeValueResolvers.getResolver(keyClass);
        TypeWrapper valueType = TypeWrappers.getAndResolveGenericType(targetType, 1);
        Class<?> valueClass = valueType.resolveOrObject();
        TypeValueResolver valueResolver = TypeValueResolvers.getResolver(valueClass);
        DefaultResult<T> result = new DefaultResult<>();
        if (valueResolver != null && keyResolver != null) {
            Map<Object, Object> resultMap = TypeUtils.createMap(targetType.resolve());
            for (Map.Entry<?, Object> entry : source.entrySet()) {
                Object keyValue = this.resolveElement(keyResolver, keyType, config, entry.getKey());
                Object value = this.resolveElement(valueResolver, valueType, config, entry.getValue());
                resultMap.put(keyValue, value);
            }
            result.setInstance(CastUtils.castSafe(resultMap));
            return result;
        } else {
            ConversionService conversionService = ConversionServiceProvider.getConversionService(config);
            Map<Object, Object> resultMap = TypeUtils.createMap(targetType.resolve());
            for (Map.Entry<?, Object> entry : source.entrySet()) {
                Object keyValue = super.convert(entry.getKey(), keyClass, conversionService);
                Object value = super.convert(entry.getValue(), valueClass, conversionService);
                resultMap.put(keyValue, value);
            }
            result.setInstance(CastUtils.castSafe(resultMap));
            return result;
        }
    }

    private Object resolveElement(TypeValueResolver resolver, TypeWrapper type, ResolveConfig config, Object obj) {
        Result<Object> value = null;
        try {
            value = resolver.resolve(obj, type, config);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        if (!value.success() || value.instance() == null) {
            throw new IllegalStateException(value.resultMsg());
        } else {
            return value.instance();
        }
    }

}
