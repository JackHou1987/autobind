package cn.com.hjack.autobind.binder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.converter.ResolvableConverters;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.ClassWrapper;
import cn.com.hjack.autobind.utils.FieldWrapper;
import cn.com.hjack.autobind.utils.TypeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.base.Strings;


/**
 * @Description: TODO
 * @author houqq
 * @date: 2025年11月5日
 *
 */
public abstract class AbstractBeanMapper<T> implements Mapper<T> {

    private List<Validator> internalValidators = new ArrayList<>();

    protected Map<String, CustomFieldEditor> fieldEditors = new HashMap<>();

    protected ClassWrapper classWrapper;

    protected T target;

    protected Map<String, TypeWrapper> variableContext;

    protected ResolveConfig config;

    public AbstractBeanMapper(T target, Map<String, TypeWrapper> variableContext, ResolveConfig config) {
        if (target == null) {
            throw new IllegalStateException("instance can not be null");
        }
        if (!TypeUtils.isJavaBeanClass(target.getClass()) && !TypeUtils.isMapClass(target.getClass())) {
            throw new IllegalStateException("instance must be java bean or map");
        }
        this.target = target;
        this.variableContext = Optional.ofNullable(variableContext).orElse(new HashMap<>());
        this.config = config;
        if (TypeUtils.isJavaBeanClass(target.getClass())) {
            this.classWrapper = ClassWrapper.forClass(this.target.getClass());
        }
    }

    @Override
    public Result<T> beanToBean(Object source, Validator validator) {
        throw new UnsupportedOperationException("unsupport exception");
    }

    @Override
    public Map<String, Object> beanToMap(T source) {
        throw new UnsupportedOperationException("unsupport exception");
    }

    @Override
    public Result<T> mapToBean(Map<String, Object> map, Validator validator) {
        throw new UnsupportedOperationException("unsupport exception");
    }

    protected void toBean4Map(Map<String, Object> map, Validator validator) {
        if (target == null || !TypeUtils.isJavaBeanClass(target.getClass())) {
            return;
        }
        Map<String, FieldWrapper> fieldNameMap = classWrapper.getFieldNameMap();
        if (MapUtils.isEmpty(fieldNameMap)) {
            return;
        }
        EvaluationContext context = new StandardEvaluationContext(target);
        fieldNameMap.forEach((fieldName, beanField) -> {
            Field field = beanField.getField();
            AutoBindField autoBind = beanField.getAutoBind();
            // 新增condition校验器
            addELConditionValidator(context, autoBind, field);
            FieldTypeWrapper typeWrapper = TypeWrappers.getFieldType(field, target.getClass(), this.variableContext);
            this.fieldEditors.put(field.getName(), new CustomFieldEditor(getValue(map, autoBind, field), this, field, autoBind, typeWrapper, ResolvableConverters.getConverter(typeWrapper)));
        });
        // 新增全局校验器
        addGlobalValidator(validator);
        bind();
    }

    /**
     * @Description: 添加全局校验器(用户针对整个java bean做的校验逻辑)
     */
    protected void addGlobalValidator(Validator validator) {
        if (validator != null) {
            addValidator(validator);
        }
    }

    /**
     * @Title: addConditionValidator
     * @Description: 添加基于字段的EL表达式校验器
     * @param: EvaluationContext
     * @param: ExpressionParser
     * @param: AutoBindField
     * @param: Field
     */
    private void addELConditionValidator(EvaluationContext context, AutoBindField autoBind, Field field) {
        // 如果autbind注解为空，或者EL表达式校验没开启，则跳过
        if (!ConvertFeature.isEnabled(config.convertFeature(), ConvertFeature.EL_VALIDATE_ENABLE)
                && (autoBind == null || !ConvertFeature.isEnabled(autoBind.features(), ConvertFeature.EL_VALIDATE_ENABLE))) {
            return;
        }
        if (autoBind == null || Strings.isNullOrEmpty(autoBind.condition())) {
            return;
        }
        addValidator(new Validator() {
            ExpressionParser parser = new SpelExpressionParser();
            @Override
            public void validate(Object t, ValidationErrors errors) {
                Expression exps = parser.parseExpression(autoBind.condition());
                if (!exps.getValue(context, Boolean.class)) {
                    if (!Strings.isNullOrEmpty(autoBind.errorMsg())) {
                        errors.collectError(field.getName(), field.getName() + "." + autoBind.errorMsg());
                    } else {
                        errors.collectError(field.getName(), field.getName() + ".bind error");
                    }
                }
            }
        });
    }


