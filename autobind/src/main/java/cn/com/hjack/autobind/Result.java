/**
 *
 */
package cn.com.hjack.autobind;


/**
 * {@link cn.com.hjack.autobind.ResolvableConverter#convert}方法返回结果
 * @author houqq
 * @date: 2025年7月21日
 */
public interface Result<T> {

    boolean success();

    T instance();

    void instance(T instance);

    String resultMsg();

    void setResultMsg(String resultMsg);

    String getResultCode();

    void setResultCode(String resultCode);
}
