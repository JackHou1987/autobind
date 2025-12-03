/**
 *
 */
package cn.com.hjack.autobind.converter;

import cn.com.hjack.autobind.ResolvableConverter;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.generater.Generaters;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.util.List;

/**
 * @ClassName: ArrayValueResolver
 * @Description: 数组转换器
 * @author houqq
 * @date: 2025年6月16日
 *
 */
public class ArrayValueResolver extends AbstractResolvableConverter {

    public static ArrayValueResolver instance = new ArrayValueResolver();

    private ArrayValueResolver() {

    }

    @Override
    protected <T> Result<T> doConvert(Object object, TypeWrapper targetType, ResolveConfig config) {
        if (!TypeUtils.isArrayClass(targetType.resolve())) {
            return DefaultResult.errorResult("target type must be array");
        }
        if (config != null && config.fastMode()) {
            ResolvableConverter converter = Generaters.arrayResolverGenerater(object.getClass(), targetType).generate();
            if (converter == null) {
                return DefaultResult.errorResult("generate array resolver error");
            } else {
                return converter.convert(object, targetType, config);
            }
        }
        List<Object> source = TypeUtils.arrayOrCollectionToList(object);
        // component type是数组类型
        if (targetType.getComponentType().getType() instanceof GenericArrayType
                || TypeUtils.isArrayClass(targetType.getComponentType().resolve())) {
            TypeWrapper componentType = TypeWrappers.getAndResolveComponentNonArrayType(targetType);
            Class<?> componentClass = TypeUtils.getArrayClass(componentType.resolveOrObject(), TypeUtils.getArrayTypeDimension(targetType.getComponentType().getType()));
            return convertArray(ArrayValueResolver.instance, targetType.getComponentType(), config, source, componentClass);
        } else { // 子类型非数组类型
            TypeWrapper componentType = TypeWrappers.getAndResolveComponentNonArrayType(targetType);
            ResolvableConverter valueConverter = ResolvableConverters.getConverter(componentType.resolve());
            if (valueConverter != null) {
                return convertArray(valueConverter, targetType.getComponentType(), config, source, componentType.resolveOrObject());
            } else {
                return DefaultResult.errorResult("can not convert source to target, converter not found");
            }
        }
    }

    private <T> Result<T> convertArray(ResolvableConverter converter, TypeWrapper componentType, ResolveConfig config, List<Object> source, Class<?> componentNonArrayClass) {
        Object targetArray = Array.newInstance(componentNonArrayClass, source.size());
        for (int i = 0; i < source.size(); i++) {
            Result<Object> value = converter.convert(source.get(i), componentType, config);
            if (!value.success()) {
                return DefaultResult.errorResult(CastUtils.castSafe(Array.newInstance(componentNonArrayClass, source.size())), value.resultMsg());
            } else {
                Array.set(targetArray, i, value.instance());
            }
        }
        return DefaultResult.successResult(CastUtils.castSafe(targetArray));
    }

}
