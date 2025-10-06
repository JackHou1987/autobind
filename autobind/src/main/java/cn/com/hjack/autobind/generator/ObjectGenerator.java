/**
 *
 */
package cn.com.hjack.autobind.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cn.com.hjack.autobind.*;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.utils.*;
import com.google.common.base.Strings;
import javassist.*;
import javassist.bytecode.ClassFile;
import org.apache.commons.collections4.MapUtils;


/**
 * @ClassName: DefaultJavaBeanResolverGenerator
 * @Description: 对象生成器,负责生成各类代理对象，用于性能加速或者其他
 * @author houqq
 * @date: 2025年7月17日
 */
public class ObjectGenerator {

    public static ObjectGenerator instance = new ObjectGenerator();

    // outerkey -> target java bean class, innerkey -> source java bean class(缓存source为javabean或map的javabean resolver缓存)
    private Map<Class<?>, Map<Class<?>, TypeValueResolver>> javaBeanResolverCache = new ConcurrentHashMap<>();

    // outerkey -> target array class, innerkey -> source class(缓存source为object的array resolver缓存)
    private Map<Class<?>, Map<Class<?>, TypeValueResolver>> arrayResolverCache = new ConcurrentHashMap<>();

    // outerkey -> map value type, innerkey -> source class(缓存source为javabean的map resolver缓存)
    private Map<Class<?>, Map<Class<?>, TypeValueResolver>> mapResolverCache = new ConcurrentHashMap<>();

    private Map<Class<?>, Object> lazyLoadProxyCache = new ConcurrentHashMap<>();

    private Map<Class<?>, Map<String, Object>> beanToMapProxyCache = new ConcurrentHashMap<>();

    private static final String GET_RESOLVER_CODE = "TypeValueResolver resolver = TypeValueResolvers.getResolver(%s);\n";

    private static final String CALL_RESOLVE_METHOD_CODE = "Result childResult = resolver.resolve(%s, %s, %s);\n";

    private static final String NEW_TARGET_CODE = "  %s target = new %s%s;\n";

    private static final String CONVERT_SOURCE_CODE = "  %s source = (%s) object;\n";

    private static final String FIND_FIELD_CODE = "Field field = ClassWrapper.forClass(%s).findField(\"%s\");\n";

    private static final String GET_FIELD_TYPE_CODE = "FieldTypeWrapper fieldType = TypeWrappers.getFieldType(%s, %s, %s);\n";

    private static final String SET_ERROR_RESULT_CODE = "result.setSuccess(false);\nresult.setResultCode(\"%s\");\nresult.setResultMsg(\"%s\");\n";

    private static final String CastUtils_GET_PRIM_CODE = "CastUtils.toPrim%sValue(%s);\n";

    private static final String CastUtils_GET_WRAP_CODE = "CastUtils.toWrap%sValue(%s);\n";

    private static final String PARSE_EXPRESSION_CODE = "Expression exps = parser.parseExpression(\"%s\");\nBoolean exprs = exps.getValue(context, Boolean.class);\nif (!Objects.equals(exprs, Boolean.TRUE)) {\n%s\n}\n";

    private static final String GET_INSTANCE_CODE = "(%s) childResult.instance();\n";

    private static final String SET_VALUE_CODE = "target.%s(%s);\n";

    private static final String GET_VALUE_CODE = "source.%s()";

    private static final String SOURCE_NULL_CODE = "  if (object == null) {\n    %s target = new %s;\n    return DefaultResult.defaultSuccessResult(target);\n  }\n";

    private static final String GET_SOURCE_VALUE_CODE = "  Object %s = this.%sValue(source);\n";

    private ObjectGenerator() {

    }

    /**
     * @Title: generateJavaBeanToMapProxy
     * @Description: 生成java bean 到 map的代理
     * @param: java bean class
     * @param: java bean object
     * @return: 代理map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateJavaBeanToMapProxy(Class<?> beanClass, Object bean) {
        if (beanClass == null) {
            throw new IllegalStateException("bean class can not be null");
        }
        if (!TypeUtils.isJavaBeanClass(beanClass)) {
            throw new IllegalStateException("proxy class must be java bean");
        }
        if (!TypeUtils.isInstance(beanClass, bean)) {
            throw new IllegalStateException("bean must be an instance of beanclass");
        }
        try {
            return beanToMapProxyCache.computeIfAbsent(beanClass, (key) -> {
                ClassPool pool = ClassPool.getDefault();
                try {
                    generateJavaBeanToMapImportsStub(pool);
                    CtClass ctClass = generateBeanToMapProxyClass(pool, beanClass);
                    generateBeanToMapProxyFields(beanClass, ctClass);
                    generateBeanToMapProxyConstructs(pool, ClassWrapper.forClass(beanClass), ctClass);
                    Method[] methods = Map.class.getDeclaredMethods();
                    for (Method method : methods) {
                        generateBeanToMapProxyMethod(pool, ctClass, ClassWrapper.forClass(beanClass), method);
                    }
                    Class<?> objCls = ctClass.toClass();
                    Constructor<?> constructor = objCls.getConstructor(beanClass);
                    return (Map<String, Object>) constructor.newInstance(bean);
                } catch (Exception e) {
                    return null;
                }
            });
        } catch (Exception e) {
            return null;
        }
    }

    private void generateJavaBeanToMapImportsStub(ClassPool pool) {
        pool.importPackage("java.util.Objects");
        pool.importPackage("java.util.HashSet");
        pool.importPackage("java.util.Set");
        pool.importPackage("java.util.Map");
        pool.importPackage("cn.com.hjack.autobind.generator.ObjectGenerator");
        pool.importPackage("cn.com.hjack.autobind.utils.CastUtils");
    }

    /**
     * @Title: generateBeanToMapProxyClass
     * @Description: 生成bean到map的代理class
     * @param: ClassPool
     * @param: targetClass
     * @return: CtClass
     */
    private CtClass generateBeanToMapProxyClass(ClassPool pool, Class<?> targetClass) throws Exception {
        CtClass cls = pool.makeClass(String.format("cn.com.hjack.autobind.resolver.%s$BeanToMapProxy", targetClass.getSimpleName()));
        cls.setInterfaces(new CtClass[] {pool.get("java.util.Map")});
        return cls;
    }

    /**
     * @Title: generateBeanToMapProxyFields
     * @Description: 生成bean到map的代理字段
     * @param: beanClass
     * @param: ClassPool
     * @param: CtClass
     */
    private void generateBeanToMapProxyFields(Class<?> beanClass, CtClass cls) throws Exception {
        CtField beanField = CtField.make(String.format("private %s bean;\n", TypeUtils.getCanonicalName(beanClass)), cls);
        cls.addField(beanField);
        CtField entrySetField = CtField.make("private Set entrySet = new HashSet();\n", cls);
        cls.addField(entrySetField);
    }

