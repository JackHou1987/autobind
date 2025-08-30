/**
 *
 */
package cn.com.hjack.autobind;


/**
 * @ClassName: ValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月16日
 *
 */
public interface TypeValueResolver {

    <T> Result<T> resolve(Object source, TypeWrapper targetType, ResolveConfig config) throws Exception;

}
