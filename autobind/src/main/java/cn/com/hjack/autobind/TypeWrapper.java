/**
 *
 */
package cn.com.hjack.autobind;

import java.lang.reflect.Type;
import java.util.Map;


/**
 * @ClassName: TypeWrapper
 * @Description: 类型type实例的封装,每个typewrapper均有一个variable context结构，
 * <br> 保存了其字段本身或者参数化类型的type名称及实际的typewrapper对象映射，用于泛型解析
 * @author houqq
 * @date: 2025年6月23日
 *
 */
public interface TypeWrapper {

    TypeWrapper getGeneric(int index);

    TypeWrapper[] getGenerics();

    /**
     * @Title: getComponentType
     * @Description: 获得该类型的component type
     * @return: TypeWrapper
     * @throws
     */
    TypeWrapper getComponentType();

    /**
     * @Title: getType
     * @Description: 获取typewrapper的generic type
     * @param: @return
     * @return: Type
     * @throws
     */
    Type getType();


    /**
     * @Title: none
     * @Description: 判断该type是否代表一个不指任何类型的空类型
     * @param: @return
     * @return: boolean
     * @throws
     */
    boolean emptyType();

    /**
     * @Title: resolve
     * @Description: 获取解析后的实际类型
     * @return: Class<?>
     */
    Class<?> resolve();

    Class<?> resolveOrObject();

    Class<?> resolveOrThrow();

    Class<?> resolveOrDefault(Class<?> defaultClass);

    Map<String, TypeWrapper> resolveVariableContext();

    /**
     * @Title: resolveGenericType
     * @Description: 解析参数化类型
     * @param: 参数化原始类型,可能就是如T、E之类的泛型
     * @return: 解析后的目标类型
     */
    TypeWrapper resolveGeneric(TypeWrapper originGenericType);

    /**
     * @Title: resolveArrayOrignalComponentType
     * @Description: 解析数组的组件类型
     * @param: 组件的原始类型
     * @return: 解析后的目标类型
     * @throws
     */
    TypeWrapper resolveComponentType(TypeWrapper originComponentType);

}
