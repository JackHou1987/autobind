/**
 *
 */
package cn.com.hjack.autobind.converter;

import java.util.Optional;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.generater.Generaters;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;

/**
 * 当生成的map、collection或者java bean对象时不立即转换其中值，等待第一次使用时进行转换
 * @author houqq
 * @date: 2025年8月13日
 */
public class LazyLoadValueResolver implements ResolvableConverter {

    public static LazyLoadValueResolver instance = new LazyLoadValueResolver();

    private LazyLoadValueResolver() {

    }

    @Override
    public <T> Result<T> convert(Object source, TypeWrapper targetType, ResolveConfig config) {
        DefaultResult<Object> defaultResult = new DefaultResult<Object>();
        if (TypeUtils.isMapClass(targetType.resolve())) {
            Object proxy = Generaters.lazyLoadProxyGenerater(targetType.resolve(), () -> {
                try {
                    ResolveConfig resoveConfig = Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig);
                    resoveConfig = resoveConfig.removeConvertFeature(ConvertFeature.LAZY_MODE);
                    Result<T> result = MapValueResolver.instance.convert(source, targetType, resoveConfig);
                    defaultResult.setResultMsg(result.resultMsg());
                    return result.instance();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).generate();
            defaultResult.setInstance(proxy);
            return CastUtils.castSafe(defaultResult);
        } else if (TypeUtils.isCollectionClass(targetType.resolve())) {
            Object proxy = Generaters.lazyLoadProxyGenerater(targetType.resolve(), () -> {
                try {
                    ResolveConfig resoveConfig = Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig);
                    resoveConfig = resoveConfig.removeConvertFeature(ConvertFeature.LAZY_MODE);
                    Result<T> result = CollectionValueResolver.instance.convert(source, targetType, resoveConfig);
                    defaultResult.setResultMsg(result.resultMsg());
                    return result.instance();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).generate();
            defaultResult.setInstance(proxy);
            return CastUtils.castSafe(defaultResult);
        } else if (TypeUtils.isJavaBeanClass(targetType.resolve())) {
            Object proxy = Generaters.lazyLoadProxyGenerater(targetType.resolve(), () -> {
                try {
                    ResolveConfig resoveConfig = Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig);
                    resoveConfig = resoveConfig.removeConvertFeature(ConvertFeature.LAZY_MODE);
                    Result<T> result = JavaBeanValueResolver.instance.convert(source, targetType, resoveConfig);
                    defaultResult.setResultMsg(result.resultMsg());
                    return result.instance();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).generate();
            defaultResult.setInstance(proxy);
            return CastUtils.castSafe(defaultResult);
        } else {
            throw new IllegalStateException("unkown type");
        }

    }

}
