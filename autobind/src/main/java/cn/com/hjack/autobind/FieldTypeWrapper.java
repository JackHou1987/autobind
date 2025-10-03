/**
 *
 */
package cn.com.hjack.autobind;

import java.lang.reflect.Type;


/**
 * @ClassName: FileTypeWrapper
 * @Description: 封装了一个类字段的type,该字段或者其参数化类型均代表一个FieldTypeWrapper，且其Field type wrapper对象的feild均指向同一个field
 * @author houqq
 * @date: 2025年6月23日
 */
public interface FieldTypeWrapper extends TypeWrapper {

    /**
     * @Title: getFieldTypeClass
     * @Description: 获取field的实际类型
     * @return: Class<?>
     * @throws
     */
    Class<?> getFieldTypeClass();

    /**
     * @Title: getFieldGenericType
     * @Description: 获取字段的generic type
     * @return: Type
     */
    Type getFieldGenericType();

}
