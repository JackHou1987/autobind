/**
 *
 */
package cn.com.hjack.autobind;

import java.util.Map;

/**
 * @ClassName: ResolveContext
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月28日
 *
 */
public interface ResolveContext {

    ResolveConfig getConfig();

    Map<String, Object> getAttributes();

    TypeValueResolver getResolver();

    TypeWrapper getTargetType();

    Object getSource();

    void setAttribute(String key, Object value);

    Object getAttribute(String key);

    <T> Result<T> resolve() throws Exception;
}
