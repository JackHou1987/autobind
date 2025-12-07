/**
 *
 */
package cn.com.hjack.autobind.binder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.ClassWrapper;
import cn.com.hjack.autobind.utils.FieldWrapper;
import cn.com.hjack.autobind.utils.TypeUtils;
import org.apache.commons.collections4.MapUtils;

import com.google.common.base.Strings;


/**
 * 缺省Mapper接口实现，默认通过反射实现类型转换
 * @author houqq
 * @date: 2025年11月10日
 */
public class DefaultBeanMapper<T> extends AbstractBeanMapper<T> {

    public DefaultBeanMapper(T target, Map<String, TypeWrapper> variableContext, ResolveConfig config) {
        super(target, variableContext, config);
    }

    @Override
    public Result<T> beanToBean(Object source, Validator validator) {
        Class<?> sourceClass = null;
        if (source != null) {
            sourceClass = source.getClass();
        }
        toBean4Map(BeanMappers.getMapper(sourceClass, TypeWrappers.getType(Map.class), getConfig()).beanToMap(source), validator);
        return validate();
    }

    @Override
    public Result<T> mapToBean(Map<String, Object> map, Validator validator) {
        toBean4Map(map, validator);
        return validate();
    }

    @Override
    public Map<String, Object> beanToMap(T source) {
        if (this.getTarget() == null || !TypeUtils.isMapClass(getTarget().getClass())) {
            throw new IllegalStateException("target type must be map");
        }
        if (source == null) {
            return CastUtils.castSafe(getTarget());
        }
        Map<String, Object> result = CastUtils.castSafe(getTarget());
        Map<String, Object> target = beanToMap(source, new HashMap<>());
        if (!MapUtils.isEmpty(target)) {
            result.putAll(target);
        }
        return result;
    }

    /**
     * 递归将java bean及其中字段值包含java bean的转换为map。
     * @param target bean instance
     * @param javaBeanFieldObject 存储javabean字段和定义对其声明的javaBean实体对象，判断循环引用
     * @return: 转换后的map对象
     */
    private Map<String, Object> beanToMap(Object target, Map<Field, Object> javaBeanFieldObject) {
        if (target == null || !TypeUtils.isJavaBeanClass(target.getClass())) {
            return new HashMap<>();
        }
        Map<String, Object> result = new HashMap<>();
        Map<String, FieldWrapper> targetFields = ClassWrapper.forClass(target.getClass()).getFieldNameMap();
        if (MapUtils.isEmpty(targetFields)) {
            return result;
        }
        targetFields.forEach((name, fieldWrapper) -> {
            Field field = fieldWrapper.getField();
            AutoBindField autoBind = fieldWrapper.getAutoBind();
            Object value = CastUtils.getFieldValue(field, target);
            Class<?> valueClass = TypeUtils.getFieldActualClass(field, value);
            // 值类型是集合、map和数组
            if (TypeUtils.isCollectionClass(valueClass)
                    || TypeUtils.isMapClass(valueClass)
                    || TypeUtils.isArrayClass(valueClass)) {
                result.putAll(collectSendFieldNameObjects(autoBind, field, value));
            } else if (TypeUtils.isJavaBeanClass(valueClass)) {
                // 如果已有字段、字段所属object映射关系,则跳过,防止循环引用无限递归
                if (javaBeanFieldObject.get(field) != null) {
                    return;
                } else {
                    javaBeanFieldObject.put(field, target);
                }
                try {
                    Map<String, Object> beanMap;
                    if (value == null) {
                        Object instance = valueClass.newInstance();
                        beanMap = beanToMap(instance, javaBeanFieldObject);
                    } else {
                        beanMap = beanToMap(value, javaBeanFieldObject);
                    }
                    result.putAll(collectSendFieldNameObjects(autoBind, field, beanMap));
                } catch (Exception e) {
                    result.putAll(new HashMap<>());
                }
            } else {
                if (value == null && autoBind != null) {
                    String defaultValue = autoBind.defaultValue();
                    if (!Strings.isNullOrEmpty(defaultValue)) {
                        assert field != null;
                        value = CastUtils.setNumberScale(CastUtils.convert(defaultValue, field.getType()), autoBind);
                        value = CastUtils.formatDate(value, autoBind);
                    }
                } else {
                    // 转换数字和日期格式
                    value = CastUtils.setNumberScale(value, autoBind);
                    value = CastUtils.formatDate(value, autoBind);
                }
                result.putAll(collectSendFieldNameObjects(autoBind, field, value));
            }
        });
        return result;
    }

    private Map<String, Object> collectSendFieldNameObjects(AutoBindField autoBind, Field field, Object value) {
        Map<String, Object> result = new HashMap<>();
        String[] fieldNames = CastUtils.getSendFieldNames(autoBind, field);
        if (fieldNames != null) {
            for (String fieldName : fieldNames) {
                result.put(fieldName, value);
            }
        }
        return result;
    }


}
