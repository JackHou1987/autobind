/**
 *
 */
package cn.com.hjack.autobind;

/**
 * @ClassName: ClassValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月1日
 *
 */
public interface ClassValueResolver extends TypeValueResolver {

    <T> Result<T> resolve(Object source, Class<?> targetClass, ResolveConfig config) throws Exception;
}
