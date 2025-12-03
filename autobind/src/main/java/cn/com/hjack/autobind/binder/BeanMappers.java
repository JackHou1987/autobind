/**
 *
 */
package cn.com.hjack.autobind.binder;

import cn.com.hjack.autobind.Mapper;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.generater.Generaters;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;

import java.util.HashMap;
import java.util.Optional;


/**
 * @ClassName: Mappers
 * @Description: TODO
 * @author houqq
 * @date: 2025年11月10日
 *
 */
public class BeanMappers {

    public static <T> Mapper<T> getMapper(Class<?> sourceClass, TypeWrapper targetType, ResolveConfig config) {
        if (targetType == null) {
            throw new IllegalStateException("get mapper error, unkown type");
        } else {
            ResolveConfig resolveConfig = Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig);
            Class<?> targetClass = targetType.resolveOrObject();
            if (TypeUtils.isJavaBeanClass(targetClass)) {
                if (resolveConfig.fastMode()) {
                    return CastUtils.castSafe(Generaters.mapperGenerater(sourceClass, targetType, resolveConfig).generate());
                } else {
                    try {
                        return new DefaultBeanMapper<>(CastUtils.castSafe(targetType.resolve().newInstance()), targetType.resolveVariableContext(), resolveConfig);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            } else if (TypeUtils.isMapClass(targetClass)) {
                if (resolveConfig.fastMode()) {
                    return CastUtils.castSafe(Generaters.mapperGenerater(sourceClass, targetType, resolveConfig).generate());
                } else {
                    try {
                        return new DefaultBeanMapper<>(CastUtils.castSafe(TypeUtils.createMap(targetClass)), new HashMap<>(), config);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            } else {
                throw new IllegalStateException("get mapper error, target type must be java bean or map");
            }
        }
    }
}
