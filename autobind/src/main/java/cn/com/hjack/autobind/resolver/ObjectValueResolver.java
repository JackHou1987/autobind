/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.validation.DefaultResult;

/**
 * @ClassName: ObjectValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月27日
 *
 */
public class ObjectValueResolver implements TypeValueResolver {

    public static ObjectValueResolver instance = new ObjectValueResolver();

    @Override
    public Result<Object> resolve(Object source, TypeWrapper targetType, ResolveConfig config) throws Exception {
        return DefaultResult.defaultSuccessResult(source);
    }

}
