/**
 *
 */
package cn.com.hjack.autobind;

import cn.com.hjack.autobind.utils.Converter;

/**
 * 用于泛型类型解析
 * @author houqq
 * @date: 2025年10月21日
 * @see Converter
 */
public abstract class TypeReference<T> {

    private T instance;

    protected TypeReference(T instance) {
        this.instance = instance;
    }

    protected TypeReference() {
    }

    public T getInstance() {
        return instance;
    }
}
