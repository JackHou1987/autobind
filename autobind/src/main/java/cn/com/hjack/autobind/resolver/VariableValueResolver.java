/**
 *
 */
package cn.com.hjack.autobind.resolver;


import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.factory.TypeValueResolvers;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.validation.DefaultResult;

/**
 * @ClassName: VariableValueResolver
 * @Description: 泛型解析器
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
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "can not convert source to target, can not find converter");
        }
    }

}
