/**
 *
 */
package cn.com.hjack.autobind;

import java.lang.reflect.Type;
import java.util.Map;

/**
 *  封装了{@link java.lang.reflect.Type}类型，每个typewrapper均包含一个map结构用于保存泛型类型的type名称和实际类型的映射，用于上下文泛型解析。
 * <ol>
 * <li>{@link cn.com.hjack.autobind.TypeWrapper#resolve}方法会返回该类型的class。
 * <li>{@link cn.com.hjack.autobind.FieldTypeWrapper}类型特指javabean字段类型。
 * <li>每个类型都有一个varibale context(泛型上下文)，一种存储类型名和参数化类型的map结构，如当前类型是{@link cn.com.hjack.autobind.FieldTypeWrapper}则其泛型上下文是所声明的class的参数化类型名称和类型名map,
 * 当调用{@link cn.com.hjack.autobind.TypeWrapper#resolveVariableContext}方法时返回，
 * 得到的泛型map会保存在beanmapper或fieldtypewrapperimpl中，用于泛型解析。
 * </ol>
 * @author houqq
 * @date: 2025年6月23日
 * @see cn.com.hjack.autobind.FieldTypeWrapper
 */
public interface TypeWrapper {

    /**
     * 获取当前类型的参数化类型
     * @param 参数化类型索引
     * @return 参数化类型
     */
    TypeWrapper getGeneric(int index);

    /**
     * 获取当前类型所有参数化类型
     * @return: 参数化类型数组
     */
    TypeWrapper[] getGenerics();

    /**
     * 获得该类型的组件类型
     * @return: 组件类型
     */
    TypeWrapper getComponentType();

    /**
     * 获取当前类型的真实Type
     * @return: Type
     */
    Type getType();

    /**
     * 判断当前类型是否代表一个不指任何类型的空类型
     * @return: boolean
     */
    boolean emptyType();

    /**
     * 获取当前类型解析后的class
     */
    Class<?> resolve();

    /**
     * 获取当前类型解析后的class，如果为空则返回Object.class
     */
    Class<?> resolveOrObject();

    /**
     *   获取当前类型解析后的class，如果为空则抛出异常
     */
    Class<?> resolveOrThrow();

    /**
     * 获取当前类型解析后的class，如果为空则返回指定class
     */
    Class<?> resolveOrDefault(Class<?> defaultClass);

    /**
     * 返回当前类型参数化后的类型名称、实际类型map结构。
     * <ol>
     *   <li>当前类型为泛型或class，则返回空map
     *   <li>当前类型参数化类型且可解析，则将解析后的实际类型放入待返回的map中
     *   <li>当前类型参数化类型不可解析，则从variableContext中获取相应类型放入待返回map中
     *   <li>当前类型参数化类型不可解析，其variableContext中匹配不到实际类型，则返回object类型放入map中
     * </ol>
     * @return 当前参数化类型map,key:类型名称 value:解析后的实际类型
     */
    Map<String, TypeWrapper> resolveVariableContext();

    /**
     * 解析泛型,如果该泛型不能解析，则从当前类型的泛型上下文中获取，仍取不到则返回object类型
     * @param originGenericType 待解析的泛型
     * @return 解析后的目标类型
     */
    TypeWrapper resolveGeneric(TypeWrapper originGenericType);

    /**
     * 解析数组的组件类型
     * @param originComponentType 组件的原始类型
     * @return: 解析后的目标类型
     */
    TypeWrapper resolveComponentType(TypeWrapper originComponentType);

}
