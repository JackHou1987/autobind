/**
 *
 */
package cn.com.hjack.autobind;

import java.lang.reflect.Field;
import java.lang.reflect.Type;


/**
 * @ClassName: FileTypeWrapper
 * @Description: 封装了一个类字段的type,该字段或者其参数化类型均代表一个FieldTypeWrapper，且其Field type wrapper对象的feild均指向同一个field
 * @author houqq
 * @date: 2025年6月23日
 */
public interface FieldTypeWrapper extends TypeWrapper {

    Field getField();

    /**
     * @Title: getFieldTypeClass
     * @Description: 获取字段实际的class,如果为泛型则返回Object或者Object数组类型
     * @return: Class<?>
     * @throws
     */
    Class<?> getFieldTypeClass();

    /**
     * @Title: getFieldGenericType
     * @Description: 获取当前type wrapper所属的field的generic type -> field.getGenericType(),该方法不同于getType(),
     * <br> getType是经过解析后返回的类型
     * @return: Type
     * @throws
     */
    Type getFieldGenericType();

}
