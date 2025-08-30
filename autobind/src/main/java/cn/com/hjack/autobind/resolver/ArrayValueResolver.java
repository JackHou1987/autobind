package cn.com.hjack.autobind.resolver;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.com.hjack.autobind.factory.TypeValueResolvers;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.factory.ConversionServiceProvider;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.validation.DefaultResult;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.generator.ObjectGenerator;
import cn.com.hjack.autobind.utils.TypeUtils;
import org.springframework.core.convert.ConversionService;


/**
 * @ClassName: ArrayValueResolver
 * @Description: TODO
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
        if (object == null || targetType == null
                || targetType.getComponentType() == null
                || !TypeUtils.isArrayClass(targetType.resolve())) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "object can not be null");
        }
        if (!(object instanceof Collection) && !TypeUtils.isArrayClass(object.getClass())) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "object can not be null");
        }
        if (config != null && config.fastMode()) {
            TypeValueResolver resolver = ObjectGenerator.instance.generateResolver(object.getClass(), targetType);
            if (resolver == null) {
                return DefaultResult.errorResult(null, Constants.FAIL_CODE, "can not create proxy class");
            } else {
                return resolver.resolve(object, targetType, config);
            }
        }
        int size;
        Object source;
        if (TypeUtils.isCollectionClass(object.getClass())) {
            source = new ArrayList<>((Collection<?>) object);
            size = ((List<?>) source).size();
        } else {
            source = object;
            size = Array.getLength(object);
        }
        DefaultResult<Object> result = new DefaultResult<>();
        // component type是数组类型
        if (targetType.getComponentType().getType() instanceof GenericArrayType
                || TypeUtils.isArrayClass(targetType.getComponentType().resolve())) {
            TypeWrapper componentType = TypeWrappers.getAndResolveComponentNonArrayType(targetType);
            Class<?> componentClass = TypeUtils.getArrayClass(componentType.resolveOrObject(), TypeUtils.getArrayTypeDimension(targetType.getComponentType().getType()));
            Object resultArray = Array.newInstance(componentClass, size);
            TypeValueResolver valueResolver = new ArrayValueResolver();
            for (int i = 0; i < size; i++) {
                Object obj;
                if (TypeUtils.isCollectionClass(object.getClass())) {
                    assert source instanceof List<?>;
                    obj = ((List<?>) source).get(i);
                } else {
                    obj = Array.get(source, i);
                }
                if (obj != null) {
                    Result<Object> value = valueResolver.resolve(obj, targetType.getComponentType(), config);
                    if (value == null || !value.success() || value.instance() == null) {
                        result.setSuccess(false);
                        if (value == null) {
                            result.setResultCode(Constants.FAIL_CODE);
                            result.setResultMsg(Constants.FAIL_MESSAGE);
                        } else {
                            result.setResultCode(Constants.FAIL_CODE);
                            result.setResultMsg(value.resultMsg());
                            Array.set(resultArray, i, value.instance());
                        }
                    } else {
                        Array.set(resultArray, i, value.instance());
                    }
                }
            }
            result.setInstance(resultArray);
            return result;
        } else { // 子类型非数组类型
            TypeWrapper componentType = targetType.resolveComponentType(targetType.getComponentType());
            Class<?> componentCls = componentType.resolveOrObject();
            TypeValueResolver valueResolver = TypeValueResolvers.getResolver(componentType.resolve());
            if (valueResolver != null) {
                Object resultArray = Array.newInstance(componentCls, size);
                for (int i = 0; i < size; i++) {
                    Object obj;
                    if (TypeUtils.isCollectionClass(object.getClass())) {
                        assert source instanceof List<?>;
                        obj = ((List<?>) source).get(i);
                    } else {
                        obj = Array.get(source, i);
                    }
                    if (obj != null) {
                        Result<Object> value = valueResolver.resolve(obj, componentType, config);
                        if (value == null || !value.success() || value.instance() == null) {
                            result.setSuccess(false);
                            if (value == null) {
                                result.setResultCode(Constants.FAIL_CODE);
                                result.setResultMsg(Constants.FAIL_MESSAGE);
                            } else {
                                result.setResultCode(Constants.FAIL_CODE);
                                result.setResultMsg(value.resultMsg());
                                Array.set(resultArray, i, value.instance());
                            }
                        } else {
                            Array.set(resultArray, i, value.instance());
                        }
                    }
                }
                result.setInstance(resultArray);
                return result;
            } else {
                ConversionService conversionService = ConversionServiceProvider.getConversionService(config);
                Object resultArray = Array.newInstance(componentCls, size);
                for (int i = 0; i < size; i++) {
                    Object obj;
                    if (TypeUtils.isCollectionClass(object.getClass())) {
                        assert source instanceof List<?>;
                        obj = ((List<?>) source).get(i);
                    } else {
                        obj = Array.get(source, i);
                    }
                    if (obj != null) {
                        Object value = convert(obj, componentCls, conversionService);
                        if (value != null) {
                            Array.set(resultArray, i, value);
                        }
                    }
                }
                result.setInstance(resultArray);
                return result;
            }
        }
    }

}
