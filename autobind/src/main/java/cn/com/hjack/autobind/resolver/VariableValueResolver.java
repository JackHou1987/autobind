/**
 *
 */
package cn.com.hjack.autobind.resolver;


import cn.com.hjack.autobind.factory.TypeValueResolvers;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.factory.ConversionServiceProvider;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.validation.DefaultResult;
import cn.com.hjack.autobind.utils.CastUtils;
import org.springframework.core.convert.ConversionService;


/**
 * @ClassName: VariableValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月16日
 *
 */
public class VariableValueResolver extends AbstractTypeValueResolver {

    public static VariableValueResolver instance = new VariableValueResolver();

    private VariableValueResolver() {

    }

    @Override
    protected <T> Result<T> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config) throws Exception {
        if (source == null || targetType == null) {
            return DefaultResult.errorResult(null, "", "object can not be null");
        }
        TypeValueResolver valueResolver = TypeValueResolvers.getResolver(targetType.resolve());
        DefaultResult<T> result = new DefaultResult<>();
        if (valueResolver != null) {
            Result<Object> childResult = valueResolver.resolve(source, targetType, config);
            if (childResult == null || !childResult.success() || childResult.instance() == null) {
                result.setSuccess(false);
                if (childResult == null) {
                    result.setResultCode(Constants.FAIL_CODE);
                    result.setResultMsg(Constants.FAIL_MESSAGE);
                } else {
                    result.setResultMsg(childResult.resultMsg());
                    result.setResultCode(Constants.FAIL_CODE);
                    result.setInstance(CastUtils.castSafe(childResult.instance()));
                }
            } else {
                result.setInstance(CastUtils.castSafe(childResult.instance()));
            }
            return result;
        } else {
            ConversionService conversionService = ConversionServiceProvider.getConversionService(config);
            Class<?> actualCls = targetType.resolve();
            if (actualCls == null) {
                Object value = convert(source, Object.class, conversionService);
                if (value != null) {
                    result.setInstance(CastUtils.castSafe(value));
                    return result;
                } else {
                    result.setResultCode(Constants.FAIL_CODE);
                    result.setResultMsg(Constants.FAIL_MESSAGE);
                    return result;
                }
            } else {
                Object value = convert(source, actualCls, conversionService);
                if (value != null) {
                    result.setInstance(CastUtils.castSafe(value));
                    return result;
                } else {
                    result.setResultCode(Constants.FAIL_CODE);
                    result.setResultMsg(Constants.FAIL_MESSAGE);
                    return result;
                }
            }
        }
    }

}
