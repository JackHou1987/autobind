/**
 *
 */
package cn.com.hjack.autobind;

/**
 * @ClassName: ConvertHandler
 * @Description: TODO
 * @author houqq
 * @date: 2025年9月4日
 */
public interface ConvertInterceptor {

    Object beforeConvert(Object source, TypeWrapper targetType, ResolveConfig config);

    Object postConvert(Object convertedObject, TypeWrapper targetType, ResolveConfig config);

}
