package cn.com.hjack.autobind.generater;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.ClassWrapper;
import cn.com.hjack.autobind.utils.FieldWrapper;
import cn.com.hjack.autobind.utils.TypeUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewMethod;
import org.apache.commons.collections4.MapUtils;

import com.google.common.base.Strings;


/**
 * @ClassName: MapperGenerator
 * @Description: TODO
 * @author houqq
 * @date: 2025年11月5日
 *
 */
public class MapperGenerater implements Generater<Mapper<?>> {

    // key -> java bean class value -> mapper
    private static Map<Class<?>, Mapper<?>> toMapCache = new ConcurrentHashMap<>();

    // outerkey -> target class inner key -> source class
    private static Map<Class<?>, Map<Class<?>, Mapper<?>>> toJavaBeanMapperCache = new ConcurrentHashMap<>();

    private TypeWrapper targetType;

    private ResolveConfig config;

    private Class<?> sourceClass;

    private ClassWrapper classWrapper;

    public MapperGenerater(Class<?> sourceClass, TypeWrapper targetType, ResolveConfig config) {
        if (sourceClass == null) {
            throw new IllegalStateException("source class can not be null");
        }
        if (!TypeUtils.isMapClass(sourceClass) && !TypeUtils.isJavaBeanClass(sourceClass)) {
            throw new IllegalStateException("source class must be bean or map");
        }
        if (targetType == null || targetType.resolve() == null) {
            throw new IllegalStateException("target type can not be null");
        }
        this.sourceClass = sourceClass;
        this.targetType = targetType;
        this.config = config;
        if (TypeUtils.isJavaBeanClass(targetType.resolve())) {
            this.classWrapper = ClassWrapper.forClass(targetType.resolve());
        }
    }

    @Override
    public Mapper<?> generate() {
        Class<?> targetClass = targetType.resolveOrObject();
        if (TypeUtils.isMapClass(targetClass)) {
            if (!TypeUtils.isJavaBeanClass(sourceClass)) {
                throw new UnsupportedOperationException("unsupport source type");
            }
            return toMapCache.computeIfAbsent(sourceClass, (key) -> {
                try {
                    ClassPool pool = ClassPool.getDefault();
                    // 生成导入
                    generateImportsStub(pool);
                    CtClass ctClass = generateBeanToMapMapperClass(pool, sourceClass);
                    generateConstructStub(pool, ctClass);
                    generateBeanToMapMethodStub(ctClass);
                    Class<?> mapperClass = ctClass.toClass();
                    Constructor<?> constructor = mapperClass.getConstructor(Object.class, Map.class, ResolveConfig.class);
                    return (Mapper<?>) constructor.newInstance(TypeUtils.createMap(targetClass), new HashMap<>(), config);
                } catch (Exception e) {
                    return null;
                }
            });
        } else if (TypeUtils.isJavaBeanClass(targetClass)) {
            if (!TypeUtils.isMapClass(sourceClass) && !TypeUtils.isJavaBeanClass(sourceClass)) {
                throw new UnsupportedOperationException("unsupport source type");
            }
            Map<Class<?>, Mapper<?>> resovlerMap = toJavaBeanMapperCache.computeIfAbsent(targetClass, (key) -> {
                return new ConcurrentHashMap<>();
            });
            return resovlerMap.computeIfAbsent(sourceClass, (key) -> {
                try {
                    ClassPool pool = ClassPool.getDefault();
                    // 生成导入
                    generateImportsStub(pool);
                    // 生成类
                    CtClass ctClass = generateMapToBeanMapperClass(pool, sourceClass, targetClass);
                    generateConstructStub(pool, ctClass);
                    if (TypeUtils.isMapClass(sourceClass)) {
                        generateMapToBeanMethodStub(ctClass);
                    } else {
                        generateBeanToBeanMethodStub(ctClass);
                    }
                    Class<?> mapperClass = ctClass.toClass();
                    Constructor<?> constructor = mapperClass.getConstructor(Object.class, Map.class, ResolveConfig.class);
                    return (Mapper<?>) constructor.newInstance(targetType.resolveOrThrow().newInstance(), targetType.resolveVariableContext(), config);
                } catch (Exception e) {
                    return null;
                }
            });
        } else {
            throw new UnsupportedOperationException("unsupport target type");
        }
    }

