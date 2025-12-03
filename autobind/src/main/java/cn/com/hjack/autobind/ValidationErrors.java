/**
 *
 */
package cn.com.hjack.autobind;

/**
 * @ClassName: ValidationErrors
 * @Description: TODO
 * @author houqq
 * @date: 2025年10月21日
 *
 */
public interface ValidationErrors {

    void collectError(String fieldName, String msg);
}