    /**
     * @Title: generateBeanToMapProxyConstructs
     * @Description: 生成bean到map的代理构造器
     * @param: ClassPool
     * @param: ClassWrapper
     * @param: CtClass
     */
    private void generateBeanToMapProxyConstructs(ClassPool pool, ClassWrapper beanClass, CtClass cls) throws Exception {
        CtConstructor constructor = new CtConstructor(new CtClass[] {pool.get(beanClass.getBeanCls().getName())}, cls);
        constructor.setBody(generateBeanToMapConstructContentStub(beanClass));
        cls.addConstructor(constructor);
    }

    /**
     * @Title: generateBeanToMapConstructContent
     * @Description: 生成java bean 到map的构造方法content
     * @param: java bean class
     * @return: String
     */
    private String generateBeanToMapConstructContentStub(ClassWrapper beanClass) {
        StringBuilder body = new StringBuilder();
        body.append("{\n");
        body.append("  if ($1 == null) {\n");
        body.append("    throw new IllegalStateException(\"java bean object can not be null\");\n");
        body.append("  }\n");
        body.append(String.format("  this.bean = (%s) $1;\n", TypeUtils.getCanonicalName(beanClass.getBeanCls())));
        beanClass.getSendFieldNameMap().forEach((fieldName, fieldWrapper) -> {
            if (fieldWrapper.getAutoBind() != null && fieldWrapper.getAutoBind().exclude()) {
                return;
            }
            if (beanClass.getJavaBeanFieldMap().containsKey(fieldName)) {
                body.append(String.format("Object %s_value = (Object) this.bean.%s();\n", fieldName, fieldWrapper.getReadMethod().getName()));
                body.append(String.format("Map child = ObjectGenerator.instance.generateJavaBeanToMapProxy(%s, %s_value);\n", TypeUtils.getCanonicalNameWithSuffix(fieldWrapper.getFieldType()), fieldName));
                body.append(String.format("cn.com.hjack.autobind.generator.ObjectGenerator.MapProxyEntry %s_entry = new cn.com.hjack.autobind.generator.ObjectGenerator.MapProxyEntry(\"%s\", child);\n", fieldName, fieldName));
                body.append(String.format("entrySet.add(%s_entry);\n", fieldName));
            } else {
                body.append(String.format("%s %s_beanValue = this.bean.%s();\n", TypeUtils.getCanonicalName(fieldWrapper.getFieldType()), fieldName, fieldWrapper.getReadMethod().getName()));
                if (fieldWrapper.getFieldType().isPrimitive()) {
                    body.append(String.format("Object %s_objectValue = (Object) CastUtils.toWrap%sValue(%s_beanValue);\n", fieldName, TypeUtils.getPrimitiveClassWrapName(fieldWrapper.getFieldType()), fieldName));
                    body.append(String.format("cn.com.hjack.autobind.generator.ObjectGenerator.MapProxyEntry %s_entry = new cn.com.hjack.autobind.generator.ObjectGenerator.MapProxyEntry(\"%s\", %s_objectValue);\n", fieldName, fieldName, fieldName));
                } else {
                    body.append(String.format("Object %s_objectValue = (Object) %s_beanValue;\n", fieldName, fieldName));
                    body.append(String.format("cn.com.hjack.autobind.generator.ObjectGenerator.MapProxyEntry %s_entry = new cn.com.hjack.autobind.generator.ObjectGenerator.MapProxyEntry(\"%s\", %s_objectValue);\n", fieldName, fieldName, fieldName));
                }
                body.append(String.format("entrySet.add(%s_entry);\n", fieldName));
            }
        });
        body.append("}\n");
        return body.toString();
    }

    /**
     * @Title: generateBeanToMapProxyMethod
     * @Description: 生成bean到map的代理方法
     * @param: ClassPool
     * @param: CtClass
     * @param: java bean class
     * @param: 被代理的方法
     */
    private void generateBeanToMapProxyMethod(ClassPool pool, CtClass cls, ClassWrapper beanClass, Method method) throws Exception {
        Class<?> returnType = method.getReturnType();
        String methodContent = generateBeanToMapMethodStub(method, beanClass);
        CtMethod ctMethod = CtNewMethod.make(pool.get(returnType.getName()), method.getName(), getParameterClass(pool, method), getExceptionClass(pool, method), methodContent, cls);
        cls.addMethod(ctMethod);
    }

    /**
     * @Title: generateBeanToMapMethodStub
     * @Description: 生成bean到map的方法
     * @param: 被代理的bean方法
     * @param: ClassWrapper
     * @return: String
     */
    private String generateBeanToMapMethodStub(Method method, ClassWrapper beanClass) {
        StringBuilder body = new StringBuilder();
        body.append("{\n");
        if (!Objects.equals(method.getName(), "get")
                && !Objects.equals(method.getName(), "entrySet")
                && !Objects.equals(method.getName(), "isEmpty")) {
            body.append("throw new IllegalStateException(\"unsupport operation\");\n");
            body.append("}\n");
            return body.toString();
        }
        if (Objects.equals(method.getName(), "get")) {
            beanClass.getSendFieldNameMap().forEach((fieldName, fieldWrapper) -> {
                body.append(String.format("if (Objects.equals($1, \"%s\")) {\n", fieldName));
                if (fieldWrapper.getFieldType().isPrimitive()) {
                    body.append(String.format("%s value = CastUtils.toWrap%sValue(this.bean.%s());\n",
                            TypeUtils.getPrimitiveClassWrapName(fieldWrapper.getFieldType()),
                            TypeUtils.getPrimitiveClassWrapName(fieldWrapper.getFieldType()),
                            fieldWrapper.getReadMethod().getName()));
                    body.append("return (Object) value;\n");
                } else {
                    body.append(String.format("return this.bean.%s();\n", fieldWrapper.getReadMethod().getName()));
                }
                body.append("}\n");
            });
            body.append("return null;\n");
        }
        if (Objects.equals(method.getName(), "entrySet")) {
            body.append("return entrySet;\n");
        }
        if (Objects.equals(method.getName(), "isEmpty")) {
            body.append("return entrySet.size() <= 0;\n");
        }
        body.append("}\n");
        return body.toString();
    }

    /**
     * @Title: generateLazyLoadProxy
     * @Description: 针对collection、map和jaavbean生成懒加载(直到调用其方法才实际进行初始化动作)代理
     * @param: 目标class
     * @param: supplier
     * @return: lazy load代理对象
     * @throws
     */
    public Object generateLazyLoadProxy(Class<?> targetClass, Supplier<?> supplier) {
        try {
            if (targetClass == null) {
                throw new IllegalStateException("proxy class can not be null");
            }
            if (supplier == null) {
                throw new IllegalStateException("supplier can not be null");
            }
            return lazyLoadProxyCache.computeIfAbsent(targetClass, (key) -> {
                try {
                    ClassPool pool = ClassPool.getDefault();
                    CtClass ctClass = this.generateLazyLoadProxyClass(pool, targetClass);
                    this.generateLazyLoadProxyFields(targetClass, pool, ctClass);
                    this.generateLazyLoadProxyConstructs(pool, targetClass, ctClass);
                    Method[] methods = targetClass.getDeclaredMethods();
                    for (Method method : methods) {
                        generateLazyLoadProxyMethod(pool, ctClass, targetClass, method);
                    }
                    Class<?> objCls = ctClass.toClass();
                    Constructor<?> constructor = objCls.getConstructor(Supplier.class);
                    return constructor.newInstance(supplier);
                } catch (Exception e) {
                    return null;
                }
            });
        } catch (Exception e) {
            return null;
        }
    }