    private CtClass generateBeanToMapMapperClass(ClassPool pool, Class<?> sourceClass) throws Exception {
        CtClass cls = pool.makeClass(CastUtils.formatWithNoNewLine("cn.com.hjack.autobind.converter.BeanToMap$%s$Map", sourceClass.getName()));
        cls.setSuperclass(pool.get("cn.com.hjack.autobind.binder.AbstractBeanMapper"));
        return cls;
    }

    private CtClass generateMapToBeanMapperClass(ClassPool pool, Class<?> sourceClass, Class<?> targetClass) throws Exception {
        CtClass cls = pool.makeClass(CastUtils.formatWithNoNewLine("cn.com.hjack.autobind.converter.MapToBean$%s$%s", sourceClass.getName(), targetClass.getName()));
        cls.setSuperclass(pool.get("cn.com.hjack.autobind.binder.AbstractBeanMapper"));
        return cls;
    }


    private void generateConstructStub(ClassPool pool, CtClass cls) throws Exception {
        CtConstructor constructor = new CtConstructor(new CtClass[] {pool.get("java.lang.Object"), pool.get("java.util.Map"), pool.get("cn.com.hjack.autobind.ResolveConfig")}, cls);
        constructor.setBody(generateConstructBodyStub());
        cls.addConstructor(constructor);
    }

    private String generateConstructBodyStub() {
        return CastUtils.format("{") +
                CastUtils.formatAndIndent2("super($1, $2, $3);") +
                CastUtils.format("}");
    }

    private void generateBeanToBeanMethodStub(CtClass ctClass) {
        Map<String, FieldWrapper> fieldNameMap = classWrapper.getFieldNameMap();
        generateToBean4BeanMethodStub(ctClass, fieldNameMap);
        generateMethod(generateBeanToBeanMethodBodyStub(), ctClass);
    }

    private String generateBeanToBeanMethodBodyStub() {
        // 如果chain node为空,且目标字段为java bean，则将source java bean转为map返回
        return CastUtils.format("public Result beanToBean(Object source, Validator validator) {") +
                CastUtils.formatAndIndent2("toBean4Bean(source, validator);") +
                CastUtils.formatAndIndent2("return super.validate();") +
                CastUtils.format("}");
    }

    private void generateGatValue4BeanMethodStub(CtClass ctClass, Map<String, FieldWrapper> fieldNameMap) {
        ClassWrapper sourceClassWrapper = ClassWrapper.forClass(sourceClass);
        fieldNameMap.forEach((fieldName, targetField) -> {
            FieldWrapper.FieldNodeSlot fieldNodeSlot = sourceClassWrapper.getFieldSlotByFieldName(targetField.getRecvFieldName(), this.config);
            generateMethod(fieldNodeSlot.generateInvokeStub(fieldName, "source"), ctClass);
        });
    }

