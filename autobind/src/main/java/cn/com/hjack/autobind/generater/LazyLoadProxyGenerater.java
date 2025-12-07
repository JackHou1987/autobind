package cn.com.hjack.autobind.generater;

import cn.com.hjack.autobind.Generater;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;
import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @ClassName: LazyLoadProxyGenerator
 * @Description: TODO
 * @author houqq
 * @date: 2025年10月31日
 *
 */
public class LazyLoadProxyGenerater implements Generater<Object> {

    private Class<?> targetClass;

    private Supplier<?> supplier;

    private static Map<Class<?>, Object> lazyLoadProxyCache = new ConcurrentHashMap<>();

    public LazyLoadProxyGenerater(Class<?> targetClass, Supplier<?> supplier) {
        if (targetClass == null) {
            throw new IllegalStateException("proxy class can not be null");
        }
        if (supplier == null) {
            throw new IllegalStateException("supplier can not be null");
        }
        this.targetClass = targetClass;
        this.supplier = supplier;
    }

    @Override
    public Object generate() {
        return lazyLoadProxyCache.computeIfAbsent(targetClass, (key) -> {
            try {
                ClassPool pool = ClassPool.getDefault();
                CtClass ctClass = generateLazyLoadProxyClass(pool, targetClass);
                generateLazyLoadProxyFields(targetClass, pool, ctClass);
                generateLazyLoadProxyConstructs(pool, targetClass, ctClass);
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
    }

    private CtClass generateLazyLoadProxyClass(ClassPool pool, Class<?> targetClass) throws Exception {
        CtClass targetCls = pool.get(targetClass.getName());
        CtClass cls = pool.makeClass(String.format("cn.com.yitong.actions.autobind.converter.%s$LazyLoadProxy", targetClass.getSimpleName()));
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
        constructor.setBody(generateLazyLoadProxyConstructContentStub());
        cls.addConstructor(constructor);
    }

    private String generateLazyLoadProxyConstructContentStub() {
        return CastUtils.format("{") +
                CastUtils.formatAndIndent2("if ($1 == null) {") +
                CastUtils.formatAndIndent4("throw new IllegalStateException(\"supplier can not be null\");") +
                CastUtils.formatAndIndent2("}") +
                CastUtils.formatAndIndent2("this.supplier = $1;") +
                CastUtils.format("}");
    }

    private void generateLazyLoadProxyMethod(ClassPool pool, CtClass cls, Class<?> targetClass, Method method) throws Exception {
        Class<?> returnType = method.getReturnType();
        String methodContent = generateLazyLoadMethodStub(targetClass, method);
        CtMethod ctMethod = CtNewMethod.make(pool.get(returnType.getName()), method.getName(), getParameterClass(pool, method), getExceptionClass(pool, method), methodContent, cls);
        cls.addMethod(ctMethod);
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
        sb.append(CastUtils.format("{"));
        sb.append(CastUtils.formatAndIndent2("if (this.object == null) {"));
        sb.append(CastUtils.formatAndIndent4("lock.lock();"));
        sb.append(CastUtils.formatAndIndent4("if (this.object == null) {"));
        sb.append(CastUtils.formatAndIndent6("try {"));
        sb.append(CastUtils.formatAndIndent8("this.object = (%s) supplier.get();", TypeUtils.getCanonicalName(targetClass)));
        sb.append(CastUtils.formatAndIndent6("} finally {"));
        sb.append(CastUtils.formatAndIndent4("lock.unlock();"));
        sb.append(CastUtils.formatAndIndent6("}"));
        sb.append(CastUtils.formatAndIndent4("}"));
        sb.append(CastUtils.formatAndIndent2("}"));
        String body;
        String returnTypeName = TypeUtils.getCanonicalName(method.getReturnType());
        if (Objects.equals(returnTypeName, "void")) {
            body = doGenerateLazyLoadMethodStub(method, false);
        } else {
            body = doGenerateLazyLoadMethodStub(method, true);
        }
        sb.append(body);
        sb.append(CastUtils.format("}"));
        return sb.toString();
    }

    private String doGenerateLazyLoadMethodStub(Method method, boolean haveReturnValue) {
        StringBuilder body = new StringBuilder();
        Parameter[] parameters = method.getParameters();
        if (parameters == null || parameters.length == 0) {
            if (haveReturnValue) {
                body.append(CastUtils.formatAndIndent2("if (object != null) {"));
                body.append(CastUtils.formatAndIndent4("return this.object.%s();"));
                body.append(CastUtils.formatAndIndent2("} else {"));
                body.append(CastUtils.formatAndIndent4("return null;"));
                body.append(CastUtils.formatAndIndent2("}"));
            } else {
                body.append(CastUtils.formatAndIndent2("if (object != null) {"));
                body.append(CastUtils.formatAndIndent4("this.object.%s();"));
                body.append(CastUtils.formatAndIndent2("}"));
            }
            return String.format(body.toString(), method.getName());
        } else {
            if (haveReturnValue) {
                body.append(CastUtils.formatAndIndent2("if (object != null) {"));
                body.append(CastUtils.formatAndIndent4("return this.object.%s(%s);"));
                body.append(CastUtils.formatAndIndent2("} else {"));
                body.append(CastUtils.formatAndIndent4("return null;"));
                body.append(CastUtils.formatAndIndent2("}"));
            } else {
                body.append(CastUtils.formatAndIndent2("if (object != null) {"));
                body.append(CastUtils.formatAndIndent4("this.object.%s(%s);"));
                body.append(CastUtils.formatAndIndent2("}"));
            }
            String parameterStr = IntStream.range(0, parameters.length).mapToObj(i -> {
                return "$" + (i + 1);
            }).collect(Collectors.joining(","));
            return String.format(body.toString(), method.getName(), parameterStr);
        }
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

}
