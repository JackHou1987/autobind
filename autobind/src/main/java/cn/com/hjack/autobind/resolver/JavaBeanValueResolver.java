/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.ConvertFeature;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.mapper.BeanMapper;
import cn.com.hjack.autobind.validation.DefaultResult;
import cn.com.hjack.autobind.generator.ObjectGenerator;
import cn.com.hjack.autobind.utils.TypeUtils;

import java.util.Map;



/**
 * @ClassName: JavaBeanValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月16日
 *
 */
public class JavaBeanValueResolver extends AbstractTypeValueResolver {

    public static JavaBeanValueResolver instance = new JavaBeanValueResolver();

    private JavaBeanValueResolver() {

    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> Result<T> doResolveValue(Object object, TypeWrapper targetType, ResolveConfig config) throws Exception {
        if (object == null || targetType == null
                || !TypeUtils.isJavaBeanClass(targetType.resolve())) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "object can not be null");
        }
        if (!TypeUtils.isMapClass(object.getClass())
                && !TypeUtils.isJavaBeanClass(object.getClass())) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "object can not be null");
        }
        if (ConvertFeature.isEnabled(config.convertFeature(), ConvertFeature.LAZY_MODE)) {
            return ProxyValueResolver.instance.resolve(object, targetType, config);
        }
        if (config.fastMode()) {
            TypeValueResolver resolver = ObjectGenerator.instance.generateResolver(object.getClass(), targetType);
            if (resolver == null) {
                return DefaultResult.errorResult(null, Constants.FAIL_CODE, "can not create proxy class");
            } else {
                return resolver.resolve(object, targetType, config);
            }
        }
        BeanMapper<T> childMapper = new BeanMapper<>((T) targetType.resolve().newInstance(), targetType.resolveVariableContext(), config);
        Result<T> childResult;
        if (TypeUtils.isMapClass(object.getClass())) {
            childResult = childMapper.bindMapToBean((Map<String, Object>) object, config.validator());
        } else {
            if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.BEAN_TO_MAP_PROXY_DISABLE)) {
                childResult = childMapper.bindBeanToBean(object, config.validator());
            } else {
                Map<String, Object> proxyMap = ObjectGenerator.instance.generateJavaBeanToMapProxy(object.getClass(), object);
                childResult = childMapper.bindMapToBean(proxyMap, config.validator());
            }
        }
        DefaultResult<T> result = new DefaultResult<>();
        if (childResult == null || !childResult.success() || childResult.instance() == null) {
            result.setSuccess(false);
            if (childResult == null) {
                result.setResultCode(Constants.FAIL_CODE);
                result.setResultMsg(Constants.FAIL_MESSAGE);
            } else {
                result.setResultCode(Constants.FAIL_CODE);
                result.setResultMsg(childResult.resultMsg());
                result.setInstance(childResult.instance());
            }
        } else {
            result.setInstance(childResult.instance());
        }
        return result;
    }
}
