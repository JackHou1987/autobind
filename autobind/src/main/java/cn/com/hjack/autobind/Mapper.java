/**
 *
 */
package cn.com.hjack.autobind;

import java.util.Map;

/**
 * javabean映射接口。其中javabean实例称为聚合量，其他类型称为标量，将聚合量先转换为标量再转换为另一种聚合量是类型转换一个常用方法，该接口主要提供三种映射
 * <ol>
 * <li>map到javabean映射
 * <li>javabean到map映射
 * <li>javabean到javabean映射
 * </ol>
 * 其中map转为bean或者bean转为bean后会对目标bean依据Validator做校验
 * @author houqq
 * @date: 2025年10月28日
 * @see cn.com.hjack.autobind.ResolvableConverter
 * @see cn.com.hjack.autobind.Validator
 */
public interface Mapper<T> {

    /**
     * map转为javabean，当javabean字段注解autofield指定recvFieldName则key为recvFieldName的值，否则key为字段的名称
     * <br> javabean字段值为map的value,当前Map找不到相应的key，则递归搜索map中的key
     * @param  map source map
     * @param  validator validator
     * @see cn.com.hjack.autobind.Validator
     */
    Result<T> mapToBean(Map<String, Object> map, Validator validator);

    /**
     * 按字段名称将javabean转为javabean，如果source javabean找不到相应的key，则递归搜索字段为javabean的字段
     * @param source javabean对象
     * @param validator Validator对象
     */
    Result<T> beanToBean(Object source, Validator validator);

    /**
     * 将javabean转换为map，如果javabean字段也为javabean,则递归转换
     * @param source javabean对象
     */
    Map<String, Object> beanToMap(T source);

    ResolveConfig getConfig();

    T getTarget();

}
