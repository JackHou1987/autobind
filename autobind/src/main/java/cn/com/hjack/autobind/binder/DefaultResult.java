/**
 *
 */
package cn.com.hjack.autobind.binder;

import cn.com.hjack.autobind.Constants;
import cn.com.hjack.autobind.Result;
import com.alibaba.fastjson.JSONObject;


/**
 *    缺省Result
 * @author houqq
 * @date: 2025年7月9日
 */
public class DefaultResult<T> implements Result<T> {

    private boolean success;

    private String resultCode;

    private String resultMsg;

    private T instance;

    public DefaultResult(T instance, boolean success, String resultCode, String resultMsg) {
        this.instance = instance;
        this.success = success;
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    public DefaultResult() {
        success = true;
        resultCode = Constants.SUCCESS_CODE;
        resultMsg = Constants.SUCCESS_MESSAGE;
    }

    @Override
    public boolean success() {
        return success;
    }

    public static <T> Result<T> errorResult(T instance, String resultCode, String resultMsg) {
        return new DefaultResult<>(instance, false, resultCode, resultMsg);
    }

    public static <T> Result<T> errorResult(T instance, String resultMsg) {
        return new DefaultResult<>(instance, false, Constants.FAIL_CODE, resultMsg);
    }

    public static <T> Result<T> errorResult(String resultCode, String resultMsg) {
        return new DefaultResult<>(null, false, resultCode, resultMsg);
    }

    public static <T> Result<T> errorResult(String resultMsg) {
        return new DefaultResult<>(null, false, Constants.FAIL_CODE, resultMsg);
    }

    public static <T> Result<T> errorResult(T instance) {
        return new DefaultResult<>(instance, false, Constants.FAIL_CODE, Constants.FAIL_MESSAGE);
    }

    public static <T> Result<T> errorResult() {
        return new DefaultResult<>(null, false, Constants.FAIL_CODE, Constants.FAIL_MESSAGE);
    }

    public static <T> Result<T> successResult(T instance) {
        return new DefaultResult<>(instance, true, Constants.SUCCESS_CODE, Constants.SUCCESS_MESSAGE);
    }

    public static <T> Result<T> successResult() {
        return new DefaultResult<>(null, true, Constants.SUCCESS_CODE, Constants.SUCCESS_MESSAGE);
    }

    @Override
    public T instance() {
        return instance;
    }

    @Override
    public String resultMsg() {
        return resultMsg;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }

    @Override
    public String toString() {
        return "DefaultResult [success=" + success + ", resultCode=" + resultCode + ", resultMsg=" + resultMsg
                + ", instance=" + JSONObject.toJSONString(instance) + "]";
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    @Override
    public void instance(T instance) {
        this.instance = instance;
    }

    public void error(String errorMsg) {
        this.setSuccess(false);
        this.setResultCode(Constants.FAIL_CODE);
        this.setResultMsg(errorMsg);
    }
}
