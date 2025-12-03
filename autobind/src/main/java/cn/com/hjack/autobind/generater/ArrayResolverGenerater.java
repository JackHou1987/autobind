package cn.com.hjack.autobind.generater;

import cn.com.hjack.autobind.Generater;
import cn.com.hjack.autobind.ResolvableConverter;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.bytecode.ClassFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;




/**
 *   数组resolver生成器
 * @author houqq
 * @date: 2025年10月31日
 *
 */
public class ArrayResolverGenerater implements Generater<ResolvableConverter> {

    private static final String GET_RESOLVER_CODE = "ResolvableConverter resolver = ResolvableConverters.getConverter(%s);";

    private static final String CALL_RESOLVE_METHOD_CODE = "Result childResult = resolver.convert(%s, %s, %s);";

    private static final String CASTUTILS_GET_PRIM_CODE = "CastUtils.toPrim%sValue(%s);";

    private static final String GET_INSTANCE_CODE = "(%s) childResult.instance();";

    private static final String SOURCE_NULL_CODE = "if (object == null) { %s target = new %s; return DefaultResult.successResult(target); }";

    // outerkey -> target array class, innerkey -> source class(缓存source为object的array resolver缓存)
    private static Map<Class<?>, Map<Class<?>, ResolvableConverter>> arrayResolverCache = new ConcurrentHashMap<>();

    private Class<?> sourceClass;

    private TypeWrapper targetType;

    public ArrayResolverGenerater(Class<?> sourceClass, TypeWrapper targetType) {
        if (sourceClass == null || targetType == null || targetType.resolve() == null) {
            throw new IllegalStateException("source or target type can not be null");
        }
        this.sourceClass = sourceClass;
        this.targetType = targetType;
    }

    @Override
    public ResolvableConverter generate() {
        Class<?> targetClass = targetType.resolveOrObject();
        Map<Class<?>, ResolvableConverter> resovlerMap = arrayResolverCache.computeIfAbsent(targetClass, (key) -> {
            return new ConcurrentHashMap<>();
        });
        return resovlerMap.computeIfAbsent(sourceClass, (key) -> {
            try {
                ClassPool pool = ClassPool.getDefault();
                // 生成导入
                generateImportsStub(pool);
                // 生成类
                CtClass ctClass = this.generateArrayResolverClass(pool, sourceClass, targetClass);
                ctClass.getClassFile().setMajorVersion(ClassFile.JAVA_8);
                this.generateArrayResolveMethodStub(ctClass, sourceClass, targetType);
                Class<?> resolverClass = ctClass.toClass();
                return (ResolvableConverter) resolverClass.newInstance();
            } catch (Exception e) {
                return null;
            }
        });
    }

    private CtClass generateArrayResolverClass(ClassPool pool, Class<?> sourceClass, Class<?> arrayClass) throws Exception {
        CtClass cls = pool.makeClass(CastUtils.formatWithNoNewLine("cn.com.hjack.autobind.converter.ArrayValueResolver%s$%s$Proxy", sourceClass.getName(), arrayClass.getName()));
        cls.setSuperclass(pool.get("cn.com.hjack.autobind.converter.AbstractResolvableConverter"));
        return cls;
    }
    private void generateArrayResolveMethodStub(CtClass ctClass, Class<?> sourceClass, TypeWrapper arrayType) throws Exception {
        ctClass.addMethod(CtNewMethod.make(generateObjectToArrayStub(sourceClass, arrayType), ctClass));
    }

