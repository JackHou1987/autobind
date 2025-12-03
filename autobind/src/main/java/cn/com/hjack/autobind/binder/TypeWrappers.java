/**
 *
 */
package cn.com.hjack.autobind.binder;

import cn.com.hjack.autobind.FieldTypeWrapper;
import cn.com.hjack.autobind.TypeWrapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


/**
 * @author houqq
 * @date: 2025年6月30日
 */
public class TypeWrappers {

    public static TypeWrapper EMPTY = new TypeWrapperImpl();

    public static TypeWrapper object() {
        return new TypeWrapperImpl(Object.class);
    }

    public static TypeWrapper none() {
        return new TypeWrapperImpl();
    }

    public static TypeWrapper getType(Class<?> type) {
        return new TypeWrapperImpl(type);
    }

    public static TypeWrapper getType(Class<?> baseType, Class<?> implType) {
        return new TypeWrapperImpl(baseType, implType);
    }

    public static FieldTypeWrapper getFieldType(Field field, Class<?> implClass, Map<String, TypeWrapper> variableContext) {
        return new FieldTypeWrapperImpl(field, implClass, variableContext);
    }

    public static TypeWrapper getAndResolveGenericType(TypeWrapper typeWrapper, int index) {
        if (typeWrapper == null) {
            return TypeWrappers.EMPTY;
        } else {
            TypeWrapper genericType = typeWrapper.resolveGeneric(typeWrapper.getGeneric(index));
            if (genericType == null) {
                return TypeWrappers.EMPTY;
            } else {
                return genericType;
            }
        }
    }

    /**
     * 解析多维数组非数组组件类型
     * @param arrayType 数组类型
     * @return: TypeWrapper
     */
    public static TypeWrapper getAndResolveComponentNonArrayType(TypeWrapper arrayType) {
        if (arrayType == null) {
            return TypeWrappers.EMPTY;
        } else {
            TypeWrapper componentType = arrayType.resolveComponentType(arrayType.getComponentType());
            if (componentType == null) {
                return TypeWrappers.EMPTY;
            } else {
                return componentType;
            }
        }
    }

    public static FieldTypeWrapper getFieldType(Field field, Class<?> implClass, TypeWrapper typeWrapper) {
        if (typeWrapper != null) {
            return new FieldTypeWrapperImpl(field, implClass, typeWrapper.resolveVariableContext());
        } else {
            return new FieldTypeWrapperImpl(field, implClass, new HashMap<>());
        }
    }
}
