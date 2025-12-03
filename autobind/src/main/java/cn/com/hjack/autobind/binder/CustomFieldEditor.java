/**
 *
 */
package cn.com.hjack.autobind.binder;

import cn.com.hjack.autobind.*;

import java.lang.reflect.Field;


/**
 * @ClassName: CustomFieldEditor
 * @Description: TODO
 * @author houqq
 * @date: 2025年11月10日
 */
public class CustomFieldEditor {

    private Object source;

    private Field field;

    private AbstractBeanMapper<?> mapper;

    private ResolvableConverter converter;

    private TypeWrapper targetType;

    private AutoBindField autoBind;

    public Object edit() {
        try {
            if (converter == null) {
                throw new IllegalStateException("can not convert source to target, converter not found");
            }
            Result<Object> result = converter.convert(source, targetType, ResolveConfig.merge(mapper.getConfig(), autoBind));
            if (!result.success()) {
                mapper.reportFieldConvertError(result.resultMsg(), field);
                return result.instance();
            } else {
                return result.instance();
            }
        } catch (Exception e) {
            mapper.reportFieldConvertError(e.getMessage(), field);
            return null;
        }
    }

    public CustomFieldEditor(Object source, AbstractBeanMapper<?> mapper, Field field, AutoBindField autoBind, TypeWrapper targetType, ResolvableConverter converter) {
        if (mapper == null || field == null || targetType == null) {
            throw new IllegalStateException("param can not be null");
        }
        this.converter = converter;
        this.mapper = mapper;
        this.field = field;
        this.targetType = targetType;
        this.autoBind = autoBind;
        this.source = source;
    }

    public Mapper<?> getMapper() {
        return mapper;
    }

    public Field getField() {
        return field;
    }
}
