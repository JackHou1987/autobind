/**
 *
 */
package cn.com.hjack.autobind.mapper;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.factory.ConversionServiceProvider;
import cn.com.hjack.autobind.factory.TypeValueResolvers;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.utils.*;
import cn.com.hjack.autobind.validation.DefaultResult;
import com.google.common.base.Strings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.google.common.base.CaseFormat;


/**
 * @ClassName: BeanMapper
 * @Description: 主要执行将javabean转换为map或者将map转换为javabean或者对map做键值的转化和一些处理操作，主要实现基于spring的databinder
 * @author houqq
 * @date: 2025年6月13日
 *
 */
public class BeanMapper<T> {

    private DataBinder dataBinder;

    private List<Validator> internalValidators = new ArrayList<>();

    private T target;

    private Map<String, TypeWrapper> variableContext;

    private ClassWrapper classDesc;

    private ConversionService conversionService;

    private ResolveConfig config;

    public BeanMapper(T target) {
        this(target, new HashMap<>(), ResolveConfig.defaultConfig);
    }

    public BeanMapper(T target, Map<String, TypeWrapper> variableContext) {
        this(target, variableContext, ResolveConfig.defaultConfig);
    }

    @SuppressWarnings("unchecked")
    public BeanMapper(T target, Map<String, TypeWrapper> variableContext, ResolveConfig config) {
        if (target == null) {
            throw new IllegalStateException("instance can not be null");
        }
        this.target = target;
        if (TypeUtils.isJavaBeanClass(target.getClass())) {
            classDesc = ClassWrapper.forClass(this.target.getClass());
            this.variableContext = Optional.ofNullable(variableContext).orElse(new HashMap<>());
            this.conversionService = this.initConversionService(config);
            this.dataBinder = new DataBinder(target);
            dataBinder.setConversionService(conversionService);
        } else {
            if (TypeUtils.isMapClass(this.target.getClass())) {
                this.variableContext = Optional.ofNullable(variableContext).orElse(new HashMap<>());
                this.conversionService = this.initConversionService(config);
                Map<String, Object> targetMap = (Map<String, Object>) target;
                this.dataBinder = new DataBinder(target) {
                    @Override
                    protected void doBind(MutablePropertyValues mpvs) {
                        List<PropertyValue> propertyValues = mpvs.getPropertyValueList();
                        if (CollectionUtils.isEmpty(propertyValues)) {
                            return;
                        } else {
                            MapConvertFeature[] featureArray = Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig).mapConvertFeature();
                            int features = MapConvertFeature.of(featureArray);
                            propertyValues.forEach(pv -> {
                                targetMap.put(convertMapKeyStyle(pv.getName(), features), convertMapValueType(pv.getValue(), new HashMap<>(), featureArray));
                            });
                        }
                    }
                };
            } else {
                throw new IllegalStateException("target instance must be java bean or map");
            }
        }
    }

    public BeanMapper(TypeReference<T> typeReference, ResolveConfig registry) {
        if (typeReference == null || typeReference.getInstance() == null) {
            throw new IllegalStateException("instance can not be null");
        }
        this.target = typeReference.getInstance();
        if (TypeUtils.isJavaBeanClass(target.getClass())) {
            classDesc = ClassWrapper.forClass(this.target.getClass());
            this.dataBinder = new DataBinder(target);
            this.conversionService = this.initConversionService(registry);
            dataBinder.setConversionService(conversionService);
            this.variableContext = this.initVariableContext(typeReference);
        } else {
            throw new IllegalStateException("instance must be java bean");
        }
    }

    public BeanMapper(TypeReference<T> typeReference) {
        this(typeReference, null);
    }

    public T getTarget() {
        return target;
    }

    public ResolveConfig getConfig() {
        return config;
    }

    /**
     * @Title: bindMapToBean
     * @Description: 将map对象转换为java bean对象，map中key字段为java bean field 字段
     * @param: map对象
     * @param: 校验器
     * @return: Result<T>
     */
    public Result<T> bindMapToBean(Map<String, Object> map, Validator validator) {
        if (MapUtils.isEmpty(map)) {
            return DefaultResult.defaultSuccessResult(target);
        }
        doBindBean(map, validator);
        return validate();
    }

    /**
     * @Title: bindBeanToBean
     * @Description: 将源java bean绑定到目标java bean,先将源java bean转为map后再转为目标java bean
     * @param: 源javabean
     * @param: 校验器
     * @param: MapConvertFeature
     * @return: Result<T>
     */
    public Result<T> bindBeanToBean(Object source, Validator validator, MapConvertFeature... features) {
        this.doBindBean(BeanMapper.beanToMap(source.getClass(), source, features), validator);
        return validate();
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

    /**
     * @Title: initVariableContext
     * @Description: 初始化基于typereference的variable context
     * @param: TypeReference
     * @return: Map<String,TypeWrapper>
     */
    private Map<String, TypeWrapper> initVariableContext(TypeReference<T> typeReference) {
        TypeWrapper typeWrapper = TypeWrappers.getType(TypeReference.class, typeReference.getClass());
        TypeWrapper[] parameterizedTypes = Optional.ofNullable(typeWrapper.getGeneric(0)).orElseThrow(() -> {return new IllegalStateException("type wrapper can not be null");}).getGenerics();
        if (parameterizedTypes == null || parameterizedTypes.length == 0) {
            return new HashMap<>();
        }
        TypeWrapper[] genericTypes = TypeWrappers.getType(target.getClass()).getGenerics();
        if (genericTypes == null || genericTypes.length == 0 || genericTypes.length != parameterizedTypes.length) {
            return new HashMap<>();
        }
        Map<String, TypeWrapper> variableContext = new HashMap<>();
        for (int i = 0; i < parameterizedTypes.length; i++) {
            TypeWrapper parameterizedType = parameterizedTypes[i];
            Class<?> parameterizedCls = parameterizedType.resolve();
            if (parameterizedCls == null) {
                return new HashMap<>();
            }
            TypeWrapper genericType = genericTypes[i];
            variableContext.put(genericType.getType().getTypeName(), parameterizedType);
        }
        return variableContext;
    }

    private ConversionService initConversionService(ResolveConfig config) {
        return ConversionServiceProvider.getConversionService(config);
    }

    /**
     * @Title: doBindBean
     * @Description: 将map转换为javabean
     * @param: source map
     * @param: 校验器
     */
    private void doBindBean(Map<String, Object> map, Validator validator) {
        if (target == null || !TypeUtils.isJavaBeanClass(target.getClass())
                || MapUtils.isEmpty(map)) {
            return;
        }
        Map<String, FieldWrapper> fieldNameMap = classDesc.getFieldNameMap();
        if (MapUtils.isEmpty(fieldNameMap)) {
            return;
        }
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        EvaluationContext context = new StandardEvaluationContext(target);
        ExpressionParser parser = new SpelExpressionParser();
        fieldNameMap.forEach((fieldName, beanField) -> {
            if (Strings.isNullOrEmpty(fieldName) || beanField == null) {
                return;
            }
            Field field = beanField.getField();
            if (field == null || Modifier.isStatic(field.getModifiers())) {
                return;
            }
            AutoBindField autoBind = beanField.getAutoBind();
            if (autoBind != null && autoBind.exclude()) {
                return;
            }
            // 新增condition校验器
            addFieldELConditionValidator(context, parser, autoBind, field);
            Object paramValue = getValue(map, autoBind, field);
            if (paramValue != null) {
                FieldTypeWrapper typeWrapper = TypeWrappers.getFieldType(field, target.getClass(), this.variableContext);
                TypeValueResolver valueResolver = TypeValueResolvers.getResolver(typeWrapper);
                if (valueResolver != null) {
                    dataBinder.registerCustomEditor(typeWrapper.getFieldTypeClass(), field.getName(), new CustomFieldEditor<T>(this, field, autoBind, typeWrapper, valueResolver));
                }
                propertyValues.add(field.getName(), paramValue);
            }
        });
        // 新增全局校验器
        addGlobalValidator(validator);
        bind(propertyValues);
    }

    /**
     * @Title: getValue
     * @Description: 从map中获得field名称所对应的value,如果value为空，且autoBind.defaultValue()不为空，则返回默认值
     * @param: source map
     * @param: autoBind
     * @param: field
     * @return: value
     */
    private Object getValue(Map<String, Object> source, AutoBindField autoBind, Field field) {
        String fieldName = getFieldName(autoBind, field);
        if (Strings.isNullOrEmpty(fieldName)) {
            return null;
        }
        String defaultValue = null;
        if (autoBind != null && !Strings.isNullOrEmpty(autoBind.defaultValue())) {
            defaultValue = autoBind.defaultValue();
        }
        return getValueOrDefault(source, fieldName, field.getType(), defaultValue);
    }

    /**
     * @Title: getValue
     * @Description: 依据receiveFieldName找到map中key对应的value,如果找不到，
     * 则递归查找map value中为map的对象。返回如果当前field class为java bean，则返回当前map,否则返回default value
     * @param: 待查找的map
     * @param: 待查找的key名称
     * @param: 字段class
     * @param: 默认值
     * @return: keyName对应的value
     */
    public static Object getValueOrDefault(Map<String, Object> map, String keyName, Class<?> fieldClass, String defaultValue) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Object fieldValue = doGetValue(map, keyName);
        if (fieldValue != null) {
            return fieldValue;
        } else {
            // 如果待查找的字段类型为java bean,且map中找不到相应的key,则返回当前map
            if (TypeUtils.isJavaBeanClass(fieldClass)) {
                return map;
            } else {
                return defaultValue;
            }
        }
    }

    /**
     * @Title: doGetValue
     * @Description: 从source map中找到keyName对应的value
     * @param: source map
     * @param: key name
     * @param: field class
     * @return: key value
     */
    @SuppressWarnings("unchecked")
    private static Object doGetValue(Map<String, Object> source, String keyName) {
        if (source.get(keyName) != null) {
            return source.get(keyName);
        }

        Set<Map.Entry<String, Object>> entries = source.entrySet();
        // map中找不到字段名则递归查找嵌套map中字段名称
        for (Map.Entry<String, Object> entry : entries) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                Object fieldValue = doGetValue((Map<String, Object>) value, keyName);
                if (fieldValue != null) {
                    return fieldValue;
                }
            }
        }
        return null;
    }

    /**
     * @Title: getFieldName
     * @Description: 获得字段名称，如果AutoBindField注解不为空，则返回AutoBindField的recvFieldName值，否则返回字段名称
     * @param: autoBind
     * @param: field
     * @return: String
     */
    private static String getFieldName(AutoBindField autoBind, Field field) {
        if (autoBind == null && field == null) {
            return null;
        } else if (autoBind == null) {
            return field.getName();
        } else {
            if (!Strings.isNullOrEmpty(autoBind.recvFieldName())) {
                return autoBind.recvFieldName();
            } else {
                return field.getName();
            }
        }
    }

    /**
     * @ClassName: CustomFieldEditor
     * @Description: 基于spring的字段编辑器
     * @author houqq
     * @date: 2025年9月3日
     */
    private static class CustomFieldEditor<T> extends PropertyEditorSupport {

        private Object target;

        private Field field;

        private BeanMapper<?> beanMapper;

        private TypeValueResolver valueResolver;

        private TypeWrapper targetType;

        private AutoBindField autoBind;

        public CustomFieldEditor(BeanMapper<?> beanMapper, Field field, AutoBindField autoBind, TypeWrapper targetType, TypeValueResolver valueResolver) {
            if (beanMapper == null || field == null || targetType == null || valueResolver == null) {
                throw new IllegalStateException("param can not be null");
            }
            this.valueResolver = valueResolver;
            this.beanMapper = beanMapper;
            this.field = field;
            this.targetType = targetType;
            this.autoBind = autoBind;
        }

        @Override
        public void setAsText(String source) throws IllegalArgumentException {
            if (target != null) {
                setValue(source);
            } else {
                if (Strings.isNullOrEmpty(source)) {
                    this.setValue(null);
                } else {
                    try {
                        Result<Object> result = valueResolver.resolve(source, targetType, ResolveConfig.copy(beanMapper.getConfig(), autoBind));
                        if (result == null || !result.success()
                                || result.instance() == null) {
                            if (result != null) {
                                target = result.instance();
                                setValue(target = result.instance());
                                beanMapper.reportFieldConvertError(result.resultMsg(), field);
                            } else {
                                setValue(null);
                                beanMapper.reportFieldConvertError("error", field);
                            }
                        } else {
                            setValue(target = result.instance());
                        }
                        target = getValue();
                    } catch (Exception e) {
                        beanMapper.reportFieldConvertError(e.getMessage(), field);
                        setValue(null);
                    }
                }
            }
        }

        @Override
        public void setValue(Object value) {
            if (target != null) {
                super.setValue(value);
            } else {
                if (value == null) {
                    super.setValue(null);
                } else {
                    try {
                        Result<Object> result = valueResolver.resolve(value, targetType, ResolveConfig.copy(beanMapper.getConfig(), autoBind));
                        if (result == null || !result.success()
                                || result.instance() == null) {
                            if (result != null) {
                                super.setValue(result.instance());
                                beanMapper.reportFieldConvertError(result.resultMsg(), field);
                            } else {
                                super.setValue(null);
                                beanMapper.reportFieldConvertError("error", field);
                            }
                        } else {
                            super.setValue(result.instance());
                        }
                        target = getValue();
                    } catch (Exception e) {
                        beanMapper.reportFieldConvertError(e.getMessage(), field);
                        super.setValue(null);
                    }
                }
            }
        }
    }

    /**
     * @Title: validate
     * @Description: 执行实际的字段校验，该逻辑会将每个字段的condition校验器和global校验器一一做校验处理
     */
    private Result<T> validate() {
        List<String> errrMsgs = new ArrayList<>();
        if (hasValidator()) {
            org.springframework.validation.Validator springValidator = new org.springframework.validation.Validator() {
                @Override
                public boolean supports(Class<?> clazz) {
                    return true;
                }
                @Override
                public void validate(Object target, Errors errors) {
                    final ValidationErrors validationErrors = new ValidationErrors() {
                        @Override
                        public void collectError(String fieldName, String msg) {
                            if (TypeUtils.isMapClass(target.getClass())) {
                                errrMsgs.add(msg);
                            } else {
                                if (!Strings.isNullOrEmpty(fieldName)) {
                                    errors.rejectValue(fieldName, fieldName + "_error", msg);
                                } else {
                                    errors.reject("error", msg);
                                }
                            }
                        }
                    };
                    List<Validator> validators = getValidators();
                    if (!CollectionUtils.isEmpty(validators)) {
                        validators.stream().filter(Objects::nonNull).forEach(validator -> {
                            validator.validate(getTarget(), validationErrors);
                        });
                    }
                }
            };
            addSpringValidator(springValidator);
        }
        // addSpringValidator(new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator()));
        doValidate();
        T instance = getTarget();
        if (TypeUtils.isMapClass(instance.getClass())) {
            if (CollectionUtils.isEmpty(errrMsgs)) {
                return DefaultResult.defaultSuccessResult(instance);
            } else {
                return DefaultResult.errorResult(instance, Constants.FAIL_CODE, errrMsgs.get(0));
            }
        } else {
            List<ObjectError> errors = getAllErrors();
            if (CollectionUtils.isEmpty(errors)) {
                return DefaultResult.defaultSuccessResult(instance);
            } else {
                ObjectError error = errors.stream().filter(Objects::nonNull).collect(Collectors.toList()).get(0);
                if (error == null) {
                    return DefaultResult.defaultSuccessResult(instance);
                } else {
                    return DefaultResult.errorResult(instance, Constants.FAIL_CODE, error.getDefaultMessage());
                }
            }
        }
    }


    private List<Validator> getValidators() {
        return internalValidators;
    }

    private boolean hasValidator() {
        return !CollectionUtils.isEmpty(internalValidators);
    }

    private void addSpringValidator(org.springframework.validation.Validator validator) {
        if (validator != null) {
            dataBinder.addValidators(validator);
        }
    }

    private void doValidate() {
        dataBinder.validate();
    }

    /**
     * @Title: bind
     * @Description: 绑定属性
     * @param: MutablePropertyValues
     */
    private void bind(MutablePropertyValues propertyValues) {
        dataBinder.bind(propertyValues);
    }

    /**
     * @Title: getAllErrors
     * @Description: 获得所有校验errors
     * @return: List<ObjectError>
     */
    private List<ObjectError> getAllErrors() {
        if (dataBinder == null || dataBinder.getBindingResult() == null) {
            return Collections.emptyList();
        } else {
            return dataBinder.getBindingResult().getAllErrors();
        }
    }


    /**
     * @Description: 添加全局校验器(用户针对整个java bean做的校验逻辑)
     */
    private void addGlobalValidator(Validator validator) {
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
    private void addFieldELConditionValidator(EvaluationContext context, ExpressionParser parser, AutoBindField autoBind, Field field) {
        // 如果autbind注解为空，或者EL表达式校验没开启，则跳过
        if (autoBind == null || !ConvertFeature.isEnabled(autoBind.features(), ConvertFeature.EL_VALIDATE_ENABLE)) {
            return;
        }
        String condition = autoBind.condition();
        if (!Strings.isNullOrEmpty(condition)) {
            addValidator(new Validator() {
                @Override
                public void validate(Object t, ValidationErrors errors) {
                    Expression exps = parser.parseExpression(condition);
                    if (Boolean.FALSE.equals(exps.getValue(context, Boolean.class))) {
                        if (!Strings.isNullOrEmpty(autoBind.errorMsg())) {
                            errors.collectError(field.getName(), field.getName() + "." + autoBind.errorMsg());
                        } else {
                            errors.collectError(field.getName(), field.getName() + ".bind error");
                        }
                    }
                }
            });
        }
    }

    /**
     * @Title: addInternalValidator
     * @Description: 新增内部校验器
     * @param: validator
     */
    private void addValidator(Validator validator) {
        if (validator != null) {
            internalValidators.add(validator);
        }
    }

    public static Map<String, Object> beanToMap4Collection(Class<?> collectionType, Collection<Object> source, MapConvertFeature... features) {
        return beanToMap4Collection(null, collectionType, source, features);
    }

    public static Map<String, Object> beanToMap4Collection(String keyName, Class<?> collectionType, Collection<Object> source, MapConvertFeature... features) {
        if (collectionType == null) {
            throw new IllegalStateException("instance class can not be null");
        }
        Map<String, Object> result = new HashMap<>();
        if (source == null) {
            if (!Strings.isNullOrEmpty(keyName)) {
                result.put(keyName, TypeUtils.createCollection(collectionType));
            } else {
                result.put("LIST", TypeUtils.createCollection(collectionType));
            }
            return result;
        } else {
            Collection<Object> target = convertBeanToMap4Collection((Collection<Object>) source, new HashMap<>(), features);
            if (!Strings.isNullOrEmpty(keyName)) {
                result.put(keyName, target);
            } else {
                result.put("LIST", target);
            }
            return result;
        }
    }

    /**
     * @Title: toCollection
     * @Description: 转换集合中的元素: 递归转换collection中map、java bean、array、collection元素。
     * 其中map中value如果为java bean则转换为map。java bean转换为map。array中元素如果为java bean则转换为map。collection中元素如果为javabean则转换为java bean
     * @param: 源collection对象
     * @param: 转换map时，键值转换逻辑
     * @return: 转换后的collection对象
     */
    private static Collection<Object> convertBeanToMap4Collection(Collection<Object> collections, Map<Field, Object> javaBeanFieldObject, MapConvertFeature... features) {
        if (!CollectionUtils.isEmpty(collections)) {
            CollectionUtils.transform(collections, new Transformer<Object, Object>() {
                @Override
                public Object transform(Object input) {
                    if (input == null) {
                        return null;
                    } else if (TypeUtils.isJavaBeanClass(input.getClass())) {
                        return convertBeanToMap4JavaBean(input, javaBeanFieldObject);
                    } else {
                        return input;
                    }
                }
            });
            return collections;
        } else {
            return collections;
        }
    }

    public static Map<String, Object> beanToMap4Map(Map<String, Object> source, MapConvertFeature... features) {
        return beanToMap4Map(null, source, features);
    }

    public static Map<String, Object> beanToMap4Map(String keyName, Map<String, Object> source, MapConvertFeature... features) {
        Map<String, Object> result = new HashMap<>();
        if (source == null) {
            if (!Strings.isNullOrEmpty(keyName)) {
                result.put(keyName, new HashMap<>());
            } else {
                result.put("MAP", new HashMap<>());
            }
            return result;
        } else {
            Map<String, Object> target = convertBeanToMap4Map(source, new HashMap<>(), features);
            MapConvertFeature[] featureArray = features;
            if (MapConvertFeature.isEnabled(MapConvertFeature.of(featureArray), MapConvertFeature.KEY_EXPAND)) {
                if (!MapUtils.isEmpty(target)) {
                    target.forEach(result::put);
                }
            } else {
                if (!Strings.isNullOrEmpty(keyName)) {
                    result.put(keyName, target);
                } else {
                    result.put("MAP", target);
                }
            }
            return result;
        }
    }

    /**
     * @Title: convertMap
     * @Description: 转换map,主要转换其中key value风格
     * @param: 源map
     * @param: map key -> java bean field value -> java bean object(用于防止java bean循环引用导致无限递归)
     * @return: 目的map
     * @throws
     */
    private static Map<String, Object> convertBeanToMap4Map(Map<String, Object> source, Map<Field, Object> javaBeanFieldObject, MapConvertFeature... feature) {
        if (source == null) {
            return new HashMap<>();
        } else {
            Map<String, Object> map = new HashMap<>();
            MapConvertFeature[] featureArray = feature;
            int features = MapConvertFeature.of(featureArray);
            source.forEach((key, value) -> {
                map.put(convertMapKeyStyle(key, features), convertMapValueType(value, javaBeanFieldObject, featureArray));
            });
            return map;
        }
    }

    public static Map<String, Object> beanToMap4Array(Object source, MapConvertFeature... features) {
        return beanToMap4Array(null, source, features);
    }

    public static Map<String, Object> beanToMap4Array(String keyName, Object source, MapConvertFeature... features) {
        Map<String, Object> result = new HashMap<>();
        if (source == null) {
            return result;
        } else {
            Object target = convertBeanToMap4Array(source, new HashMap<>(), features);
            if (!Strings.isNullOrEmpty(keyName)) {
                result.put(keyName, target);
            } else {
                result.put("ARRAY", target);
            }
            return result;
        }
    }

    /**
     * @Title: convertArray
     * @Description: 递归将array中的元素如果为java bean的转换为map
     * @param: 源数组对象
     * @param: 转换map时，键值转换逻辑
     * @return: 转换后的数组
     * @throws
     */
    private static Object convertBeanToMap4Array(Object source, Map<Field, Object> javaBeanFieldObject, MapConvertFeature... features) {
        if (source != null && Array.getLength(source) != 0) {
            int length = Array.getLength(source);
            Object target = TypeUtils.createArrayBySource(source, length);
            for (int i = 0; i < length; i++) {
                Object obj = Array.get(source, i);
                if (TypeUtils.isJavaBeanClass(obj.getClass())) {
                    Array.set(target, i, convertBeanToMap4JavaBean(obj, javaBeanFieldObject));
                } else {
                    Array.set(target, i, obj);
                }
            }
            return target;
        } else {
            return source;
        }
    }

    public static Map<String, Object> beanToMap(Class<?> objCls, Object source, MapConvertFeature... features) {
        return beanToMap(null, objCls, source, features);
    }

    public static Map<String, Object> beanToMap(String keyName, Class<?> objCls, Object source, MapConvertFeature... features) {
        if (objCls == null) {
            throw new IllegalStateException("instance class can not be null");
        }
        Map<String, Object> result = new HashMap<>();
        if (source == null) {
            try {
                Object object = objCls.newInstance();
                Map<String, Object> target = convertBeanToMap4JavaBean(object, new HashMap<>());
                if (!Strings.isNullOrEmpty(keyName)) {
                    result.put(keyName, target);
                } else {
                    if (!MapUtils.isEmpty(target)) {
                        result.putAll(target);
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return result;
        } else {
            Map<String, Object> target = convertBeanToMap4JavaBean(source, new HashMap<>());
            // 如果指定key,则返回key,map结构
            if (!Strings.isNullOrEmpty(keyName)) {
                result.put(keyName, target);
            } else {
                if (!MapUtils.isEmpty(target)) {
                    result.putAll(target);
                }
            }
            return result;
        }
    }

    /**
     * @Title: convertBean
     * @Description: 递归将java bean及其中字段值包含java bean的转换为map。
     * @param: bean instance
     * @param: 存储javabean字段和定义对其声明的javaBean实体对象，判断循环引用
     * @param: map对象中的key value转换风格
     * @return: 转换后的map对象
     */
    private static Map<String, Object> convertBeanToMap4JavaBean(Object target, Map<Field, Object> javaBeanFieldObject) {
        if (target == null || !TypeUtils.isJavaBeanClass(target.getClass())) {
            return new HashMap<>();
        }
        Map<String, Object> result = new HashMap<>();
        Map<String, FieldWrapper> fieldMap = ClassWrapper.forClass(target.getClass()).getFieldNameMap();
        if (MapUtils.isEmpty(fieldMap)) {
            return result;
        }
        fieldMap.forEach((name, fieldWrapper) -> {
            if (fieldWrapper == null || Strings.isNullOrEmpty(name)) {
                return;
            }
            Field field = fieldWrapper.getField();
            if (field == null || Modifier.isStatic(field.getModifiers())) {
                return;
            }
            AutoBindField autoBind = field.getAnnotation(AutoBindField.class);
            if (autoBind != null && autoBind.exclude()) {
                return;
            }
            Object value = getFieldValue(field, target);
            Class<?> valueClass = TypeUtils.getFieldActualClass(field, value);
            if (TypeUtils.isCollectionClass(valueClass)) {
                String[] fieldNames = getOutParamFieldName(autoBind, field);
                if (fieldNames != null) {
                    for (String fieldName : fieldNames) {
                        result.put(fieldName, value);
                    }
                }
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
                        beanMap = convertBeanToMap4JavaBean(instance, javaBeanFieldObject);
                    } else {
                        beanMap = convertBeanToMap4JavaBean(value, javaBeanFieldObject);
                    }
                    String[] fieldNames = getOutParamFieldName(autoBind, field);
                    if (fieldNames != null && fieldNames.length != 0) {
                        for (String fieldName : fieldNames) {
                            result.put(fieldName, beanMap);
                        }
                    } else {
                        throw new IllegalStateException("unkown field name");
                    }
                } catch (Exception e) {
                    result.putAll(new HashMap<>());
                }
            } else if (TypeUtils.isMapClass(valueClass)) {
                String[] fieldNames = getOutParamFieldName(autoBind, field);
                if (fieldNames != null && fieldNames.length != 0) {
                    for (String fieldName : fieldNames) {
                        result.put(fieldName, value);
                    }
                } else {
                    throw new IllegalStateException("unkown field name");
                }
            } else if (TypeUtils.isArrayClass(valueClass)) {
                String[] fieldNames = getOutParamFieldName(autoBind, field);
                if (fieldNames != null) {
                    for (String fieldName : fieldNames) {
                        result.put(fieldName, value);
                    }
                }
            } else {
                if (value == null && autoBind != null) {
                    String defaultValue = autoBind.defaultValue();
                    if (!Strings.isNullOrEmpty(defaultValue)) {
                        value = CastUtils.setNumberScale(convert(defaultValue, field.getType()), autoBind);
                        value = CastUtils.formatDate(value, autoBind);
                    }
                } else {
                    // 转换数字和日期格式
                    value = CastUtils.setNumberScale(value, autoBind);
                    value = CastUtils.formatDate(value, autoBind);
                }
                String[] fieldNames = getOutParamFieldName(autoBind, field);
                if (fieldNames != null) {
                    for (String fieldName : fieldNames) {
                        result.put(fieldName, value);
                    }
                }
            }
        });
        return result;
    }

    private static Object getFieldValue(Field field, Object instance) {
        field.setAccessible(true);
        try {
            return field.get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @Title: getParamName
     * @Description: 返回出参字段名称
     * @param: autoBind
     * @param: field
     * @return: 出参字段名称数组
     * @throws
     */
    private static String[] getOutParamFieldName(AutoBindField autoBind, Field field) {
        if (autoBind != null && autoBind.sendFieldName() != null && autoBind.sendFieldName().length != 0) {
            return autoBind.sendFieldName();
        } else {
            return new String[] {field.getName()};
        }
    }

    /**
     * @Title: convertMapKeyStyle
     * @Description: 转换map中key字符串的大小写展示风格
     * @param: map key
     * @param: features
     * @return: 转换后的key字符串
     * @throws
     */
    private static String convertMapKeyStyle(Object key, int features) {
        String mapKey = String.valueOf(key);
        if (Strings.isNullOrEmpty(mapKey)) {
            return null;
        } else {
            String keyValue;
            if (MapConvertFeature.isEnabled(features, MapConvertFeature.KEY_LOWER_CAMEL)) {
                keyValue = convertStrStyle(mapKey, CaseFormat.LOWER_CAMEL);
            } else if (MapConvertFeature.isEnabled(features, MapConvertFeature.KEY_LOWER_HYPHEN)) {
                keyValue = convertStrStyle(mapKey, CaseFormat.LOWER_HYPHEN);
            } else if (MapConvertFeature.isEnabled(features, MapConvertFeature.KEY_LOWER_UNDERSCORE)) {
                keyValue = convertStrStyle(mapKey, CaseFormat.LOWER_UNDERSCORE);
            } else if (MapConvertFeature.isEnabled(features, MapConvertFeature.KEY_UPPER_CAMEL)) {
                keyValue = convertStrStyle(mapKey, CaseFormat.UPPER_CAMEL);
            } else if (MapConvertFeature.isEnabled(features, MapConvertFeature.KEY_UPPER_UNDERSCORE)) {
                keyValue = convertStrStyle(mapKey, CaseFormat.UPPER_UNDERSCORE);
            }  else {
                keyValue = mapKey;
            }
            return keyValue;
        }
    }

    /**
     * @Title: convertWord
     * @Description: 转换字符串大小写风格
     * @param: @param str
     * @param: @param targetFormat
     */
    private static String convertStrStyle(String str, CaseFormat targetFormat) {
        if (Strings.isNullOrEmpty(str)) {
            return null;
        } else {
            CaseFormat sourceFormat;
            if (str.matches("[A-Z0-9]+")) {
                sourceFormat = CaseFormat.UPPER_UNDERSCORE;
            } else if (str.matches("[a-z0-9]+")) {
                sourceFormat = CaseFormat.LOWER_UNDERSCORE;
            } else {
                sourceFormat = resolveStrStyle(str);
            }
            if (sourceFormat == null) {
                return str;
            } else {
                return sourceFormat.to(targetFormat, str);
            }
        }
    }

    private static CaseFormat resolveStrStyle(String str) {
        if (Strings.isNullOrEmpty(str)) {
            return null;
        } else if (Pattern.matches("^[A-Z][a-zA-Z0-9]*$", str)) {
            return CaseFormat.UPPER_CAMEL;
        } else if (Pattern.matches("^[a-z][a-zA-Z0-9]*$", str)) {
            return CaseFormat.LOWER_CAMEL;
        } else if (Pattern.matches("^[a-z0-9]+(_[a-z0-9]+)*$", str)) {
            return CaseFormat.LOWER_UNDERSCORE;
        } else if (Pattern.matches("^[a-z0-9]+(-[a-z0-9]+)*$", str)) {
            return CaseFormat.LOWER_HYPHEN;
        } else if (Pattern.matches("^[A-Z0-9]+(_[A-Z0-9]+)*$", str)) {
            return CaseFormat.UPPER_UNDERSCORE;
        } else {
            return null;
        }
    }

    /**
     * @Title: convertMapValueType
     * @Description: 将map中value转换为两种类型(字符串或其他类型)
     * @param: map value
     * @param: features MapConvertFeature
     * @return: 转换后的map value
     */
    private static Object convertMapValueType(Object value, Map<Field, Object> javaBeanFieldObject, MapConvertFeature[] features) {
        if (value == null) {
            return value;
        }
        if (TypeUtils.isCollectionClass(value.getClass())) {
            return value;
        } else if (TypeUtils.isArrayClass(value.getClass())) {
            return value;
        } else if (TypeUtils.isMapClass(value.getClass())) {
            return value;
        } else if (TypeUtils.isJavaBeanClass(value.getClass())) {
            return convertBeanToMap4JavaBean(value, javaBeanFieldObject);
        } else {
            if ((!MapConvertFeature.isEnabled(MapConvertFeature.of(features), MapConvertFeature.VALUE_CONVERT_TO_OBJ) && !MapConvertFeature.isEnabled(MapConvertFeature.of(features), MapConvertFeature.VALUE_CONVERT_TO_STRING))) {
                return value;
            } else {
                if (String.class.isAssignableFrom(value.getClass())) {
                    if (MapConvertFeature.isEnabled(MapConvertFeature.of(features), MapConvertFeature.VALUE_CONVERT_TO_STRING)) {
                        return String.valueOf(value);
                    } else {
                        Class<?> targetType = typeInference(String.valueOf(value));
                        return convert(String.valueOf(value), targetType);
                    }
                } else {
                    if (MapConvertFeature.isEnabled(MapConvertFeature.of(features), MapConvertFeature.VALUE_CONVERT_TO_STRING)) {
                        return convert(value, String.class);
                    } else {
                        return value;
                    }
                }
            }
        }
    }

    /**
     * @Title: typeInference
     * @Description: 类型推断,尝试基于一些简单规则推断字符串原始类型
     * @param: 原始字符串
     * @return: 实际类型class
     */
    private static Class<?> typeInference(String str) {
        if (Strings.isNullOrEmpty(str)) {
            return null;
        } else if (CastUtils.isCreatable(str)) {
            try {
                Integer.parseInt(str);
                return Integer.class;
            } catch (Exception e) {
                try {
                    Long.parseLong(str);
                    return Long.class;
                } catch (Exception ex) {
                    return BigDecimal.class;
                }
            }
        } else if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
            return Boolean.class;
        } else if (str.contains(",")) {
            return List.class;
        } else if (CastUtils.parseDateTime(str) != null) {
            return Date.class;
        } else {
            return String.class;
        }
    }

    private static Object convert(Object value, Class<?> targetType) {
        try {
            return ConversionServiceProvider.getGlobalConversionService().convert(value, targetType);
        } catch (Exception e) {
            return value;
        }
    }

}