    /**
     * @Title: generateConvertMapToBeanStub
     * @Description: 生成将map转化为bean的代码桩
     * @param: CtClass
     * @param: TypeWrapper
     * @return: String
     */
    private void generateToBean4BeanMethodStub(CtClass ctClass, Map<String, FieldWrapper> fieldNameMap) {
        generateGatValue4BeanMethodStub(ctClass, fieldNameMap);
        generateMethod(generateBindMethodBodyStub(fieldNameMap), ctClass);
        generateMethod(generateToBean4BeanMethodBodyStub(fieldNameMap), ctClass);
    }
    /**
     * @Title: generateFindValueMethod
     * @Description: 从source java bean中找到字段值
     * @param: source java bean class
     * @param: 从source java bean class搜索相应字段的field chain
     * @param: 目标字段类型(目标java bean字段class,如果目标为map,则为空)
     * @param: 目标字段名称
     * @return: 代码桩
     */
    private String generateToBean4BeanMethodBodyStub(Map<String, FieldWrapper> fieldNameMap) {
        StringBuilder body = new StringBuilder();
        // 如果chain node为空,且目标字段为java bean，则将source java bean转为map返回
        body.append(CastUtils.format("private void toBean4Bean(Object source, Validator validator) {"));
        body.append(CastUtils.formatAndIndent2("if (source != null && !(source instanceof %s)) {", TypeUtils.getCanonicalName(sourceClass)));
        body.append(CastUtils.formatAndIndent4("throw new IllegalStateException(\"param class is not an instance of source\");"));
        body.append(CastUtils.formatAndIndent2("}"));
        body.append(CastUtils.formatAndIndent2("%s object = (%s) source;", TypeUtils.getCanonicalName(sourceClass), TypeUtils.getCanonicalName(sourceClass)));
        body.append(CastUtils.formatAndIndent2("EvaluationContext context = new StandardEvaluationContext(getTarget());"));
//		body.append(CastUtils.formatAndIndent2("ExpressionParser parser = new SpelExpressionParser();"));
        fieldNameMap.forEach((fieldName, beanField) -> {
            Field field = beanField.getField();
            AutoBindField autoBind = beanField.getAutoBind();
            // 新增condition校验器
            String validateBody = generateAddELConditionValidatorStub(autoBind, field);
            if (!Strings.isNullOrEmpty(validateBody)) {
                body.append(CastUtils.formatAndIndent2(validateBody));
            }
            body.append(CastUtils.formatAndIndent2("FieldWrapper %s_fieldWrapper = ClassWrapper.forClass(%s).getField(\"%s\");", fieldName, TypeUtils.getCanonicalNameWithSuffix(this.classWrapper.getBeanCls()), fieldName));
            body.append(CastUtils.formatAndIndent2("FieldTypeWrapper %s_typeWrapper = TypeWrappers.getFieldType(%s_fieldWrapper.getField(), getTarget().getClass(), super.variableContext);", fieldName, fieldName));
            body.append(CastUtils.formatAndIndent2("ResolvableConverter %s_resolver = ResolvableConverters.getConverter(%s_typeWrapper);", fieldName, fieldName));
            body.append(CastUtils.formatAndIndent2("fieldEditors.put(\"%s\", new cn.com.hjack.autobind.binder.CustomFieldEditor(this.%sValue(object), this, %s_fieldWrapper.getField(), %s_fieldWrapper.getAutoBind(), %s_typeWrapper, %s_resolver));",
                    fieldName, fieldName, fieldName, fieldName, fieldName, fieldName));
        });
        body.append(CastUtils.formatAndIndent2("super.addGlobalValidator(validator);"));
        body.append(CastUtils.formatAndIndent2("bind();"));
        body.append(CastUtils.format("}"));
        return body.toString();
    }

