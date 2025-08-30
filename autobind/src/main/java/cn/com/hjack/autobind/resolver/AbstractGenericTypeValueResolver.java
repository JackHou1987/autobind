/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.factory.TypeValueResolvers;
import org.springframework.core.convert.ConversionService;

import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.validation.DefaultResult;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.factory.ConversionServiceProvider;

/**
 * @ClassName: AbstractGenericTypeValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年7月15日
 *
 */
public abstract class AbstractGenericTypeValueResolver extends AbstractTypeValueResolver {

    @Override
    protected <T> Result<T> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config)
            throws Exception {
        if (source == null || targetType == null || targetType.resolve() == null) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "source or target type can not be null");
        }
        TypeWrapper genericType = targetType.resolveGeneric(targetType.getGeneric(0));
        if (genericType == null || genericType.resolve() == null
                || genericType.resolve() == Object.class) {
            return DefaultResult.defaultSuccessResult(this.createObjectAndSetGenericValue(targetType.resolve(), source));
        } else {
            DefaultResult<T> result = new DefaultResult<>();
            TypeValueResolver valueResolver = TypeValueResolvers.getResolver(genericType.resolve());
            if (valueResolver != null) {
                Result<Object> childResult = valueResolver.resolve(source, genericType, config);
                if (childResult == null || !childResult.success() || childResult.instance() == null) {
                    result.setSuccess(false);
                    if (childResult == null) {
                        result.setResultCode(Constants.FAIL_CODE);
                        result.setResultMsg(Constants.FAIL_MESSAGE);
                    } else {
                        result.setResultCode(Constants.FAIL_CODE);
                        result.setResultMsg(childResult.resultMsg());
                        T instance = this.createObjectAndSetGenericValue(targetType.resolve(), childResult.instance());
                        result.setInstance(instance);
                    }
                } else {
                    T instance = this.createObjectAndSetGenericValue(targetType.resolve(), childResult.instance());
                    result.setInstance(instance);
                }
                return result;
            } else {
                ConversionService conversionService = ConversionServiceProvider.getConversionService(config);
                Object value = super.convert(source, genericType.resolve(), conversionService);
                if (value != null) {
                    T instance = this.createObjectAndSetGenericValue(targetType.resolve(), value);
                    return DefaultResult.defaultSuccessResult(instance);
                } else {
                    return DefaultResult.errorResult(createObject(targetType.resolve()));
                }

            }
        }
    }

    protected abstract <T> T createObjectAndSetGenericValue(Class<?> targetType, Object value);

    protected abstract <T> T createObject(Class<?> targetType);

}
