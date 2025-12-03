/**
 *
 */
package cn.com.hjack.autobind.binder;

import cn.com.hjack.autobind.ValidationErrors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * @author houqq
 * @date: 2025年8月29日
 */
public class DefaultValidationErrors implements ValidationErrors {

    private Map<String, String> errorMsg = new HashMap<>();

    @Override
    public void collectError(String fieldName, String msg) {
        errorMsg.put(fieldName, msg);
    }

    public Collection<String> getErrorMsgs() {
        return errorMsg.values();
    }

}
