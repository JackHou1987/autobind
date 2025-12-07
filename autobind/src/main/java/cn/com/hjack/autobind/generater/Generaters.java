/**
 *
 */
package cn.com.hjack.autobind.generater;

import cn.com.hjack.autobind.*;

import java.util.function.Supplier;


/**
 * 代码生成接口工厂{@link cn.com.hjack.autobind.Generater},主要分为三种类型:
 * <ol><li>基于数组的代码生成器<li>延迟加载的代码生成<li>Mapper映射的代码生成</ol>
 * @author houqq
 * @date: 2025年10月31日
 * @see cn.com.hjack.autobind.generater.ArrayResolverGenerater
 * @see cn.com.hjack.autobind.generater.LazyLoadProxyGenerater
 * @see cn.com.hjack.autobind.generater.MapperGenerater
 */
public class Generaters {

    public static Generater<ResolvableConverter> arrayResolverGenerater(Class<?> sourceClass, TypeWrapper targetType) {
        return new ArrayResolverGenerater(sourceClass, targetType);
    }

    public static Generater<Object> lazyLoadProxyGenerater(Class<?> targetClass, Supplier<?> supplier) {
        return new LazyLoadProxyGenerater(targetClass, supplier);
    }

    public static Generater<Mapper<?>> mapperGenerater(Class<?> sourceClass, TypeWrapper targetType, ResolveConfig config) {
        return new MapperGenerater(sourceClass, targetType, config);
    }
}