    private void generateBeanToMapMethodStub(CtClass ctClass) {
        generateMethod(generateBeanToMapMethodBodyStub("", sourceClass), ctClass);
        generateMethod(generateBeanToMapMethodBodyStub(), ctClass);
    }
    private String generateBeanToMapMethodBodyStub(String methodName, Class<?> beanClass) {
        StringBuilder body = new StringBuilder();
        Map<String, FieldWrapper> fields = ClassWrapper.forClass(beanClass).getFieldNameMap();
        if (MapUtils.isEmpty(fields)) {
            return body.toString();
        } else {
            body.append(CastUtils.format("private Map do%sBeanToMap(%s source) {", methodName, TypeUtils.getCanonicalName(beanClass)));
            body.append(CastUtils.formatAndIndent2("if (source == null) {"));
            body.append(CastUtils.formatAndIndent4("return new HashMap();"));
            body.append(CastUtils.formatAndIndent2("}"));
            body.append(CastUtils.formatAndIndent2("Map map = new HashMap();"));
            fields.forEach((fieldName, field) -> {
                Class<?> fieldType = field.getFieldType();
                if (TypeUtils.isCollectionClass(fieldType)
                        || TypeUtils.isMapClass(fieldType)
                        || TypeUtils.isArrayClass(fieldType)) {
                    String[] sendFieldNames = CastUtils.getSendFieldNames(field.getAutoBind(), field.getField());
                    if (sendFieldNames != null) {
                        for (String sendFieldName : sendFieldNames) {
                            body.append(CastUtils.formatAndIndent2("map.put(\"%s\", source.%s());", sendFieldName,
                                    field.getReadMethod().getName()));
                        }
                    }
                } else if (TypeUtils.isJavaBeanClass(fieldType)) {
                    String[] sendFieldNames = CastUtils.getSendFieldNames(field.getAutoBind(), field.getField());
                    if (sendFieldNames != null) {
                        for (String sendFieldName : sendFieldNames) {
                            body.append(CastUtils.formatAndIndent2("map.put(\"%s\", BeanMappers.getMapper(%s, TypeWrappers.getType(Map.class), getConfig()).beanToMap(source.%s()));", sendFieldName,
                                    TypeUtils.getCanonicalNameWithSuffix(fieldType), field.getReadMethod().getName()));
                        }
                    }
                } else {
                    if (fieldType.isPrimitive()) {
                        body.append(CastUtils.formatAndIndent4("%s %s_value = CastUtils.toWrap%sValue(source.%s());", TypeUtils.getPrimitiveClassWrapName(fieldType),
                                fieldName, TypeUtils.getPrimitiveClassWrapName(fieldType), field.getReadMethod().getName()));
                    } else {
                        body.append(CastUtils.formatAndIndent4("%s %s_value = source.%s();", TypeUtils.getCanonicalName(fieldType), fieldName, field.getReadMethod().getName()));
                    }
                    AutoBindField autoBind = field.getAutoBind();
                    if (autoBind != null) {
                        if (!Strings.isNullOrEmpty(autoBind.defaultValue())) {
                            body.append(CastUtils.formatAndIndent4("if (%s_value == null) {", fieldName));
                            body.append(CastUtils.formatAndIndent6("%s_value = CastUtils.convert(%s_value, %s);", fieldName, fieldName, TypeUtils.getCanonicalNameWithSuffix(fieldType)));
                            body.append(CastUtils.formatAndIndent4("}"));
                        }
                        body.append(CastUtils.formatAndIndent4("%s_value = CastUtils.setNumberScale(%s_value, %s, %s);", fieldName, fieldName,
                                autoBind.scale(), CastUtils.getRoundingModeStr(autoBind.roundingMode())));
                        body.append(CastUtils.formatAndIndent4("%s_value = CastUtils.formatDate(%s_value, \"%s\");", fieldName, fieldName, autoBind.format()));
                        String[] sendFieldNames = CastUtils.getSendFieldNames(field.getAutoBind(), field.getField());
                        if (sendFieldNames != null) {
                            for (String sendFieldName : sendFieldNames) {
                                body.append(CastUtils.formatAndIndent4("map.put(\"%s\", %s_value);", sendFieldName, fieldName));
                            }
                        }
                    } else {
                        String[] sendFieldNames = CastUtils.getSendFieldNames(field.getAutoBind(), field.getField());
                        if (sendFieldNames != null) {
                            for (String sendFieldName : sendFieldNames) {
                                body.append(CastUtils.formatAndIndent4("map.put(\"%s\", %s_value);", sendFieldName, fieldName));
                            }
                        }
                    }
                }
            });
            body.append(CastUtils.formatAndIndent2("return map;"));
            body.append(CastUtils.format("}"));
            return body.toString();
        }
    }

    private String generateBeanToMapMethodBodyStub() {
        StringBuilder body = new StringBuilder();
        body.append(CastUtils.format("public Map beanToMap(Object bean) {"));
        body.append(CastUtils.formatAndIndent2("return doBeanToMap((%s) bean);", TypeUtils.getCanonicalName(sourceClass)));
        body.append(CastUtils.format("}"));
        return body.toString();
    }
    private void generateMapToBeanMethodStub(CtClass ctClass) {
        generateToBean4MapMethodStub(ctClass);
        generateMethod(generateMapToBeanMethodBodyStub(), ctClass);
    }

    private String generateMapToBeanMethodBodyStub() {
        StringBuilder body = new StringBuilder();
        body.append(CastUtils.format("public Result mapToBean(Map map, Validator validator) {"));
        body.append(CastUtils.formatAndIndent2("toBean4Map(map, validator);"));
        body.append(CastUtils.formatAndIndent2("return super.validate();"));
        body.append(CastUtils.format("}"));
        return body.toString();
    }

    /**
     * @Title: generateConvertMapToBeanStub
     * @Description: 生成将map转化为bean的代码桩
     * @param: CtClass
     * @param: TypeWrapper
     * @return: String
     */
    private void generateToBean4MapMethodStub(CtClass ctClass) {
        Map<String, FieldWrapper> wrappers = classWrapper.getFieldNameMap();
        generateMethod(generateBindMethodBodyStub(wrappers), ctClass);
        generateMethod(generateToBean4MapMethodBodyStub(wrappers), ctClass);
    }

