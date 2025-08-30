package cn.com.hjack.autobind.type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.utils.TypeUtils;
import org.springframework.util.ObjectUtils;


/**
 * @ClassName: DefaultTypeWrapper
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月4日
 *
 */
public class DefaultTypeWrapper extends AbstractTypeWrapper {

    private Type type;

    private Class<?> baseClass;

    private Class<?> implClass;

    public static DefaultTypeWrapper EMPTY = new DefaultTypeWrapper();

    public DefaultTypeWrapper(Class<?> baseCls, Class<?> implCls) {
        if (baseCls == null || implCls == null) {
            throw new IllegalArgumentException("base type or impl type can not be null");
        }
        this.type = TypeUtils.getClassOrParameterizedType(baseCls, implCls);
    }

    public DefaultTypeWrapper(Class<?> implCls) {
        if (implCls == null) {
            throw new IllegalArgumentException("impl type can not be null");
        }
        this.type = implCls;
    }

    public DefaultTypeWrapper(Type type) {
        this.type = type;
    }

    public DefaultTypeWrapper() {
    }


    @Override
    public TypeWrapper getGeneric(int index) {
        if (type instanceof Class) {
            return DefaultTypeWrapper.EMPTY;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (index > actualTypeArguments.length - 1) {
                throw new IllegalStateException("");
            } else {
                // return new DefaultTypeWrapper(actualTypeArguments[index]);
                return TypeWrappers.getType(actualTypeArguments[index]);
            }
        } else if (type instanceof TypeVariable
                || type instanceof WildcardType || type instanceof GenericArrayType) {
            return DefaultTypeWrapper.EMPTY;
        } else {
            return DefaultTypeWrapper.EMPTY;
        }
    }

    @Override
    public TypeWrapper[] getGenerics() {
        if (type == null) {
            return new TypeWrapper[] {DefaultTypeWrapper.EMPTY};
        } else if (type instanceof Class) {
            return new TypeWrapper[] {DefaultTypeWrapper.EMPTY};
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            TypeWrapper[] typeWrappers = new TypeWrapper[actualTypeArguments.length];
            for (int i = 0; i < actualTypeArguments.length; i++) {
                // typeWrappers[i] = new DefaultTypeWrapper(actualTypeArguments[i]);
                typeWrappers[i] = TypeWrappers.getType(actualTypeArguments[i]);
            }
            return typeWrappers;
        } else if (type instanceof TypeVariable
                || type instanceof WildcardType
                || type instanceof GenericArrayType) {
            return new TypeWrapper[] {DefaultTypeWrapper.EMPTY};
        } else {
            return new TypeWrapper[] {DefaultTypeWrapper.EMPTY};
        }
    }

    @Override
    public TypeWrapper getComponentType() {
        if (type == null) {
            return DefaultTypeWrapper.EMPTY;
        } else if (type instanceof Class) {
            Class<?> cls = (Class<?>) type;
            if (cls.isArray()) {
                // return new DefaultTypeWrapper(cls.getComponentType());
                return TypeWrappers.getType(cls.getComponentType());
            } else {
                return DefaultTypeWrapper.EMPTY;
            }
        } else if (type instanceof GenericArrayType) {
            // return new DefaultTypeWrapper(((GenericArrayType) type).getGenericComponentType());
            return TypeWrappers.getType(((GenericArrayType) type).getGenericComponentType());
        } else {
            return DefaultTypeWrapper.EMPTY;
        }
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean emptyType() {
        return type == null;
    }

    @Override
    public Class<?> resolve() {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class<?>) {
                return (Class<?>) rawType;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    @Override
    public int hashCode() {
        int hashCode = 0;
        if (this.type != null) {
            hashCode = 31 * hashCode + type.hashCode();
        }
        if (this.baseClass != null) {
            hashCode = 31 * hashCode + baseClass.hashCode();
        }
        if (this.implClass != null) {
            hashCode = 31 * hashCode + implClass.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DefaultTypeWrapper)) {
            return false;
        }

        DefaultTypeWrapper otherType = (DefaultTypeWrapper) other;
        if (type != otherType.type && !ObjectUtils.nullSafeEquals(this.type, otherType.type)) {
            return false;
        }
        if (baseClass != otherType.baseClass && !ObjectUtils.nullSafeEquals(this.baseClass, otherType.baseClass)) {
            return false;
        }
        if (implClass != otherType.implClass && !ObjectUtils.nullSafeEquals(this.implClass, otherType.implClass)) {
            return false;
        }
        if (type != otherType.type && !ObjectUtils.nullSafeEquals(this.type, otherType.type)) {
            return false;
        }
        return true;
    }

}


