/**
 *
 */
package cn.com.hjack.autobind;


/**
 * @ClassName: Result
 * @Description: TODO
 * @author houqq
 * @date: 2025年7月21日
 *
 */
public interface Result<T> {

    /**
     * @Title: success
     * @Description: TODO
     * @param: @return
     * @return: boolean
     * @throws
     */
    boolean success();

    T instance();

    void instance(T instance);

    String resultMsg();

    void setResultMsg(String resultMsg);

    String getResultCode();

    void setResultCode(String resultCode);
}
