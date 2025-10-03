/**
 *
 */
package cn.com.hjack.autobind;

import java.lang.reflect.Type;
import java.util.Map;


/**
 * @ClassName: TypeWrapper
 * @Description: 类型type实例的封装,每个typewrapper均有一个variable context结构，
 * <br> 保存了参数化类型的type名称和实际类型的映射，用于上下文泛型解析
 * @author houqq
 * @date: 2025年6月23日
 *
 */
public interface TypeWrapper {

    /**
     * @Title: getGeneric
     * @Description: 获取当前类型的参数化类型
     * @param: 参数化类型索引
     * @return: 参数化类型
     */
    TypeWrapper getGeneric(int index);

    /**
     * @Title: getGenerics
     * @Description: 获取当前类型的参数化类型
     * @return: 参数化类型
     */
    TypeWrapper[] getGenerics();

    /**
     * @Title: getComponentType
     * @Description: 获得该类型的组件类型
     * @return: 组件类型
     */
    TypeWrapper getComponentType();

    /**
     * @Title: getType
     * @Description: 获取当前类型的Type
     * @return: Type
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
     * @Description: 获取当前类型解析后的实际类型
     * @return: Class<?>
     */
    Class<?> resolve();

    Class<?> resolveOrObject();

    Class<?> resolveOrThrow();

    Class<?> resolveOrDefault(Class<?> defaultClass);

    /**
     * @Title: resolveVariableContext
     * @Description: 返回当前类型参数化类型名称、实际类型映射。
     *   当前参数化类型若为泛型则尝试从父类型的variableContext中找到匹配类型,如果匹配不到则返回Object
     * @return: Map<String,TypeWrapper>
     */
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
     */
    TypeWrapper resolveComponentType(TypeWrapper originComponentType);

}