    private CtClass generateLazyLoadProxyClass(ClassPool pool, Class<?> targetClass) throws Exception {
        CtClass targetCls = pool.get(targetClass.getName());
        CtClass cls = pool.makeClass(String.format("cn.com.hjack.autobind.resolver.%s$LazyLoadProxy", targetClass.getSimpleName()));
        if (targetClass.isInterface()) {
            cls.setInterfaces(new CtClass[] {targetCls});
        } else {
            cls.setSuperclass(targetCls);
        }
        return cls;
    }

    private void generateLazyLoadProxyFields(Class<?> targetClass, ClassPool pool, CtClass cls) throws Exception {
        CtField supplierField = new CtField(pool.get("java.util.function.Supplier"), "supplier", cls);
        supplierField.setModifiers(Modifier.PRIVATE);
        cls.addField(supplierField);
        CtField actualObjectField = new CtField(pool.get(TypeUtils.getCanonicalName(targetClass)), "object", cls);
        actualObjectField.setModifiers(Modifier.PRIVATE);
        cls.addField(actualObjectField);
        CtField lockField = CtField.make("private java.util.concurrent.locks.Lock lock = new java.util.concurrent.locks.ReentrantLock();", cls);
        cls.addField(lockField);
    }

    private void generateLazyLoadProxyConstructs(ClassPool pool, Class<?> targetClass, CtClass cls) throws Exception {
        CtConstructor constructor = new CtConstructor(new CtClass[] {pool.get("java.util.function.Supplier")}, cls);
        constructor.setBody(generateLazyLoadProxyConstructContentStub(targetClass));
        cls.addConstructor(constructor);
    }

    private String generateLazyLoadProxyConstructContentStub(Class<?> targetClass) {
        StringBuilder body = new StringBuilder();
        body.append("{\n");
        body.append("  if ($1 == null) {");
        body.append("    throw new IllegalStateException(\"supplier can not be null\");\n");
        body.append("  }");
        body.append("  this.supplier = $1;\n");
        body.append("}\n");
        return body.toString();
    }

    private void generateLazyLoadProxyMethod(ClassPool pool, CtClass cls, Class<?> targetClass, Method method) throws Exception {
        Class<?> returnType = method.getReturnType();
        String methodContent = generateLazyLoadMethodStub(targetClass, method);
        CtMethod ctMethod = CtNewMethod.make(pool.get(returnType.getName()), method.getName(), getParameterClass(pool, method), getExceptionClass(pool, method), methodContent, cls);
        cls.addMethod(ctMethod);
    }

    private CtClass[] getParameterClass(ClassPool pool, Method method) throws Exception {
        Parameter[] parameters = method.getParameters();
        CtClass[] parameterClass;
        if (parameters == null || parameters.length == 0) {
            parameterClass = new CtClass[0];
        } else {
            parameterClass = new CtClass[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                parameterClass[i] = pool.get(parameters[i].getType().getName());
            }
        }
        return parameterClass;
    }