    /**
     * @Title: generateFindValueMethod
     * @Description: 从source java bean中找到字段值
     * @param: source java bean class
     * @param: 从source java bean class搜索相应字段的field chain
     * @param: 目标字段类型(目标java bean字段class,如果目标为map,则为空)
     * @param: 目标字段名称
     * @return: 代码桩
     */
    private String generateToBean4MapMethodBodyStub(Map<String, FieldWrapper> fieldNameMap) {
        StringBuilder body = new StringBuilder();
        body.append(CastUtils.format("protected void toBean4Map(Map map, Validator validator) {"));
        body.append(CastUtils.formatAndIndent2("EvaluationContext context = new StandardEvaluationContext(getTarget());"));
//		body.append(CastUtils.formatAndIndent2("ExpressionParser parser = new SpelExpressionParser();"));
        fieldNameMap.forEach((fieldName, beanField) -> {
            Field field = beanField.getField();
            AutoBindField autoBind = beanField.getAutoBind();
            // 新增condition校验器
            String validateBody = generateAddELConditionValidatorStub(autoBind, field);
            if (!Strings.isNullOrEmpty(validateBody)) {
                body.append(CastUtils.formatAndIndent2(validateBody));
            }
            body.append(CastUtils.formatAndIndent2("FieldWrapper %s_fieldWrapper = ClassWrapper.forClass(%s).getField(\"%s\");", fieldName, TypeUtils.getCanonicalNameWithSuffix(this.classWrapper.getBeanCls()), fieldName));
            body.append(CastUtils.formatAndIndent2("FieldTypeWrapper %s_typeWrapper = TypeWrappers.getFieldType(%s_fieldWrapper.getField(), target.getClass(), super.variableContext);", fieldName, fieldName));
            body.append(CastUtils.formatAndIndent2("ResolvableConverter %s_resolver = ResolvableConverters.getConverter(%s_typeWrapper);", fieldName, fieldName));
            body.append(CastUtils.formatAndIndent2("fieldEditors.put(\"%s\", new cn.com.hjack.autobind.binder.CustomFieldEditor(super.getValue(map, %s_fieldWrapper.getAutoBind(), %s_fieldWrapper.getField()), this, %s_fieldWrapper.getField(), %s_fieldWrapper.getAutoBind(), %s_typeWrapper, %s_resolver));",
                    fieldName, fieldName, fieldName, fieldName, fieldName, fieldName, fieldName));
        });
        body.append(CastUtils.formatAndIndent2("super.addGlobalValidator(validator);"));
        body.append(CastUtils.formatAndIndent2("bind();"));
        body.append(CastUtils.format("}"));
        return body.toString();
    }
    /**
     * @Title: addConditionValidator
     * @Description: 添加基于字段的EL表达式校验器
     * @param: EvaluationContext
     * @param: ExpressionParser
     * @param: AutoBindField
     * @param: Field
     */
    private String generateAddELConditionValidatorStub(AutoBindField autoBind, Field field) {
        // 如果autbind注解为空，或者EL表达式校验没开启，则跳过
        if (!ConvertFeature.isEnabled(config.convertFeature(), ConvertFeature.EL_VALIDATE_ENABLE)
                && (autoBind == null || !ConvertFeature.isEnabled(autoBind.features(), ConvertFeature.EL_VALIDATE_ENABLE))) {
            return null;
        }
        if (autoBind == null) {
            return null;
        } else {
            String condition = autoBind.condition();
            if (!Strings.isNullOrEmpty(condition)) {
                StringBuilder body = new StringBuilder();
                body.append(CastUtils.formatAndIndent2("super.addValidator(new Validator() {"));
                body.append(CastUtils.formatAndIndent4("ExpressionParser parser = new SpelExpressionParser();"));
                body.append(CastUtils.formatAndIndent4("public void validator(Object t, ValidationErrors errors) {"));
                body.append(CastUtils.formatAndIndent6("Expression exps = parser.parseExpression(\"%s\");", condition));
                body.append(CastUtils.formatAndIndent6("Boolean exprs = exps.getValue(context, Boolean.class);"));
                body.append(CastUtils.formatAndIndent6("if (!Objects.equals(exprs, Boolean.TRUE)) {"));
                if (!Strings.isNullOrEmpty(autoBind.errorMsg())) {
                    body.append(CastUtils.formatAndIndent8("errors.collectError(\"%s\", \"%s\");", field.getName(), field.getName() + "." + autoBind.errorMsg()));
                } else {
                    body.append(CastUtils.formatAndIndent8("errors.collectError(\"%s\", \"%s.bind error\");", field.getName(), field.getName()));
                }
                body.append(CastUtils.formatAndIndent6("}"));
                body.append(CastUtils.formatAndIndent4("}"));
                body.append(CastUtils.formatAndIndent2("});"));
                return body.toString();
            } else {
                return null;
            }
        }
    }

