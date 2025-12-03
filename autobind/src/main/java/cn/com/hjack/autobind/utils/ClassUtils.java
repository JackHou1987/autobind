/**
 *
 */
package cn.com.hjack.autobind.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.core.CollectionFactory;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;


/**
 * @ClassName: ClassUtils
 * @Description: TODO
 * @author houqq
 * @date: 2025年3月24日
 *
 */
public class ClassUtils {


    /**
     * @Title: isJavaBeanClass
     * @Description: 判断目标Class是否属于一个java bean,只有当目标类中包含非静态字段，
     * 且有对应的getter和setter方法时，目标class是java bean class
     * @param: 目标class
     * @return: boolean
     */
    public static boolean isJavaBeanClass(Class<?> cls) {
        if (cls == null) {
            return false;
        } else {
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
                    Method getterMethod = ClassUtils.findGetterMethod(field);
                    Method setterMethod = ClassUtils.findSetterMethod(field);
                    if (getterMethod != null && setterMethod != null) {
                        found.set(true);
                        return;
                    }
                }
            });
            return found.get();
        }
    }

    /**
     * @Title: isMapClass
     * @Description: 判断是否是map类型
     * @param: @param cls
     * @param: @return
     * @return: boolean
     * @throws
     */
    public static boolean isMapClass(Class<?> cls) {
        return cls != null && Map.class.isAssignableFrom(cls);
    }

    /**
     * @Title: isCollectionClass
     * @Description: 判断是否是集合类型
     * @param: @param cls
     * @param: @return
     * @return: boolean
     * @throws
     */
    public static boolean isCollectionClass(Class<?> cls) {
        return cls != null && Collection.class.isAssignableFrom(cls);
    }

    public static boolean isArrayClass(Class<?> cls) {
        return cls != null && cls.isArray();
    }

    public static boolean isAtomicReferenceClass(Class<?> cls) {
        return cls != null && AtomicReference.class.isAssignableFrom(cls);
    }

    public static Collection<Object> createCollection(Class<?> collectionType) {
        return CollectionFactory.createCollection(collectionType, 16);
    }

    public static Map<Object, Object> createMap(Class<?> mapType) {
        return CollectionFactory.createMap(mapType, 16);
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
     * @throws
     */
    public static int getArrayOrCollectionSize(Object object) {
        if (ClassUtils.isCollectionClass(object.getClass())) {
            return ((Collection<?>) object).size();
        } else if (object.getClass().isArray()) {
            return Array.getLength(object);
        } else {
            return 0;
        }
    }

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
     * @throws
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
     * @throws
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
     * @throws
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


    public static Method findGetterMethod(Field field) {
        String getterMethodName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1, field.getName().length());
        try {
            return ReflectionUtils.findMethod(field.getDeclaringClass(), getterMethodName);
        } catch (Exception e) {
            return null;
        }
    }

    public static Method findSetterMethod(Field field) {
        String setterMethodName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1, field.getName().length());
        try {
            return ReflectionUtils.findMethod(field.getDeclaringClass(), setterMethodName, field.getType());
        } catch (Exception e) {
            return null;
        }
    }

}
