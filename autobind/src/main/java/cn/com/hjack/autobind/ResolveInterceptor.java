/**
 *
 */
package cn.com.hjack.autobind;

/**
 * @ClassName: ConvertFilter
 * @Description: TODO
 * @author houqq
 * @date: 2025年9月1日
 *
 */
public interface ResolveInterceptor {

    <T> Result<T> resolve(ResolveContext context) throws Exception;
}
