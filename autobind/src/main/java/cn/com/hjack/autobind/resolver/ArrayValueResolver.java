/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.factory.TypeValueResolvers;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.generator.ObjectGenerator;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.utils.TypeUtils;
import cn.com.hjack.autobind.validation.DefaultResult;

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
public class ArrayValueResolver extends AbstractTypeValueResolver {

    public static ArrayValueResolver instance = new ArrayValueResolver();

    private ArrayValueResolver() {

    }

    @Override
    protected Result<Object> doResolveValue(Object object, TypeWrapper targetType, ResolveConfig config) throws Exception {
        if (targetType == null
                || targetType.getComponentType() == null
                || !TypeUtils.isArrayClass(targetType.resolve())) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "object can not be null");
        }
        if (config.fastMode()) {
            TypeValueResolver resolver = ObjectGenerator.instance.generateResolver(object.getClass(), targetType);
            if (resolver == null) {
                return DefaultResult.errorResult(null, Constants.FAIL_CODE, "can not create proxy class");
            } else {
                return resolver.resolve(object, targetType, config);
            }
        }
        List<Object> source = TypeUtils.arrayOrCollectionToList(object);
        // component type是数组类型
        if (targetType.getComponentType().getType() instanceof GenericArrayType
                || TypeUtils.isArrayClass(targetType.getComponentType().resolve())) {
            TypeWrapper componentType = TypeWrappers.getAndResolveComponentNonArrayType(targetType);
            Class<?> componentClass = TypeUtils.getArrayClass(componentType.resolveOrObject(), TypeUtils.getArrayTypeDimension(targetType.getComponentType().getType()));
            TypeValueResolver valueResolver = new ArrayValueResolver();
            Object resultArray = Array.newInstance(componentClass, source.size());
            return convertArray(valueResolver, targetType.getComponentType(), config, source, resultArray);
        } else { // 子类型非数组类型
            TypeWrapper componentType = targetType.resolveComponentType(targetType.getComponentType());
            Class<?> componentCls = componentType.resolveOrObject();
            TypeValueResolver valueResolver = TypeValueResolvers.getResolver(componentType.resolve());
            if (valueResolver != null) {
                Object resultArray = Array.newInstance(componentCls, source.size());
                return convertArray(valueResolver, targetType.getComponentType(), config, source, resultArray);
            } else {
                return DefaultResult.errorResult(null, Constants.FAIL_CODE, "can not convert source to target, can not find converter");
            }
        }
    }

    private Result<Object> convertArray(TypeValueResolver resolver, TypeWrapper componentType, ResolveConfig config, List<Object> source, Object targetArray) throws Exception {
        DefaultResult<Object> result = new DefaultResult<>();
        for (int i = 0; i < source.size(); i++) {
            Object obj = source.get(i);
            if (obj != null) {
                Result<Object> value = resolver.resolve(obj, componentType, config);
                if (value == null || !value.success() || value.instance() == null) {
                    result.setSuccess(false);
                    if (value == null) {
                        result.setResultCode(Constants.FAIL_CODE);
                        result.setResultMsg(Constants.FAIL_MESSAGE);
                    } else {
                        result.setResultCode(Constants.FAIL_CODE);
                        result.setResultMsg(value.resultMsg());
                        Array.set(targetArray, i, value.instance());
                    }
                } else {
                    Array.set(targetArray, i, value.instance());
                }
            }
        }
        result.setInstance(targetArray);
        return result;
    }

}
