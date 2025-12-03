/**
 *
 */
package cn.com.hjack.autobind;

import java.lang.reflect.Type;

/**
 * 继承{@link cn.com.hjack.autobind.TypeWrapper}，代表一个javabean字段type
 * @author houqq
 * @date: 2025年6月23日
 * @see cn.com.hjack.autobind.TypeWrapper
 */
public interface FieldTypeWrapper extends TypeWrapper {

    /**
     * 获取field的class类型
     */
    Class<?> getFieldTypeClass();

    /**
     * 获取字段的泛化类型
     */
    Type getFieldGenericType();

}