    /**
     * @Title: generateConvertObjectToArrayStub
     * @Description: 生成转换数组代码桩
     * @param: 源值class
     * @param: 数组array type
     * @return: String
     */
    private String generateObjectToArrayStub(Class<?> sourceClass, TypeWrapper arrayType) {
        // 获取数组最终组件类型
        Class<?> compomentClass = TypeWrappers.getAndResolveComponentNonArrayType(arrayType).resolveOrObject();
        // 获取数组维度
        int dimension = TypeUtils.getArrayTypeDimension(arrayType.getType());
        StringBuilder body = new StringBuilder();
        body.append(CastUtils.format("public Result doConvert(Object object, TypeWrapper targetType, ResolveConfig config) {"));
        body.append(CastUtils.formatAndIndent2(SOURCE_NULL_CODE, TypeUtils.getArrayCanonicalName(compomentClass, dimension), TypeUtils.getArrayCanonicalName(compomentClass, dimension, "0")));
        body.append(CastUtils.formatAndIndent2("List source = TypeUtils.arrayOrCollectionToList(object);"));
        body.append(CastUtils.formatAndIndent2("if (source.isEmpty()) {"));
        body.append(CastUtils.formatAndIndent4("%s target = new %s;", TypeUtils.getArrayCanonicalName(compomentClass, dimension), TypeUtils.getArrayCanonicalName(compomentClass, dimension, "0")));
        body.append(CastUtils.formatAndIndent4("return DefaultResult.successResult(target);"));
        body.append(CastUtils.formatAndIndent2("}"));
        TypeWrapper componentType = arrayType.getComponentType();
        Class<?> componentTypeClass = componentType.resolveOrObject();
        body.append(CastUtils.formatAndIndent2(GET_RESOLVER_CODE, TypeUtils.getCanonicalNameWithSuffix(componentTypeClass)));
        body.append(CastUtils.formatAndIndent2("if (resolver == null) {"));
        body.append(CastUtils.formatAndIndent4("return DefaultResult.errorResult(\"can not convert source to target, converter not found\");"));
        body.append(CastUtils.formatAndIndent2("}"));
        body.append(CastUtils.formatAndIndent2("%s target = new %s[source.size()]%s;", TypeUtils.getArrayCanonicalName(compomentClass, dimension), TypeUtils.getCanonicalName(compomentClass), TypeUtils.getArrayBracketDesc(dimension - 1, "")));
        body.append(CastUtils.formatAndIndent2("DefaultResult result = new DefaultResult();"));
        body.append(CastUtils.formatAndIndent2("TypeWrapper componentType = targetType.getComponentType();"));
        body.append(CastUtils.formatAndIndent2("for (int i = 0; i < source.size(); ++i) {"));
        body.append(CastUtils.formatAndIndent4("Object childSource = source.get(i);"));
        body.append(CastUtils.formatAndIndent4("if (childSource == null) { continue; }"));
        body.append(CastUtils.formatAndIndent6(CALL_RESOLVE_METHOD_CODE, "childSource", "componentType", "config"));
        body.append(CastUtils.formatAndIndent6("if (!childResult.success()) {"));
        // 如果组件类型是primitive type则特殊处理
        body.append(CastUtils.formatAndIndent8(generateErrorResultStub(compomentClass, dimension)));
        body.append(CastUtils.formatAndIndent6("} else {"));
        if (compomentClass.isPrimitive()) {
            if (dimension == 1) {
                body.append(CastUtils.formatAndIndent8("%s value = %s", TypeUtils.getPrimitiveClassWrapName(compomentClass), CastUtils.formatWithNoNewLine(GET_INSTANCE_CODE, TypeUtils.getPrimitiveClassWrapName(compomentClass))));
                body.append(CastUtils.formatAndIndent8("target[i] = %s", CastUtils.formatWithNoNewLine(CASTUTILS_GET_PRIM_CODE, TypeUtils.getPrimitiveClassWrapName(compomentClass), "value")));
            } else {
                body.append(CastUtils.formatAndIndent8("target[i] = %s", CastUtils.formatWithNoNewLine(GET_INSTANCE_CODE, TypeUtils.getArrayCanonicalName(compomentClass, dimension - 1, ""))));
            }
        } else {
            body.append(CastUtils.formatAndIndent8("target[i] = %s", CastUtils.formatWithNoNewLine(GET_INSTANCE_CODE, TypeUtils.getArrayCanonicalName(compomentClass, dimension - 1, ""))));
        }
        body.append(CastUtils.formatAndIndent6("}"));
        body.append(CastUtils.formatAndIndent2("}"));
        body.append(CastUtils.formatAndIndent2("result.setInstance(target);"));
        body.append(CastUtils.formatAndIndent2("return result;"));
        body.append(CastUtils.format("}"));
//		System.out.println(body.toString());
        return body.toString();
    }

    private String generateErrorResultStub(Class<?> compomentClass, int dimension) {
        StringBuilder body = new StringBuilder();
        body.append(CastUtils.formatWithNoNewLine("return DefaultResult.errorResult(new %s[source.size()]%s, %s);", TypeUtils.getCanonicalName(compomentClass), TypeUtils.getArrayBracketDesc(dimension - 1, ""), "childResult.resultMsg()"));
        return body.toString();
    }
    private void generateImportsStub(ClassPool pool) {
        pool.importPackage("cn.com.hjack.autobind.ResolvableConverter");
        pool.importPackage("cn.com.hjack.autobind.converter.ResolvableConverters");
        pool.importPackage("cn.com.hjack.autobind.Result");
        pool.importPackage("cn.com.hjack.autobind.binder.DefaultResult");
        pool.importPackage("cn.com.hjack.autobind.utils.TypeUtils");
        pool.importPackage("cn.com.hjack.autobind.utils.CastUtils");
        pool.importPackage("cn.com.hjack.autobind.TypeWrapper");
        pool.importPackage("cn.com.hjack.autobind.ResolveConfig");
        pool.importPackage("java.util.List");
    }

}
