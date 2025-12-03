/**
 *
 */
package cn.com.hjack.autobind.converter;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;

import java.util.Collection;
import java.util.List;


/**
 * @ClassName: CollectionValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月16日
 */
public class CollectionValueResolver extends AbstractResolvableConverter {

    public static CollectionValueResolver instance = new CollectionValueResolver();

    private CollectionValueResolver() {

    }

    @Override
    protected <T> Result<T> doConvert(Object object, TypeWrapper targetType, ResolveConfig config) {
        if (!TypeUtils.isCollectionClass(targetType.resolve())) {
            return DefaultResult.errorResult("object can not be null");
        }
        if (ConvertFeature.isEnabled(config.convertFeature(), ConvertFeature.LAZY_MODE)) {
            return ResolvableConverters.getLazyLoadValueResolver().convert(object, targetType, config);
        }
        List<Object> source = TypeUtils.arrayOrCollectionToList(object);
        TypeWrapper genericType = TypeWrappers.getAndResolveGenericType(targetType, 0);
        ResolvableConverter valueConverter = ResolvableConverters.getConverter(genericType.resolve());
        if (valueConverter != null) {
            Collection<Object> resultCollection = TypeUtils.createCollection(targetType.resolve());
            for (Object element : source) {
                if (element == null) {
                    continue;
                }
                Result<Object> childResult = valueConverter.convert(element, genericType, config);
                if (!childResult.success()) {
                    return DefaultResult.errorResult(CastUtils.castSafe(TypeUtils.createCollection(targetType.resolve())), childResult.resultMsg());
                } else {
                    resultCollection.add(childResult.instance());
                }
            }
            return DefaultResult.successResult(CastUtils.castSafe(resultCollection));
        } else {
            Collection<Object> resultCollection = TypeUtils.createCollection(targetType.resolve());
            Class<?> genericClass = genericType.resolveOrObject();
            if (genericClass == Object.class) {
                source.forEach(element -> {
                    if (element != null) {
                        resultCollection.add(element);
                    }
                });
                return DefaultResult.successResult(CastUtils.castSafe(resultCollection));
            } else {
                return DefaultResult.errorResult(CastUtils.castSafe(resultCollection), "can not convert source to target, can not find converter");
            }
        }
    }
}