    /**
     * @Title: validate
     * @Description: 执行实际的字段校验，该逻辑会将每个字段的condition校验器和global校验器一一做校验处理
     */
    protected Result<T> validate() {
        List<String> errorMsgs = doValidate();
        T instance = getTarget();
        if (CollectionUtils.isEmpty(errorMsgs)) {
            return DefaultResult.successResult(instance);
        } else {
            String errorMsg = errorMsgs.get(0);
            if (Strings.isNullOrEmpty(errorMsg)) {
                return DefaultResult.successResult(instance);
            } else {
                return DefaultResult.errorResult(instance, Constants.FAIL_CODE, errorMsg);
            }
        }
    }

    private List<String> doValidate() {
        List<String> errorMsg = new ArrayList<>();
        if (hasValidator()) {
            final ValidationErrors validationErrors = new ValidationErrors() {
                @Override
                public void collectError(String fieldName, String msg) {
                    errorMsg.add(msg);
                }
            };
            List<Validator> validators = getValidators();
            if (!CollectionUtils.isEmpty(validators)) {
                validators.stream().filter(Objects::nonNull).forEach(validator -> {
                    validator.validate(getTarget(), validationErrors);
                });
            }
        }
        return errorMsg;
    }

    protected void bind() {
        fieldEditors.forEach((fieldName, fieldEditor) -> {
            Object value = fieldEditor.edit();
            if (value != null) {
                Field field = fieldEditor.getField();
                if (field == null) {
                    return;
                }
                Mapper<?> beanMapper = fieldEditor.getMapper();
                if (beanMapper == null || beanMapper.getTarget() == null) {
                    return;
                }
                try {
                    field.setAccessible(true);
                    field.set(beanMapper.getTarget(), value);
                } catch (Exception e) {
                    return;
                }
            }
        });
    }

    /**
     *   从map中获得field名称所对应的value,如果value为空，且autoBind.defaultValue()不为空，则返回默认值
     * @param: source map
     * @param: autoBind
     * @param: field
     * @return: value
     */
    protected Object getValue(Map<String, Object> source, AutoBindField autoBind, Field field) {
        String fieldName = CastUtils.getRecvFieldName(autoBind, field);
        if (Strings.isNullOrEmpty(fieldName)) {
            return null;
        } else {
            boolean deepSeek = false;
            if (autoBind != null) {
                deepSeek = autoBind.deepSeek();
            }
            return getValueOrDefault(source, fieldName, CastUtils.getDefaultValue(autoBind), deepSeek);
        }
    }

    /**
     * 依据receiveFieldName找到map中key对应的value
     * @param 待查找的map
     * @param 待查找的key名称
     * @param 字段class
     * @param 默认值
     * @param 是否嵌套搜索
     * @return keyName对应的value
     */
    public static Object getValueOrDefault(Map<String, Object> map, String keyName, String defaultValue, boolean deepSeek) {
        if (MapUtils.isEmpty(map)) {
            return defaultValue;
        }
        Object fieldValue = doGetValue(map, keyName, deepSeek);
        if (fieldValue != null) {
            return fieldValue;
        } else {
            return defaultValue;
        }
    }

    /**
     * 从source map中找到keyName对应的value
     * @param: source map
     * @param: key name
     * @return: key value
     */
    private static Object doGetValue(Map<String, Object> source, String keyName, boolean deepSeek) {
        if (source.get(keyName) != null) {
            return source.get(keyName);
        }
        if (!deepSeek) {
            return null;
        } else {
            Set<Map.Entry<String, Object>> entries = source.entrySet();
            // map中找不到字段名则递归查找嵌套map中字段名称
            for (Map.Entry<String, Object> entry : entries) {
                Object value = entry.getValue();
                if (value != null && value instanceof Map) {
                    Object fieldValue = doGetValue(CastUtils.castSafe(value), keyName, deepSeek);
                    if (fieldValue != null) {
                        return fieldValue;
                    }
                }
            }
            return null;
        }
    }

    /**
     * @Title: addInternalValidator
     * @Description: 新增内部校验器
     * @param: validator
     */
    protected void addValidator(Validator validator) {
        if (validator != null) {
            internalValidators.add(validator);
        }
    }

    /**
     * @Title: reportFieldConvertError
     * @Description: 记录字段转换错误
     * @param: 错误信息
     * @param: 对应字段
     */
    public void reportFieldConvertError(String resultMsg, Field field) {
        if (field == null) {
            return;
        }
        addValidator(new Validator() {
            @Override
            public void validate(Object t, ValidationErrors errors) {
                if (Strings.isNullOrEmpty(resultMsg)) {
                    errors.collectError(field.getName(), field.getName() + ".bind error");
                } else {
                    errors.collectError(field.getName(), field.getName() + "." + resultMsg);
                }
            }
        });
    }

    private boolean hasValidator() {
        return !CollectionUtils.isEmpty(internalValidators);
    }

    private List<Validator> getValidators() {
        return internalValidators;
    }

    @Override
    public ResolveConfig getConfig() {
        return config;
    }

    @Override
    public T getTarget() {
        return target;
    }

}
