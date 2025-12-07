/**
 *
 */
package cn.com.hjack.autobind;

import java.util.HashMap;
import java.util.Map;

import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.ServiceLoaderUtils;
import org.apache.commons.collections4.MapUtils;


/**
 *  加载SPI converter
 * @author houqq
 * @date: 2025年10月21日
 */
public class ConverterProvider {

    private static Map<Class<?>, Map<Class<?>, Converter<?, ?>>> converterMap = new HashMap<>();

    static {
        ServiceLoaderUtils.loadClass(Converter.class).forEach(converter -> {
            TypeWrapper typeWrapper = TypeWrappers.getType(converter.getClass());
            Class<?> sourceClass = typeWrapper.getGeneric(0).resolveOrObject();
            Class<?> targetClass = typeWrapper.getGeneric(1).resolveOrObject();
            Map<Class<?>, Converter<?, ?>> targetConverterMap = converterMap.computeIfAbsent(sourceClass, (key) -> new HashMap<>());
            targetConverterMap.computeIfAbsent(targetClass, (key) -> converter);
        });
    }

    public static <S, T> Converter<S, T> getConfigConverter(Class<S> sourceClass, Class<T> targetClass) {
        if (sourceClass == null || targetClass == null) {
            return null;
        } else {
            Map<Class<?>, Converter<?, ?>> targetConverterMap = converterMap.get(sourceClass);
            if (MapUtils.isEmpty(targetConverterMap)) {
                return null;
            } else {
                return CastUtils.castSafe(targetConverterMap.get(targetClass));
            }
        }
    }

}