    private CtClass[] getExceptionClass(ClassPool pool, Method method) throws Exception {
        CtClass[] exceptionClass;
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes.length == 0) {
            exceptionClass = new CtClass[0];
        } else {
            exceptionClass = new CtClass[exceptionTypes.length];
            for (int i = 0; i < exceptionTypes.length; i++) {
                exceptionClass[i] = pool.get(exceptionTypes[i].getName());
            }
        }
        return exceptionClass;
    }

    /**
     * @Title: generateLazyLoadMethodStub
     * @Description: 生成懒加载方法桩
     * @param: 目标class
     * @param: 被代理的方法
     * @return: String
     */
    private String generateLazyLoadMethodStub(Class<?> targetClass, Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append(" if (this.object == null) {\n");
        sb.append("   lock.lock();\n");
        sb.append("   if (this.object == null) {\n");
        sb.append("     try {\n");
        sb.append(String.format("         this.object = (%s) supplier.get();\n", TypeUtils.getCanonicalName(targetClass)));
        sb.append("     } finally {\n");
        sb.append("         lock.unlock();\n");
        sb.append("     }\n");
        sb.append("   }\n");
        sb.append(" }\n");
        String body;
        String returnTypeName = TypeUtils.getCanonicalName(method.getReturnType());
        if (Objects.equals(returnTypeName, "void")) {
            body = doGenerateLazyLoadMethodStub(method, false);
        } else {
            body = doGenerateLazyLoadMethodStub(method, true);
        }
        sb.append(body);
        sb.append("}");
        return sb.toString();
    }

    private String doGenerateLazyLoadMethodStub(Method method, boolean haveReturnValue) {
        StringBuilder body = new StringBuilder();
        Parameter[] parameters = method.getParameters();
        if (parameters == null || parameters.length == 0) {
            if (haveReturnValue) {
                body.append(" if (object != null) {\n");
                body.append("   return this.object.%s();\n");
                body.append(" } else {\n");
                body.append("   return null;\n");
                body.append(" }\n");
            } else {
                body.append(" if (object != null) {\n");
                body.append("   this.object.%s();\n");
                body.append(" }\n");
            }
            return String.format(body.toString(), method.getName());
        } else {
            if (haveReturnValue) {
                body.append(" if (object != null) {\n");
                body.append("   return this.object.%s(%s);\n");
                body.append(" } else {\n");
                body.append("   return null;\n");
                body.append(" }\n");
            } else {
                body.append(" if (object != null) {\n");
                body.append("   this.object.%s(%s);\n");
                body.append(" }\n");
            }
            String parameterStr = IntStream.range(0, parameters.length).mapToObj(i -> {
                return "$" + (i + 1);
            }).collect(Collectors.joining(","));
            return String.format(body.toString(), method.getName(), parameterStr);
        }
    }

    /**
     * @Title: generateResolver
     * @Description: 生成TypeValueResolver, 只为map、array和java bean生成对应的resolver
     * @param: sourceClass
     * @param: targetType
     * @return: TypeValueResolver
     */
    public TypeValueResolver generateResolver(Class<?> sourceClass, TypeWrapper targetType) {
        if (sourceClass == null || targetType == null || targetType.resolve() == null) {
            throw new IllegalStateException("source or target type can not be null");
        }
        Class<?> targetClass = targetType.resolveOrObject();
        if (TypeUtils.isMapClass(targetClass)) {
            TypeWrapper genericType = TypeWrappers.getAndResolveGenericType(targetType, 1);
            Class<?> genericCls = genericType.resolveOrObject();
            Map<Class<?>, TypeValueResolver> resovlerMap = mapResolverCache.computeIfAbsent(genericCls, (key) -> new ConcurrentHashMap<>());
            return resovlerMap.computeIfAbsent(sourceClass, (key) -> {
                try {
                    ClassPool pool = ClassPool.getDefault();
                    // 生成导入
                    generateImportsStub(pool);
                    // 生成类
                    CtClass ctClass = generateMapResolverClass(sourceClass, targetClass, pool);
                    ctClass.getClassFile().setMajorVersion(ClassFile.JAVA_8);
                    this.generateMapResolveMethodStub(ctClass, sourceClass, targetClass, genericType);
                    Class<?> resolverClass = ctClass.toClass();
                    return (TypeValueResolver) resolverClass.newInstance();
                } catch (Exception e) {
                    return null;
                }
            });
        } else if (TypeUtils.isJavaBeanClass(targetClass)) {
            Map<Class<?>, TypeValueResolver> resovlerMap = javaBeanResolverCache.computeIfAbsent(targetClass, (key) -> new ConcurrentHashMap<>());
            return resovlerMap.computeIfAbsent(sourceClass, (key) -> {
                try {
                    ClassPool pool = ClassPool.getDefault();
                    // 生成导入
                    generateImportsStub(pool);
                    // 生成类
                    CtClass ctClass = generateJavaBeanResolveClass(sourceClass, targetClass, pool);
                    ctClass.getClassFile().setMajorVersion(ClassFile.JAVA_8);
                    this.generateJavaBeanResolveMethodStub(ctClass, sourceClass, targetType);
                    Class<?> resolverClass = ctClass.toClass();
                    return (TypeValueResolver) resolverClass.newInstance();
                } catch (Exception e) {
                    return null;
                }
            });
        } else {
            Map<Class<?>, TypeValueResolver> resovlerMap = arrayResolverCache.computeIfAbsent(targetClass, (key) -> new ConcurrentHashMap<>());
            return resovlerMap.computeIfAbsent(sourceClass, (key) -> {
                try {
                    ClassPool pool = ClassPool.getDefault();
                    // 生成导入
                    generateImportsStub(pool);
                    // 生成类
                    CtClass ctClass = this.generateArrayResolverClass(sourceClass, targetClass, pool);
                    ctClass.getClassFile().setMajorVersion(ClassFile.JAVA_8);
                    this.generateArrayResolveMethodStub(ctClass, targetType);
                    Class<?> resolverClass = ctClass.toClass();
                    return (TypeValueResolver) resolverClass.newInstance();
                } catch (Exception e) {
                    return null;
                }
            });
        }

    }

    private CtClass generateArrayResolverClass(Class<?> sourceClass, Class<?> arrayClass, ClassPool pool) throws Exception {
        CtClass cls = pool.makeClass("cn.com.hjack.autobind.resolver.ArrayValueResolver#" + sourceClass.getName() + "#" + arrayClass.getName() + "#Proxy");
        cls.setSuperclass(pool.get("cn.com.hjack.autobind.resolver.AbstractTypeValueResolver"));
        return cls;
    }

    private void generateArrayResolveMethodStub(CtClass ctClass, TypeWrapper arrayType) throws Exception {
        CtMethod mapMethod = CtNewMethod.make(this.generateConvertObjectToArrayStub(arrayType), ctClass);
        ctClass.addMethod(mapMethod);
    }

    /**
     * @Title: generateConvertObjectToArrayStub
     * @Description: 生成转换数组代码桩
     * @param: 源值class
     * @param: 数组array type
     * @return: String
     */
    private String generateConvertObjectToArrayStub(TypeWrapper arrayType) {
        // 获取数组最终组件类型
        Class<?> compomentClass = TypeWrappers.getAndResolveComponentNonArrayType(arrayType).resolveOrObject();
        // 获取数组维度
        int dimension = TypeUtils.getArrayTypeDimension(arrayType.getType());
        StringBuilder body = new StringBuilder();
        body.append("public cn.com.hjack.autobind.Result doResolveValue(Object object, TypeWrapper targetType, ResolveConfig config) throws Exception { \n");
        body.append(String.format(SOURCE_NULL_CODE, TypeUtils.getArrayCanonicalName(compomentClass, dimension), TypeUtils.getArrayCanonicalName(compomentClass, dimension, "0")));
        body.append("List source = TypeUtils.arrayOrCollectionToList(object);\n");
        body.append("if (source.isEmpty()) { \n");
        body.append(String.format("%s target = new %s;\n", TypeUtils.getArrayCanonicalName(compomentClass, dimension), TypeUtils.getArrayCanonicalName(compomentClass, dimension, "0")));
        body.append("return DefaultResult.defaultSuccessResult(target); \n");
        body.append("} \n");
        body.append(String.format("%s target = new %s[source.size()]%s;\n", TypeUtils.getArrayCanonicalName(compomentClass, dimension), TypeUtils.getCanonicalName(compomentClass), TypeUtils.getArrayBracketDesc(dimension - 1, "")));
        body.append("DefaultResult result = new DefaultResult(); \n");
        body.append("for (int i = 0; i < source.size(); ++i) { \n");
        body.append("Object childSource = source.get(i); \n");
        body.append("if (childSource == null) { continue; }\n");
        TypeWrapper componentType = arrayType.getComponentType();
        Class<?> componentTypeClass = componentType.resolveOrObject();
        body.append("TypeWrapper componentType = targetType.getComponentType(); \n");
        body.append(String.format(GET_RESOLVER_CODE, TypeUtils.getCanonicalNameWithSuffix(componentTypeClass)));
        body.append("if (resolver != null) { \n");
        body.append(String.format(CALL_RESOLVE_METHOD_CODE, "childSource", "componentType", "config"));
        body.append("if (!childResult.success()) { \n" );
        // 如果组件类型是primitive type则特殊处理
        if (compomentClass.isPrimitive()) {
            if (dimension == 1) {
                body.append(String.format("%s value = %s", TypeUtils.getPrimitiveClassWrapName(compomentClass), String.format(GET_INSTANCE_CODE, TypeUtils.getPrimitiveClassWrapName(compomentClass))));
                body.append(String.format("target[i] = %s", String.format(CastUtils_GET_PRIM_CODE, TypeUtils.getPrimitiveClassWrapName(compomentClass), "value")));
            } else {
                body.append(String.format("target[i] = %s", String.format(GET_INSTANCE_CODE, TypeUtils.getArrayCanonicalName(compomentClass, dimension - 1, ""))));
            }
        } else {
            body.append(String.format("target[i] = %s", String.format(GET_INSTANCE_CODE, TypeUtils.getArrayCanonicalName(compomentClass, dimension - 1, ""))));
        }
        body.append(generateErrorResultStub(TypeUtils.getCanonicalNameWithSuffix(componentTypeClass), this.generateGetSourceClassName(null, "childSource")));
        body.append("} else { \n" );
        if (compomentClass.isPrimitive()) {
            if (dimension == 1) {
                body.append(String.format("%s value = %s", TypeUtils.getPrimitiveClassWrapName(compomentClass), String.format(GET_INSTANCE_CODE, TypeUtils.getPrimitiveClassWrapName(compomentClass))));
                body.append(String.format("target[i] = %s", String.format(CastUtils_GET_PRIM_CODE, TypeUtils.getPrimitiveClassWrapName(compomentClass), "value")));
            } else {
                body.append(String.format("target[i] = %s", String.format(GET_INSTANCE_CODE, TypeUtils.getArrayCanonicalName(compomentClass, dimension - 1, ""))));
            }
        } else {
            body.append(String.format("target[i] = %s", String.format(GET_INSTANCE_CODE, TypeUtils.getArrayCanonicalName(compomentClass, dimension - 1, ""))));
        }
        body.append("} \n");
        body.append("} \n");
        body.append("} \n");
        body.append("result.setInstance(target); \n");
        body.append("return result; \n");
        body.append("} \n");
        return body.toString();
    }

    private String generateErrorResultStub(String targetClassName, String sourceClassName) {
        StringBuilder body = new StringBuilder();
        body.append("result.setSuccess(false); \n");
        body.append(String.format("result.setResultCode(\"%s\");\n", Constants.FAIL_CODE));
        body.append(String.format("result.setResultMsg(\"can not convert source type %s to target type %s\");\n", sourceClassName, targetClassName));
        return body.toString();
    }

    private CtClass generateMapResolverClass(Class<?> sourceClass, Class<?> targetClass, ClassPool pool) throws Exception {
        CtClass cls = pool.makeClass(String.format("cn.com.hjack.autobind.resolver.MapValueResolver#%s#%s#Proxy", sourceClass.getName(), targetClass.getName()));
        cls.setSuperclass(pool.get("cn.com.hjack.autobind.resolver.AbstractTypeValueResolver"));
        return cls;
    }

    private void generateMapResolveMethodStub(CtClass ctClass, Class<?> sourceType, Class<?> targetCls, TypeWrapper actualGenericType) throws Exception {
        CtMethod mapMethod = CtNewMethod.make(this.generateConvertBeanToMapStub(ctClass, sourceType, targetCls, actualGenericType), ctClass);
        ctClass.addMethod(mapMethod);
    }

    /**
     * @Title: generateConvertBeanToMapStub
     * @Description: 生成java bean到map的代码桩
     * @param: ctClass
     * @param: 源值类型
     * @param: 目标值类型
     * @param: map value 泛型类型
     * @return: String
     */
    private String generateConvertBeanToMapStub(CtClass ctClass, Class<?> sourceClass, Class<?> targetClass, TypeWrapper targetGenericType) {
        StringBuilder body = new StringBuilder();
        body.append("public cn.com.hjack.autobind.Result doResolveValue(Object object, TypeWrapper targetType, ResolveConfig config) throws Exception { \n");
        body.append(String.format("Map target = TypeUtils.createMap(%s);\n", TypeUtils.getCanonicalNameWithSuffix(targetClass)));
        body.append("if (object == null) { return DefaultResult.defaultSuccessResult(target); }\n");
        ClassWrapper sourceClassWrapper = ClassWrapper.forClass(sourceClass);
        Map<String, FieldWrapper> sourceFieldNames = sourceClassWrapper.getSendFieldNameMap();
        if (MapUtils.isEmpty(sourceFieldNames)) {
            body.append("return DefaultResult.defaultSuccessResult(target); \n");
            body.append("} \n");
            return body.toString();
        }
        body.append("DefaultResult result = new DefaultResult(); \n");
        body.append(String.format(CONVERT_SOURCE_CODE, TypeUtils.getCanonicalName(sourceClass), TypeUtils.getCanonicalName(sourceClass)));
        Class<?> genericClass = targetGenericType.resolve();
        sourceFieldNames.forEach((sendFieldName, field) -> {
            AutoBindField targetAutobind = field.getAutoBind();
            if (targetAutobind != null && targetAutobind.exclude()) {
                return;
            }
            Method readMethod = field.getReadMethod();
            if (readMethod == null) {
                throw new IllegalStateException("can not find read method");
            }
            String fieldVarName = generateDefFieldValueVarStub(field);
            FieldWrapper.FieldChainNode invokeNode = sourceClassWrapper.findFieldChainByFieldName(sendFieldName, new HashMap<>());
            String findValueCode = generateFindJavaBeanFieldValueMethodStub(sourceClass, invokeNode, null, sendFieldName);
            if (Strings.isNullOrEmpty(findValueCode)) {
                return;
            }
            try {
                CtMethod getValueMethod = CtNewMethod.make(findValueCode, ctClass);
                ctClass.addMethod(getValueMethod);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            body.append(String.format(GET_SOURCE_VALUE_CODE, fieldVarName, sendFieldName));
            body.append(String.format("if (%s != null) {\n", fieldVarName));
            if (genericClass == Object.class) {
                body.append(String.format("target.put(\"%s\", %s);\n", sendFieldName, fieldVarName));
            } else {
                body.append(generateConvertFieldToEntryStub(fieldVarName, targetGenericType));
                body.append("if (childResult.success()) { \n");
                body.append(String.format("target.put(\"%s\", %s);\n", sendFieldName, "childResult.instance()"));
                body.append("} else { \n");
                body.append("result.setSuccess(false); \n");
                body.append(String.format("result.setResultCode(\"%s\");\n", Constants.FAIL_CODE));
                body.append("result.setResultMsg(childResult.resultMsg()); \n");
                body.append(String.format("target.put(\"%s\", %s);\n", sendFieldName, "childResult.instance()"));
                body.append("} \n");
                body.append("}\n");
            }
            body.append("} \n");
        });
        body.append("result.setInstance(target); \n");
        body.append("return result; \n");
        body.append("} \n");
        return body.toString();
    }

    /**
     * @Title: generateConvertFieldToEntry
     * @Description: 将java bean字段转为map entry
     * @param: 源值变量名称
     * @param: map entry value类型
     * @return: String
     */
    private String generateConvertFieldToEntryStub(String sourceValueVarName, TypeWrapper mapEntryValueType) {
        StringBuilder body = new StringBuilder();
        Class<?> mapEntryValueClass = mapEntryValueType.resolve();
        body.append("TypeWrapper valueType = TypeWrappers.getAndResolveGenericType(targetType, 1); \n");
        body.append(String.format(GET_RESOLVER_CODE, TypeUtils.getCanonicalNameWithSuffix(mapEntryValueClass)));
        body.append("if (resolver != null) {\n");
        body.append(String.format(CALL_RESOLVE_METHOD_CODE, sourceValueVarName, "valueType", "config"));
        return body.toString();
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
    private String generateFindJavaBeanFieldValueMethodStub(Class<?> sourceClass, FieldWrapper.FieldChainNode sourceChainNode, Class<?> targetFieldClass, String targetFieldName) {
        StringBuilder body = new StringBuilder();
        if (sourceChainNode.getCurrent() == null) { // 如果chain node为空,且目标字段为java bean，则将source java bean转为map返回
            if (TypeUtils.isJavaBeanClass(targetFieldClass)) {
                body.append(String.format("private Object %sValue(%s source) {\n", targetFieldName, TypeUtils.getCanonicalName(sourceClass)));
                if (TypeUtils.isJavaBeanClass(sourceClass)) {
                    body.append(String.format("return BeanMapper.beanToMap(%s, %s);\n", TypeUtils.getCanonicalNameWithSuffix(sourceClass), "source"));
                } else {
                    body.append("return source;\n");
                }
                body.append("}");
                return body.toString();
            } else {
                return "";
            }
        } else {
            AtomicInteger count = new AtomicInteger(0);
            FieldWrapper field = sourceChainNode.getCurrent();
            if (field.getReadMethod() == null || field.getField() == null) {
                throw new IllegalStateException("bean has no field or read method");
            }
            body.append(String.format("private Object %sValue(%s source) {\n", targetFieldName, TypeUtils.getCanonicalName(sourceClass)));
            if (field.getFieldType().isPrimitive()) {
                String primitiveTypeName = TypeUtils.getPrimitiveClassWrapName(field.getFieldType());
                body.append("Object value = " + String.format(CastUtils_GET_WRAP_CODE, primitiveTypeName, String.format(GET_VALUE_CODE, field.getReadMethod().getName())));
            } else {
                body.append("Object value = null; \n");
                body.append(String.format("if (source.%s() != null) {\n", field.getReadMethod().getName()));
                body.append(String.format("%s %s_%s = %s;\n", TypeUtils.getCanonicalName(field.getField().getType()), targetFieldName, count.incrementAndGet(), String.format(GET_VALUE_CODE, field.getReadMethod().getName())));
                body.append(generateFindJavaBeanFieldValueBodyStub(sourceChainNode.getNext(), targetFieldName, count));
                body.append("} \n");
            }
            FieldWrapper leafNode = sourceChainNode.getLeaf();
            AutoBindField autoBindField = leafNode.getAutoBind();
            if (autoBindField != null) {
                if (!Strings.isNullOrEmpty(autoBindField.defaultValue())) {
                    body.append(String.format("value = TypeUtils.getOrDefaultValue(value, \"%s\");\n", autoBindField.defaultValue()));
                }
                if (!Strings.isNullOrEmpty(autoBindField.format())) {
                    body.append(String.format("value = CastUtils.formatDate(value, \"%s\");", autoBindField.format()));
                }
                if (autoBindField.scale() > 0) {
                    body.append(String.format("value = CastUtils.setNumberScale(value, %s, %s);\n", autoBindField.scale(), CastUtils.getRoundingModeStr(autoBindField.roundingMode())));
                }
            }
            body.append("return value; \n");
            body.append("} \n");
            return body.toString();
        }
    }

    private String generateFindJavaBeanFieldValueBodyStub(FieldWrapper.FieldChainNode chainNode, String targetFieldName, AtomicInteger count) {
        StringBuilder body = new StringBuilder();
        int countValue = count.intValue();
        if (chainNode == null) {
            body.append(String.format("value = %s_%s;\n", targetFieldName, countValue));
            return body.toString();
        }
        FieldWrapper field = chainNode.getCurrent();
        body.append(String.format("if (%s_%s != null) {\n", targetFieldName, countValue));
        if (field.getFieldType().isPrimitive()) {
            String primitiveTypeName = TypeUtils.getPrimitiveClassWrapName(field.getField().getType());
            body.append(String.format("%s %s_%s = CastUtils.toWrap%sValue(%s_%s.%s());\n", primitiveTypeName, targetFieldName, count.incrementAndGet(), primitiveTypeName, targetFieldName, countValue, field.getReadMethod().getName()));
        } else {
            body.append(String.format("%s %s = %s.%s();\n", TypeUtils.getCanonicalName(field.getField().getType()), targetFieldName + count.incrementAndGet(), targetFieldName + countValue, field.getReadMethod().getName()));
            body.append(String.format("%s %s_%s = %s_%s.%s();\n", TypeUtils.getCanonicalName(field.getField().getType()), targetFieldName, count.incrementAndGet(), targetFieldName, countValue, field.getReadMethod().getName()));
        }
        body.append(generateFindJavaBeanFieldValueBodyStub(chainNode.getNext(), targetFieldName, count));
        body.append("} \n");
        return body.toString();
    }

    private CtClass generateJavaBeanResolveClass(Class<?> sourceClass, Class<?> targetClass, ClassPool pool) throws Exception {
        CtClass cls = pool.makeClass("cn.com.hjack.autobind.resolver.JavaBeanValueResolver#" + sourceClass.getName() + "#" + targetClass.getName() + "#Proxy");
        cls.setSuperclass(pool.get("cn.com.hjack.autobind.resolver.AbstractTypeValueResolver"));
        return cls;
    }

    private void generateJavaBeanResolveMethodStub(CtClass ctClass, Class<?> sourceType, TypeWrapper targetType) throws Exception {
        String methodBody;
        if (TypeUtils.isMapClass(sourceType)) {
            methodBody = generateConvertMapToBeanStub(targetType);
        } else {
            methodBody = generateConvertBeanToBeanStub(ctClass, sourceType, targetType);
        }
        CtMethod resolveMethod = CtNewMethod.make(methodBody, ctClass);
        ctClass.addMethod(resolveMethod);
    }

    /**
     * @Title: generateConvertBeanToBeanStub
     * @Description: 生成将bean转换为bean代码桩
     * @param: CtClass
     * @param: sourceClass
     * @param: TypeWrapper
     * @return: String
     */
    private String generateConvertBeanToBeanStub(CtClass ctClass, Class<?> sourceClass, TypeWrapper targetType) {
        StringBuilder body = new StringBuilder();
        body.append("public Result doResolveValue(Object object, TypeWrapper targetType, ResolveConfig config) throws Exception { \n");
        body.append("if (object == null) { return DefaultResult.defaultSuccessResult(null); }\n");
        Class<?> targetClass = targetType.resolve();
        ClassWrapper targetClassWrapper = ClassWrapper.forClass(targetClass);
        if (targetClassWrapper == null) {
            throw new IllegalStateException("can not find class");
        }
        body.append(String.format(NEW_TARGET_CODE, TypeUtils.getCanonicalName(targetClass), TypeUtils.getCanonicalName(targetClass), "()"));
        Map<String, FieldWrapper> targetFields = targetClassWrapper.getFieldNameMap();
        if (MapUtils.isEmpty(targetFields)) {
            body.append("return DefaultResult.defaultSuccessResult(target); \n");
            body.append("} \n");
            return body.toString();
        }
        body.append(String.format(CONVERT_SOURCE_CODE, TypeUtils.getCanonicalName(sourceClass), TypeUtils.getCanonicalName(sourceClass)));
        body.append(generateVarInitStub());
        ClassWrapper sourceClassWrapper = ClassWrapper.forClass(sourceClass);
        targetFields.forEach((fieldName, targetField) -> {
            AutoBindField targetAutobind = targetField.getAutoBind();
            if (targetAutobind != null && targetAutobind.exclude()) {
                return;
            }
            String varName = generateDefFieldValueVarStub(targetField);
            // 找到与目标字段名称对应的源字段调用链如(targetField: sourceField->sourceField1->targetField)
            FieldWrapper.FieldChainNode chainNode = sourceClassWrapper.findFieldChainByFieldName(targetField.getRecvFieldName(), new HashMap<>());
            String getFieldValueMethodBody = generateFindJavaBeanFieldValueMethodStub(sourceClass, chainNode, targetField.getFieldType(), targetField.getFieldNmae());
            try {
                CtMethod getValueMethod = CtNewMethod.make(getFieldValueMethodBody, ctClass);
                ctClass.addMethod(getValueMethod);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            body.append(String.format(GET_SOURCE_VALUE_CODE, varName, targetField.getFieldNmae()));
            body.append(generateConvertFieldOrEntryToFieldStub(varName, targetClass, targetField));
        });
        body.append(generateGlobalValidateStub());
        return body.toString();
    }

    private String generateGlobalValidateStub() {
        StringBuilder body = new StringBuilder();
        body.append("if (config.validator() != null) {\n");
        body.append("DefaultValidationErrors errors = new DefaultValidationErrors();\n");
        body.append("config.validator().validate(target, errors);\n");
        body.append("List errorMsgs = new ArrayList(errors.getErrorMsgs());\n");
        body.append("if (errorMsgs != null && errorMsgs.size() > 0) {\n");
        body.append("result.setResultMsg(String.valueOf(errorMsgs.get(0)));\n");
        body.append("result.setInstance(target); \n");
        body.append("return result; \n");
        body.append("} else {\n");
        body.append("result.setInstance(target); \n");
        body.append("return result; \n");
        body.append("} \n");
        body.append("} else {\n");
        body.append("result.setInstance(target); \n");
        body.append("return result; \n");
        body.append("}}\n");
        return body.toString();
    }

    /**
     * @Title: generateConvertMapToBeanStub
     * @Description: 生成将map转化为bean的代码桩
     * @param: CtClass
     * @param: TypeWrapper
     * @return: String
     */
    private String generateConvertMapToBeanStub(TypeWrapper typeWrapper) {
        StringBuilder body = new StringBuilder();
        body.append("public Result doResolveValue(Object object, TypeWrapper targetType, ResolveConfig config) throws Exception { \n");
        body.append("if (object == null) { return DefaultResult.defaultSuccessResult(null); }\n");
        Class<?> targetClass = typeWrapper.resolve();
        ClassWrapper targetClassWrapper = ClassWrapper.forClass(targetClass);
        if (targetClassWrapper == null) {
            throw new IllegalStateException("can not find class");
        }
        body.append(String.format("%s target = new %s();\n", TypeUtils.getCanonicalName(targetClass), TypeUtils.getCanonicalName(targetClass)));
        Map<String, FieldWrapper> targetFieldNames = targetClassWrapper.getFieldNameMap();
        if (MapUtils.isEmpty(targetFieldNames)) {
            return body.toString();
        }
        body.append("Map source = (Map) object; \n");
        body.append(generateVarInitStub());
        targetFieldNames.forEach((fieldName, field) -> {
            AutoBindField autoBind = field.getAutoBind();
            if (autoBind != null && autoBind.exclude()) {
                return;
            }
            String defaultValue = "";
            if (autoBind != null) {
                defaultValue = autoBind.defaultValue();
            }
            String varName = generateDefFieldValueVarStub(field);
            body.append(String.format("Object %s = (Object) BeanMapper.getValueOrDefault(source, \"%s\", %s, \"%s\");\n", varName, field.getRecvFieldName(), TypeUtils.getCanonicalNameWithSuffix(field.getFieldType()), defaultValue));
            body.append(generateConvertFieldOrEntryToFieldStub(varName, targetClass, field));
        });
        body.append(generateGlobalValidateStub());
        return body.toString();
    }

    /**
     * @Title: generateConvertFieldOrEntryToField
     * @Description: 将javabean字段或map entry转为java bean字段
     * @param: 源值变量名称
     * @param: 源字段类型
     * @param: 目标类型(java bean class or map)
     * @param: 目标字段type类型
     * @param: 目标字段
     * @return: 代码桩
     * @throws
     */
    private String generateConvertFieldOrEntryToFieldStub(String sourceValueVarName, Class<?> targetClass, FieldWrapper targetField) {
        StringBuilder body = new StringBuilder();
        body.append(String.format("if (%s != null) {\n", sourceValueVarName));
        body.append(generateDoConvertFieldOrEntryToFieldStub(sourceValueVarName, targetClass, targetField));
        FieldWrapper.FieldChainNode invokeNode = ClassWrapper.forClass(targetClass).findFieldChainByFieldName(targetField.getFieldNmae(), new HashMap<>());
        body.append(genreateFieldTypeAndVarAssignStub(targetField));
        body.append(String.format(SET_VALUE_CODE, targetField.getWriteMethod().getName(), generateDefFieldValueVarStub(targetField)));
        body.append("if (childResult.success()) {\n");
        // el表达式校验
        body.append(generateELValidateAndConvertStub(targetField, invokeNode));
        body.append("} else { \n");
        body.append("result.setSuccess(false); \n");
        body.append(String.format("result.setResultCode(\"%s\");\n", Constants.FAIL_CODE));
        body.append(String.format("result.setResultMsg(\"%s.\" + %s);\n", targetField.getFieldNmae(), "childResult.resultMsg()"));
        body.append("}\n");
        body.append("}\n");
        body.append("} \n");
        return body.toString();
    }

    private String generateDoConvertFieldOrEntryToFieldStub(String sourceValueVarName, Class<?> targetClass, FieldWrapper targetField) {
        StringBuilder body = new StringBuilder();
        body.append(String.format(FIND_FIELD_CODE, TypeUtils.getCanonicalNameWithSuffix(targetClass), targetField.getFieldNmae()));
        body.append(String.format(GET_FIELD_TYPE_CODE, "field", TypeUtils.getCanonicalNameWithSuffix(targetClass), "targetType"));
        body.append("TypeValueResolver resolver = TypeValueResolvers.getResolver(fieldType);\n");
        body.append("if (resolver != null) { \n");
        body.append(this.generateCopyResolveConfigStub(targetField.getAutoBind()));
        body.append(String.format(CALL_RESOLVE_METHOD_CODE, sourceValueVarName, "fieldType", "resolveConfig"));
        return body.toString();
    }

    /**
     * @Title: generateCopyResolveConfigStub
     * @Description: 拷贝resolve config
     * @param: AutoBindField
     * @return: String
     */
    private String generateCopyResolveConfigStub(AutoBindField autoBind) {
        StringBuilder body = new StringBuilder();
        if (autoBind != null) {
            body.append("ResolveConfig resolveConfig;\n");
            body.append("if (config == null) {\n");
            body.append("resolveConfig = ResolveConfig.defaultConfig.clone();\n");
            body.append("} else {\n");
            body.append("resolveConfig = config.clone();\n");
            body.append("}\n");
            body.append(String.format("resolveConfig.format(\"%s\");\n", autoBind.format()));
            body.append(String.format("resolveConfig.scale(%s);\n", autoBind.scale()));
            body.append(String.format("resolveConfig.roundingMode(%s);\n", CastUtils.getRoundingModeStr(autoBind.roundingMode())));
            body.append(String.format("resolveConfig.defaultValue(\"%s\");\n", autoBind.defaultValue()));
        } else {
            body.append("ResolveConfig resolveConfig = ResolveConfig.copy(config, null);\n");
        }
        return body.toString();
    }

    private String genreateFieldTypeAndVarAssignStub(FieldWrapper field) {
        StringBuilder body = new StringBuilder();
        if (field.getFieldType().isPrimitive()) {
            String fieldClassName = TypeUtils.getPrimitiveClassWrapName(field.getFieldType());
            String typeAndVarName = this.generateGetTypeAndVarStr(TypeUtils.getCanonicalName(field.getFieldType()), generateDefFieldValueVarStub(field));
            body.append(String.format("%s = %s", typeAndVarName, String.format(CastUtils_GET_PRIM_CODE, fieldClassName, String.format("(%s) %s", fieldClassName, "childResult.instance()"))));
        } else {
            String fieldClassName = TypeUtils.getCanonicalName(field.getFieldType());
            String typeAndVarName = this.generateGetTypeAndVarStr(fieldClassName, generateDefFieldValueVarStub(field));
            body.append(String.format("%s = %s;\n", typeAndVarName, String.format("(%s) %s", fieldClassName, "childResult.instance()")));
        }
        return body.toString();
    }

    private String generateGetTypeAndVarStr(String typeName, String varName) {
        return typeName + " " + varName;
    }

    private String generateGetSourceClassName(Class<?> sourceClass, String varName) {
        if (sourceClass != null) {
            return TypeUtils.getCanonicalNameWithSuffix(sourceClass);
        } else {
            return varName + ".getClass()";
        }
    }

    private String generateDefFieldValueVarStub(FieldWrapper fieldWrapper) {
        return fieldWrapper.getFieldNmae() + "Value";
    }

    private String generateVarInitStub() {
        StringBuilder body = new StringBuilder();
        body.append("DefaultResult result = new DefaultResult(); \n");
        body.append("EvaluationContext context = new StandardEvaluationContext(target); \n");
        body.append("ExpressionParser parser = new SpelExpressionParser(); \n");
        return body.toString();
    }

    /**
     * @Title: generateValidateAndConvertStub
     * @Description: 生成EL表达式校验代码桩
     * @param: 目标字段
     * @param: 字段调用链
     * @return: EL表达式校验代码桩
     */
    private String generateELValidateAndConvertStub(FieldWrapper targetField, FieldWrapper.FieldChainNode chainNode) {
        if (targetField.getAutoBind() == null
                || !ConvertFeature.isEnabled(targetField.getAutoBind().features(), ConvertFeature.EL_VALIDATE_ENABLE)) {
            return "";
        }
        StringBuilder body = new StringBuilder();
        if (targetField.getAutoBind() != null && !Strings.isNullOrEmpty(targetField.getAutoBind().condition())) {
            String condition = targetField.getAutoBind().condition();
            if (!Strings.isNullOrEmpty(targetField.getAutoBind().errorMsg())) {
                body.append(String.format(PARSE_EXPRESSION_CODE, condition, String.format(SET_ERROR_RESULT_CODE, Constants.FAIL_CODE, chainNode.getFieldInvokeDesc() + "." + targetField.getAutoBind().errorMsg())));
            } else {
                body.append(String.format(PARSE_EXPRESSION_CODE, condition, String.format(SET_ERROR_RESULT_CODE, Constants.FAIL_CODE, chainNode.getFieldInvokeDesc() + ".bind error")));
            }
        }
        return body.toString();
    }

    private void generateImportsStub(ClassPool pool) {
        pool.importPackage("cn.com.hjack.autobind.FieldTypeWrapper");
        pool.importPackage("cn.com.hjack.autobind.TypeValueResolver");
        pool.importPackage("cn.com.hjack.autobind.TypeWrapper");
        pool.importPackage("cn.com.hjack.autobind.ResolveConfig");
        pool.importPackage("cn.com.hjack.autobind.utils.ClassWrapper");
        pool.importPackage("cn.com.hjack.autobind.utils.FieldWrapper");
        pool.importPackage("cn.com.hjack.autobind.Result");
        pool.importPackage("cn.com.hjack.autobind.utils.TypeUtils");
        pool.importPackage("cn.com.hjack.autobind.utils.CastUtils");
        pool.importPackage("cn.com.hjack.autobind.resolver.MapValueResolver");
        pool.importPackage("cn.com.hjack.autobind.resolver.JavaBeanValueResolver");
        pool.importPackage("cn.com.hjack.autobind.resolver.ArrayValueResolver");
        pool.importPackage("cn.com.hjack.autobind.resolver.VariableValueResolver");
        pool.importPackage("cn.com.hjack.autobind.resolver.AbstractTypeValueResolver");
        pool.importPackage("cn.com.hjack.autobind.generator.ObjectGenerator");
        pool.importPackage("cn.com.hjack.autobind.factory.TypeWrappers");
        pool.importPackage("cn.com.hjack.autobind.factory.TypeValueResolvers");
        pool.importPackage("cn.com.hjack.autobind.validation.DefaultResult");
        pool.importPackage("cn.com.hjack.autobind.validation.DefaultValidationErrors");
        pool.importPackage("cn.com.hjack.autobind.mapper.BeanMapper");
        pool.importPackage("cn.com.hjack.autobind.type.TypeWrapperImpl");
        pool.importPackage("java.util.Map");
        pool.importPackage("java.util.Arrays");
        pool.importPackage("java.util.HashMap");
        pool.importPackage("java.util.Objects");
        pool.importPackage("java.util.Set");
        pool.importPackage("java.util.List");
        pool.importPackage("java.util.ArrayList");
        pool.importPackage("java.util.Collection");
        pool.importPackage("java.util.Date");
        pool.importPackage("java.lang.reflect.Field");
        pool.importPackage("java.text.SimpleDateFormat");
        pool.importPackage("java.math.BigDecimal");
        pool.importPackage("org.springframework.util.StringUtils");
        pool.importPackage("org.springframework.expression.EvaluationContext");
        pool.importPackage("org.springframework.expression.Expression");
        pool.importPackage("org.springframework.expression.ExpressionParser");
        pool.importPackage("org.springframework.expression.spel.standard.SpelExpressionParser");
        pool.importPackage("org.springframework.expression.spel.support.StandardEvaluationContext");
    }

    public static class MapProxyEntry implements Map.Entry<String, Object> {

        private String key;

        private Object value;

        public MapProxyEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            return this.value = value;
        }

    }

}

