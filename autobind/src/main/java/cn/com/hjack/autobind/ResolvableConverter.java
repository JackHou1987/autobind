/**
 *
 */
package cn.com.hjack.autobind;

import cn.com.hjack.autobind.converter.*;

/**
 *  一种可解析的类型转换器，内部预置了通用的转换器，分为
 * <ol>
 * <li>map conveter {@link MapConverter}
 * <li>array conveter {@link ArrayConverter}
 * <li>collection conveter {@link CollectionConverter}
 * <li>number conveter {@link NumberConverter}
 * <li>String conveter {@link StringConverter}
 * </ol>等，可用于大部分类型转换场景
 * @author houqq
 * @date: 2025年6月16日
 * @see cn.com.hjack.autobind.TypeWrapper
 * @see cn.com.hjack.autobind.ResolveConfig
 * @see cn.com.hjack.autobind.Result
 * @see cn.com.hjack.autobind.converter.AbstractResolvableConverter
 */
public interface ResolvableConverter {

    /**
     * 将源对象转为目标对象，目标类型是{@link cn.com.hjack.autobind.TypeWrapper}，
     * 是一种封装了Type可解析包括泛型的类型，{@link cn.com.hjack.autobind.ResolveConfig}是解析时的配置
     * @param source 源对象
     * @param targetType 目标类型
     * @param config 运行时配置
     * @return 返回结果
     */
    <T> Result<T> convert(Object source, TypeWrapper targetType, ResolveConfig config);

}
