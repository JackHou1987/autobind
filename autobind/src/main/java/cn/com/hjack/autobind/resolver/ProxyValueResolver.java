/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.ConvertFeature;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.validation.DefaultResult;
import cn.com.hjack.autobind.generator.ObjectGenerator;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;

import java.util.Optional;


/**
 * @ClassName: ProxyValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月13日
 *
 */
public class ProxyValueResolver implements TypeValueResolver {

    public static ProxyValueResolver instance = new ProxyValueResolver();

    private ProxyValueResolver() {

    }

    @Override
    public <T> Result<T> resolve(Object source, TypeWrapper targetType, ResolveConfig config) throws Exception {
        if (targetType == null || targetType.resolve() == null) {
            throw new IllegalStateException("target type can not be null");
        }
        DefaultResult<Object> defaultResult = new DefaultResult<Object>();
        if (TypeUtils.isMapClass(targetType.resolve())) {
            Object proxy = ObjectGenerator.instance.generateLazyLoadProxy(targetType.resolve(), () -> {
                try {
                    ResolveConfig resoveConfig = Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig).clone();
                    resoveConfig = resoveConfig.removeConvertFeature(ConvertFeature.LAZY_MODE);
                    Result<T> result = MapValueResolver.instance.resolve(source, targetType, resoveConfig);
                    defaultResult.setResultMsg(result.resultMsg());
                    return result.instance();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            defaultResult.setInstance(proxy);
            return CastUtils.castSafe(defaultResult);
        } else if (TypeUtils.isCollectionClass(targetType.resolve())) {
            Object proxy = ObjectGenerator.instance.generateLazyLoadProxy(targetType.resolve(), () -> {
                try {
                    ResolveConfig resoveConfig = Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig).clone();
                    resoveConfig = resoveConfig.removeConvertFeature(ConvertFeature.LAZY_MODE);
                    Result<T> result = CollectionValueResolver.instance.resolve(source, targetType, resoveConfig);
                    defaultResult.setResultMsg(result.resultMsg());
                    return result.instance();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            defaultResult.setInstance(proxy);
            return CastUtils.castSafe(defaultResult);
        } else if (TypeUtils.isJavaBeanClass(targetType.resolve())) {
            Object proxy = ObjectGenerator.instance.generateLazyLoadProxy(targetType.resolve(), () -> {
                try {
                    ResolveConfig resoveConfig = Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig).clone();
                    resoveConfig = resoveConfig.removeConvertFeature(ConvertFeature.LAZY_MODE);
                    Result<T> result = JavaBeanValueResolver.instance.resolve(source, targetType, resoveConfig);
                    defaultResult.setResultMsg(result.resultMsg());
                    return result.instance();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            defaultResult.setInstance(proxy);
            return CastUtils.castSafe(defaultResult);
        } else {
            throw new IllegalStateException("unkown type");
        }

    }

}
