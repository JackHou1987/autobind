/**
 *
 */
package cn.com.hjack.autobind.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.core.CollectionFactory;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;


/**
 * @ClassName: TypeUtils
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月8日
 *
 */
public class TypeUtils {

    private static final Map<Class<?>, Boolean> javaBeanClassMap = new ConcurrentHashMap<>();

    /**
     * @Title: getComponentNonArrayType
     * @Description: 获得数组组件类型，当为多维数组时，返回最终非数组组件类型
     * @param: array type
     * @return: component type
     */
    public static Type getComponentNonArrayType(Type type) {
        if (type == null) {
            return null;
        }
        if (!(type instanceof Class) && !(type instanceof GenericArrayType)) {
            return type;
        } else {
            if (type instanceof Class) {
                Class<?> cls = (Class<?>) type;
                if (cls.isArray()) {
                    return getArrayComponentNonArrayType(cls);
                } else {
                    return cls;
                }
            } else {
                return getComponentNonArrayType(((GenericArrayType) type).getGenericComponentType());
            }
        }
    }

    public static Type getClassOrParameterizedType(Type baseType, Type implType) {
        if (baseType == null) {
            return null;
        }
        if ((baseType instanceof ParameterizedType)) {
            return null;
        }
        if (implType == null) {
            return baseType;
        }
        if ((implType instanceof ParameterizedType)) {
            return null;
        }
        Class<?> baseClass;
        if (baseType instanceof Class) {
            baseClass = (Class<?>) baseType;
        } else {
            baseClass = (Class<?>) (((ParameterizedType) baseType).getRawType());
        }
        Class<?> implClass;
        if (implType instanceof Class) {
            implClass = (Class<?>) implType;
        } else {
            implClass = (Class<?>) (((ParameterizedType) implType).getRawType());
        }
        Type[] interfaceTypes = implClass.getGenericInterfaces();
        for (Type interfaceType : interfaceTypes) {
            if (interfaceType == baseType) {
                return interfaceType;
            }
            if (interfaceType instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) interfaceType).getRawType();
                Class<?> rawClass = (Class<?>) rawType;
                if (rawClass == baseClass) {
                    return interfaceType;
                } else {
                    Type type = getClassOrParameterizedType(baseType, interfaceType);
                    if (type != null) {
                        return type;
                    } else {
                        continue;
                    }
                }
            } else if (interfaceType instanceof Class) {
                Class<?> interfaceClass = (Class<?>) interfaceType;
                if (interfaceClass == baseClass) {
                    return interfaceType;
                }
            } else {
                continue;
            }
        }
        Type superType = null;
        Type genericSuperType = implClass.getGenericSuperclass();
        while(genericSuperType != null && genericSuperType != Object.class) {
            superType = genericSuperType;
            if (superType == baseType) {
                return superType;
            }
            if (superType instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) superType).getRawType();
                Class<?> rawClass = (Class<?>) rawType;
                if (rawClass == baseClass) {
                    return superType;
                } else {
                    genericSuperType = rawClass.getGenericSuperclass();
                    continue;
                }
            } else if (superType instanceof Class) {
                Class<?> superClass = (Class<?>) superType;
                if (superClass == baseClass) {
                    return superClass;
                }
            } else {
                break;
            }
        }
        return baseType;
    }

    /**
     * @Title: getOrDefaultValue
     * @Description: 获得当前值或默认值
     * @param: value 当前值
     * @param: autoBind 默认值
     * @return: Object
     */
    public static Object getOrDefaultValue(Object value, String defaultValue) {
        if (StringUtils.isEmpty(defaultValue)) {
            return value;
        }
        if (value == null && !StringUtils.isEmpty(defaultValue)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    /**
     * @Title: arrayOrCollectionToList
     * @Description: 数组或集合转列表
     * @param: object
     * @return: 转换后的列表
     */
    public static List<Object> arrayOrCollectionToList(Object object) {
        if (object == null) {
            return new ArrayList<>();
        } else if (isCollectionClass(object.getClass())) {
            return new ArrayList<>((Collection<?>) object);
        } else if (isArrayClass(object.getClass())) {
            return arrayToList(object);
        } else {
            List<Object> collection = new ArrayList<>();
            collection.add(object);
            return collection;
        }
    }


    private static List<Object> arrayToList(Object array) {
        if (array == null || !isArrayClass(array.getClass())) {
            return new ArrayList<>();
        } else {
            int size = Array.getLength(array);
            if (size == 0) {
                return new ArrayList<>();
            } else {
                List<Object> collection = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    collection.add(Array.get(array, i));
                }
                return collection;
            }
        }
    }

    /**
     * @Title: isJavaBeanClass
     * @Description: 判断目标Class是否属于一个java bean,只有当目标类中包含非静态字段，
     * 且有对应的getter和setter方法
     * @param: 目标class
     * @return: boolean
     */
    public static boolean isJavaBeanClass(Class<?> cls) {
        if (cls == null) {
            return false;
        } else {
            return javaBeanClassMap.computeIfAbsent(cls, (key) -> {
                if (cls.isPrimitive()
                        || cls.isArray()
                        || cls.isInterface()
                        || cls.isAnnotation()
                        || cls.isEnum()
                        || cls.isAnonymousClass()
                        || cls.isSynthetic()) {
                    return false;
                }
                AtomicBoolean found = new AtomicBoolean(false);
                ReflectionUtils.doWithFields(cls, (field) -> {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        Method getterMethod = findGetterMethod(field);
                        Method setterMethod = findSetterMethod(field);
                        if (getterMethod != null && setterMethod != null) {
                            found.set(true);
                            return;
                        }
                    }
                });
                return found.get();
            });

        }
    }
    /**
     * @Title: isMapClass
     * @Description: 判断是否是map类型
     * @param: cls
     * @return: boolean
     */
    public static boolean isMapClass(Class<?> cls) {
        return cls != null && Map.class.isAssignableFrom(cls);
    }


    /**
     * @Title: isCollectionClass
     * @Description: 判断是否是集合类型
     * @param: cls
     * @return: boolean
     */
    public static boolean isCollectionClass(Class<?> cls) {
        return cls != null && Collection.class.isAssignableFrom(cls);
    }

    public static boolean isArrayClass(Class<?> cls) {
        return cls != null && cls.isArray();
    }

    public static boolean isPrimitiveArrayClass(Class<?> cls) {
        return cls != null && cls.isArray() && TypeUtils.getArrayComponentNonArrayType(cls).isPrimitive();
    }

    public static boolean isNonPrimitiveArrayClass(Class<?> cls) {
        return cls != null && cls.isArray() && !TypeUtils.getArrayComponentNonArrayType(cls).isPrimitive();
    }

    public static boolean isEnumClass(Class<?> cls) {
        return cls != null && cls.isEnum();
    }

    public static Collection<Object> createCollection(Class<?> collectionType) {
        return CollectionFactory.createCollection(collectionType, 16);
    }

    public static Map<Object, Object> createMap(Class<?> mapType) {
        return CollectionFactory.createMap(mapType, 16);
    }

    /**
     * @Title: createArrayBySource
     * @Description: 依据源数组类型创建新空数组,如果源数组最终组件类型为map或javabean则新数组组件类型为map
     * @param: 源数组array
     * @param: 数组大小
     * @return: 新数组对象
     */
    public static Object createArrayBySource(Object array, int length) {
        if (array == null) {
            return null;
        } else {
            if (isArrayClass(array.getClass().getComponentType())) {
                Object emptyChildArray = getEmptyComponentInstance(array.getClass().getComponentType());
                Object arrayObj = Array.newInstance(emptyChildArray.getClass().getComponentType(), length);
                for (int i = 0; i < length; i++) {
                    Object childObj = Array.get(array, i);
                    Array.set(arrayObj, i, createArrayBySource(childObj, Array.getLength(childObj)));
                }
                return arrayObj;
            } else {
                return createArrayByComponentType(array.getClass().getComponentType(), length);
            }
        }
    }
    /**
     * @Title: createEmptyArray
     * @Description: 创建空数组
     * @param: 数组组件类型
     * @param: 数组维度
     * @param: 数组对象
     */
    public static Object createEmptyArray(Class<?> componentType, int dimension) {
        if (dimension <= 0) {
            return null;
        } else {
            Object targetArray = Array.newInstance(componentType, 0);
            Object array = createEmptyArray(targetArray.getClass(), --dimension);
            if (array == null) {
                return targetArray;
            } else {
                targetArray = array;
                return targetArray;
            }
        }
    }

    /**
     * @Title: getArrayClass
     * @Description: 获取数组class
     * @param: 组件class
     * @param: 数组维度
     * @return: 数组class
     */
    public static Class<?> getArrayClass(Class<?> componentClass, int dimension) {
        if (dimension <= 0) {
            throw new IllegalStateException("array dimension less than zero");
        } else {
            return getArrayClass(ResolvableType.forClass(componentClass), dimension);
        }
    }

    private static Class<?> getArrayClass(ResolvableType arrayType, int dimension) {
        if (arrayType == null) {
            return null;
        }
        if (dimension <= 0) {
            return arrayType.resolve();
        } else {
            return getArrayClass(ResolvableType.forArrayComponent(arrayType), --dimension);
        }
    }

    /**
     * @Title: getArrayTypeDimension
     * @Description: 获取数组类型维度
     * @param: type -> GenericArrayType or array class
     * @return: 数组类型维度
     */
    public static int getArrayTypeDimension(Type type) {
        if (type == null) {
            return 0;
        }
        if (!(type instanceof GenericArrayType) && !(type instanceof Class)) {
            return 0;
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            return 1 + getArrayTypeDimension(arrayType.getGenericComponentType());
        } else {
            Class<?> cls = (Class<?>) type;
            if (!cls.isArray()) {
                return 0;
            } else {
                return 1 + getArrayTypeDimension(cls.getComponentType());
            }
        }
    }
    public static Class<?> getFieldActualClass(Field field, Object value) {
        if (value != null) {
            return value.getClass();
        }
        if (field != null) {
            return field.getType();
        }
        return null;
    }

    /**
     * @Title: createArrayByComponentType
     * @Description: 依据组件类型创建空数组
     * @param: 组件类型
     * @param: 数组大小
     * @return: 空数组
     */
    private static Object createArrayByComponentType(Class<?> componentCls, int length) {
        if (isJavaBeanClass(componentCls) || isMapClass(componentCls)) {
            return Array.newInstance(Map.class, length);
        } else if (isCollectionClass(componentCls)) {
            return Array.newInstance(Collection.class, length);
        } else {
            return Array.newInstance(componentCls, length);
        }
    }

    private static Object getEmptyComponentInstance(Class<?> componentCls) {
        if (isArrayClass(componentCls)) {
            Object objArray = getEmptyComponentInstance(componentCls.getComponentType());
            return Array.newInstance(objArray.getClass(), 0);
        } else if (isJavaBeanClass(componentCls) || isMapClass(componentCls)) {
            return Array.newInstance(Map.class, 0);
        } else {
            return Array.newInstance(componentCls, 0);
        }
    }

    /**
     * @Title: getArrayComponentNonArrayType
     * @Description: 获取多维数组后最终组件类型
     * @param: 数组class
     * @return: 组件Class
     */
    public static Class<?> getArrayComponentNonArrayType(Class<?> arrayClass) {
        if (!isArrayClass(arrayClass)) {
            return null;
        } else {
            if (!isArrayClass(arrayClass.getComponentType())) {
                return arrayClass.getComponentType();
            } else {
                return getArrayComponentNonArrayType(arrayClass.getComponentType());
            }
        }
    }

    public static String getClassPackageName(Class<?> cls) {
        if (cls == null) {
            return null;
        } else if (isArrayClass(cls)) {
            Class<?> componentType = getArrayComponentNonArrayType(cls);
            if (componentType == null) {
                return null;
            } else {
                return componentType.getPackage().getName();
            }
        } else {
            return cls.getPackage().getName();
        }
    }

    /**
     * @Title: getPrimitiveClassWrapName
     * @Description: 获取基本类型包装类型名称
     * @param: 基本类型
     * @return: 包装类型名称
     */
    public static String getPrimitiveClassWrapName(Class<?> cls) {
        if (cls == null || !cls.isPrimitive()) {
            return null;
        }
        if (cls == byte.class) {
            return "Byte";
        } else if (cls == char.class) {
            return "Character";
        } else if (cls == short.class) {
            return "Short";
        } else if (cls == int.class) {
            return "Integer";
        } else if (cls == long.class) {
            return "Long";
        } else if (cls == boolean.class) {
            return "Boolean";
        } else if (cls == float.class) {
            return "Float";
        } else {
            return "Double";
        }
    }

    /**
     * @Title: getPrimitiveWrapperClass
     * @Description: 获取基本类型class包装类型class,如int.class -> Integer.class
     * @param: 基本类型Class
     * @return: 包装类型Class
     */
    public static Class<?> getPrimitiveWrapperClass(Class<?> cls) {
        if (cls == null || !cls.isPrimitive()) {
            return null;
        }
        if (cls == byte.class) {
            return Byte.class;
        } else if (cls == char.class) {
            return Character.class;
        } else if (cls == short.class) {
            return Short.class;
        } else if (cls == int.class) {
            return Integer.class;
        } else if (cls == long.class) {
            return Long.class;
        } else if (cls == boolean.class) {
            return Boolean.class;
        } else if (cls == float.class) {
            return Float.class;
        } else {
            return Double.class;
        }
    }
    /**
     * @Title: getArrayOrCollectionSize
     * @Description: 获得数组或集合元素大小，如果object不是一个数组或集合，则返回0
     * @param: object source
     * @return: size
     */
    public static int getArrayOrCollectionSize(Object object) {
        if (isCollectionClass(object.getClass())) {
            return ((Collection<?>) object).size();
        } else if (object.getClass().isArray()) {
            return Array.getLength(object);
        } else {
            return 0;
        }
    }

    /**
     * @Title: getCanonicalName
     * @Description: 获取类全限定名称
     * @param: Class
     * @return: String
     */
    public static String getCanonicalName(Class<?> cls) {
        if (cls == null) {
            return null;
        } else {
            return org.apache.commons.lang3.ClassUtils.getCanonicalName(cls);
        }
    }

    /**
     * @Title: getCanonicalNameWithSuffix
     * @Description: 获取.class带后缀全限定名称
     * @param: class
     * @return: 带后缀全限定名称
     * @throws
     */
    public static String getCanonicalNameWithSuffix(Class<?> cls) {
        if (cls == null) {
            return null;
        } else {
            return org.apache.commons.lang3.ClassUtils.getCanonicalName(cls) + ".class";
        }
    }

    /**
     * @Title: getArrayCanonicalName
     * @Description: 获取数组全限定名称 如: a[2][3]
     * @param: 数组前缀
     * @param: 数组维度
     * @param: 数组索引
     * @return: 获取数组全限定名称
     */
    public static String getArrayCanonicalName(String prefix, int dimension, String index) {
        if (StringUtils.isEmpty(prefix) || dimension < 0) {
            return null;
        }
        if (dimension == 0) {
            return prefix;
        } else {
            return prefix + getArrayBracketDesc(dimension, index);
        }
    }

    /**
     * @Title: getArrayCanonicalName
     * @Description: 获取数组全限定名称(如java.lang.String[2][])
     * @param: 数组组件class
     * @param: 数组维度
     * @param: 数组下标索引
     * @return: 获取数组全限定名称
     */
    public static String getArrayCanonicalName(Class<?> componentClass, int dimension, String index) {
        if (componentClass == null || dimension < 0) {
            return null;
        }
        if (dimension == 0) {
            return getCanonicalName(componentClass);
        } else {
            return getCanonicalName(componentClass) + getArrayBracketDesc(dimension, index);
        }
    }
    /**
     * @Title: getPrimitiveArrayCanonicalName
     * @Description: 获得基本类型数组名称如:int[2][]
     * @param: 组件类型
     * @param: 数组维度
     * @param: 数组索引
     * @return: 基本类型数组名称
     */
    public static String getPrimitiveArrayCanonicalName(Class<?> componentClass, int dimension, String index) {
        if (componentClass == null || !componentClass.isPrimitive() || dimension < 0) {
            return null;
        }
        if (dimension == 0) {
            return getCanonicalName(getPrimitiveWrapperClass(componentClass));
        } else {
            return getCanonicalName(componentClass) + getArrayBracketDesc(dimension, index);
        }
    }

    public static String getArrayCanonicalName(Class<?> componentClass, int dimension) {
        return getArrayCanonicalName(componentClass, dimension, "");
    }

    /**
     * @Title: getArrayBracketDesc
     * @Description: 获取数组带中括号字符串描述符
     * @param: 数组维度
     * @param: 数组索引
     * @return: 字符串描述符
     */
    public static String getArrayBracketDesc(int dimension, String index) {
        if (dimension <= 0) {
            return "";
        } else {
            StringBuilder str = new StringBuilder();
            if (StringUtils.isEmpty(index)) {
                str.append("[]");
            } else {
                str.append("[" + index + "]");
            }
            return getArrayBracketDesc(str.toString(), --dimension);
        }
    }

    private static String getArrayBracketDesc(String prefix, int dimension) {
        StringBuilder str = new StringBuilder();
        str.append(prefix);
        if (dimension <= 0) {
            return str.toString();
        } else {
            str.append("[]");
            return getArrayBracketDesc(str.toString(), --dimension);
        }
    }

    public static boolean isInstance(Class<?> cls, Object object) {
        if (cls == null || object == null) {
            return false;
        } else {
            return cls.isInstance(object);
        }
    }

    /**
     * @Title: findGetterMethod
     * @Description: 获取当前字段的get方法
     * @param: 当前字段
     * @return: get method
     */
    public static Method findGetterMethod(Field field) {
        String getterMethodName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1, field.getName().length());
        try {
            return ReflectionUtils.findMethod(field.getDeclaringClass(), getterMethodName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @Title: findGetterMethod
     * @Description: 获取当前字段的set方法
     * @param: 当前字段
     * @return: get method
     */
    public static Method findSetterMethod(Field field) {
        String setterMethodName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1, field.getName().length());
        try {
            return ReflectionUtils.findMethod(field.getDeclaringClass(), setterMethodName, field.getType());
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isNumber(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        } else {
            char[] chrs = str.toCharArray();
            for (char c : chrs) {
                if (c < '0' || c > '9') {
                    return false;
                }
            }
            return true;
        }
    }

    public static ClassLoader getClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = TypeUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

}
