/**
 *
 */
package cn.com.hjack.autobind.converter;


import cn.com.hjack.autobind.ResolvableConverter;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.utils.CastUtils;

/**
 * @ClassName: ObjectValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月27日
 *
 */
public class ObjectValueResolver implements ResolvableConverter {

    public static ObjectValueResolver instance = new ObjectValueResolver();

    private ObjectValueResolver() {

    }

    @Override
    public <T> Result<T> convert(Object source, TypeWrapper targetType, ResolveConfig config) {
        return DefaultResult.successResult(CastUtils.castSafe(source));
    }

}
