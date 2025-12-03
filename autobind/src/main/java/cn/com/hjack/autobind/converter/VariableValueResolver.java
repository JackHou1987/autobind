/**
 *
 */
package cn.com.hjack.autobind.converter;


import cn.com.hjack.autobind.ResolvableConverter;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.DefaultResult;

/**
 *    泛型解析器
 * @author houqq
 * @date: 2025年6月16日
 *
 */
public class VariableValueResolver extends AbstractResolvableConverter {

    public static VariableValueResolver instance = new VariableValueResolver();

    private VariableValueResolver() {

    }

    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        if (source == null) {
            return DefaultResult.successResult();
        }
        ResolvableConverter converter = ResolvableConverters.getConverter(targetType.resolve());
        if (converter != null) {
            Result<T> result = converter.convert(source, targetType, config);
            if (!result.success()) {
                return DefaultResult.errorResult(result.resultMsg());
            } else {
                return DefaultResult.successResult(result.instance());
            }
        } else {
            return DefaultResult.errorResult("can not convert source to target, can not find converter");
        }
    }

}