    private String generateBindMethodBodyStub(Map<String, FieldWrapper> fieldNameMap) {
        StringBuilder body = new StringBuilder();
        body.append(CastUtils.format("protected void bind() {"));
        fieldNameMap.forEach((fieldName, beanField) -> {
            body.append(CastUtils.formatAndIndent2("CustomFieldEditor %s_editor = (CustomFieldEditor) super.fieldEditors.get(\"%s\");", fieldName, fieldName));
            body.append(CastUtils.formatAndIndent2("if (%s_editor != null) {", fieldName));
            if (beanField.getFieldType().isPrimitive()) {
                body.append(CastUtils.formatAndIndent4("%s %s_value = CastUtils.toPrim%sValue((%s) %s_editor.edit());", TypeUtils.getCanonicalName(beanField.getFieldType()),
                        fieldName, TypeUtils.getPrimitiveClassWrapName(beanField.getFieldType()), TypeUtils.getPrimitiveClassWrapName(beanField.getFieldType()), fieldName));
            } else {
                body.append(CastUtils.formatAndIndent4("%s %s_value = (%s) %s_editor.edit();", TypeUtils.getCanonicalName(beanField.getFieldType()),
                        fieldName, TypeUtils.getCanonicalName(beanField.getFieldType()), fieldName));
            }
            body.append(CastUtils.formatAndIndent4("%s targetObject = (%s) getTarget();", TypeUtils.getCanonicalName(classWrapper.getBeanCls()), TypeUtils.getCanonicalName(classWrapper.getBeanCls())));
            body.append(CastUtils.formatAndIndent4("targetObject.%s(%s_value);", beanField.getWriteMethod().getName(), fieldName));
            body.append(CastUtils.formatAndIndent2("}"));
        });
        body.append(CastUtils.format("}"));
        return body.toString();
    }
    private void generateMethod(String methodBody, CtClass ctClass) {
        if (Strings.isNullOrEmpty(methodBody)) {
            throw new IllegalStateException("method body is empty");
        }
        try {
            ctClass.addMethod(CtNewMethod.make(methodBody, ctClass));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void generateImportsStub(ClassPool pool) {
        pool.importPackage("cn.com.hjack.autobind.utils.ClassWrapper");
        pool.importPackage("cn.com.hjack.autobind.utils.FieldWrapper");
        pool.importPackage("cn.com.hjack.autobind.FieldTypeWrapper");
        pool.importPackage("cn.com.hjack.autobind.ResolvableConverter");
        pool.importPackage("cn.com.hjack.autobind.binder.TypeWrappers");
        pool.importPackage("cn.com.hjack.autobind.binder.CustomFieldEditor");
        pool.importPackage("cn.com.hjack.autobind.autobind.ResolveConfig");
        pool.importPackage("cn.com.hjack.autobind.converter.ResolvableConverters");
        pool.importPackage("cn.com.hjack.autobind.Result");
        pool.importPackage("cn.com.hjack.autobind.utils.CastUtils");
        pool.importPackage("cn.com.hjack.autobind.Mapper");
        pool.importPackage("cn.com.hjack.autobind.binder.BeanMappers");
        pool.importPackage("java.util.Map");
        pool.importPackage("java.util.HashMap");
        pool.importPackage("java.util.Objects");
        pool.importPackage("java.lang.reflect.Field");
        pool.importPackage("cn.com.hjack.autobind.Validator");
        pool.importPackage("org.springframework.expression.EvaluationContext");
        pool.importPackage("org.springframework.expression.Expression");
        pool.importPackage("org.springframework.expression.ExpressionParser");
        pool.importPackage("org.springframework.expression.spel.standard.SpelExpressionParser");
        pool.importPackage("org.springframework.expression.spel.support.StandardEvaluationContext");
        pool.importPackage("org.springframework.boot.context.properties.bind.validation.ValidationErrors");
    }

}
