/**
 *
 */
package cn.com.hjack.autobind.converter;

import java.util.Map;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.binder.BeanMappers;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;

/**
 * @ClassName: JavaBeanConverter
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月16日
 *
 */
public class JavaBeanConverter extends AbstractResolvableConverter {

    public static JavaBeanConverter instance = new JavaBeanConverter();

    private JavaBeanConverter() {

    }

    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        if (!TypeUtils.isJavaBeanClass(targetType.resolve())) {
            return DefaultResult.errorResult("object can not be null");
        }
        if (source == null) {
            try {
                return DefaultResult.successResult(CastUtils.castSafe(targetType.resolve().newInstance()));
            } catch (Exception e) {
                return DefaultResult.errorResult("create intance error");
            }
        }
        if (!TypeUtils.isMapClass(source.getClass()) && !TypeUtils.isJavaBeanClass(source.getClass())) {
            return DefaultResult.errorResult("object can not be null");
        }
        if (ConvertFeature.isEnabled(config.convertFeature(), ConvertFeature.LAZY_MODE)) {
            return ResolvableConverters.getLazyLoadValueResolver().convert(source, targetType, config);
        }
        BeanMapper<T> mapper = BeanMappers.getBeanMapper(source.getClass(), targetType, config);
        Result<T> result;
        if (TypeUtils.isMapClass(source.getClass())) {
            result = mapper.mapToBean(CastUtils.toMap((Map<?, ?>) source), config.validator());
        } else {
            result = mapper.beanToBean(source, config.validator());
        }
        if (!result.success()) {
            return DefaultResult.errorResult(result.instance(), result.resultMsg());
        } else {
            return DefaultResult.successResult(result.instance());
        }
    }

}
