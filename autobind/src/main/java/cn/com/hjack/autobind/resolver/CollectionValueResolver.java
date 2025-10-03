/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.factory.TypeValueResolvers;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.utils.TypeUtils;
import cn.com.hjack.autobind.validation.DefaultResult;

import java.util.Collection;
import java.util.List;


/**
 * @ClassName: CollectionValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月16日
 */
public class CollectionValueResolver extends AbstractTypeValueResolver {

    public static CollectionValueResolver instance = new CollectionValueResolver();

    private CollectionValueResolver() {

    }

    @Override
    protected Result<Object> doResolveValue(Object object, TypeWrapper targetType, ResolveConfig config) throws Exception {
        if (targetType == null || !TypeUtils.isCollectionClass(targetType.resolve())) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "object can not be null");
        }
        if (ConvertFeature.isEnabled(config.convertFeature(), ConvertFeature.LAZY_MODE)) {
            return ProxyValueResolver.instance.resolve(object, targetType, config);
        }
        List<Object> source = TypeUtils.arrayOrCollectionToList(object);
        TypeWrapper genericType = TypeWrappers.getAndResolveGenericType(targetType, 0);
        TypeValueResolver valueResolver = TypeValueResolvers.getResolver(genericType.resolve());
        if (valueResolver != null) {
            DefaultResult<Object> result = new DefaultResult<>();
            Collection<Object> resultCollection = TypeUtils.createCollection(targetType.resolve());
            source.forEach(element -> {
                if (element != null) {
                    Result<Object> childResult = null;
                    try {
                        childResult = valueResolver.resolve(element, genericType, config);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                    if (childResult == null || !childResult.success() || childResult.instance() == null) {
                        result.setSuccess(false);
                        if (childResult == null) {
                            result.setResultCode(Constants.FAIL_CODE);
                            result.setResultMsg(Constants.FAIL_MESSAGE);
                        } else {
                            result.setResultCode(Constants.FAIL_CODE);
                            result.setResultMsg(childResult.resultMsg());
                            resultCollection.add(childResult.instance());
                        }
                    } else {
                        resultCollection.add(childResult.instance());
                    }
                }
            });
            result.setInstance(resultCollection);
            return result;
        } else {
            Class<?> genericClass = genericType.resolveOrObject();
            if (genericClass == Object.class) {
                Collection<Object> resultCollection = TypeUtils.createCollection(targetType.resolve());
                source.forEach(element -> {
                    if (element != null) {
                        resultCollection.add(element);
                    }
                });
                return DefaultResult.defaultSuccessResult(resultCollection);
            } else {
                return DefaultResult.errorResult(null, Constants.FAIL_CODE, "can not convert source to target, can not find converter");
            }
        }
    }
}
