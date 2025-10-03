/**
 *
 */
package cn.com.hjack.autobind.type;

import cn.com.hjack.autobind.FieldTypeWrapper;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.utils.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @ClassName: DefaultFieldTypeWrapper
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月8日
 *
 */
public class DefaultFieldTypeWrapper extends AbstractTypeWrapper implements FieldTypeWrapper {

    private Field field;

    private Class<?> implClass;

    private TypeWrapper ownerType;

    private TypeWrapper actualType;

    private Class<?> resolvedClass;

    public DefaultFieldTypeWrapper(Field field, Class<?> implClass, Map<String, TypeWrapper> variableContext) {
        super(variableContext);
        if (field == null || implClass == null) {
            throw new IllegalArgumentException("field or impl class can not be null");
        }
        this.field = field;
        this.implClass = implClass;
        // this.ownerType = new DefaultTypeWrapper(field.getDeclaringClass(), implClass);
        this.ownerType = TypeWrappers.getType(field.getDeclaringClass(), implClass);
        // this.actualType = new DefaultTypeWrapper(field.getGenericType());
        this.actualType = TypeWrappers.getType(field.getGenericType());
        resolveClass();
    }

    public DefaultFieldTypeWrapper(Field field, Class<?> implClass) {
        this(field, implClass, null, null);
    }

    public DefaultFieldTypeWrapper(Field field, Class<?> implClass, TypeWrapper ownerType) {
        this(field, implClass, ownerType, null);
    }

    /**
     * 调用此构造方法的场景 1. 该字段类型为泛型， typeWrapper 参数为运行时泛型上下文中的实例
     * <br> 2. 该字段类型为泛型的参数化类型，typeWrapper 为实际参数化类型
     * @param field
     * @param implClass
     * @param typeWrapper
     */
    public DefaultFieldTypeWrapper(Field field, Class<?> implClass, TypeWrapper ownerType, TypeWrapper actualType) {
        super(Optional.ofNullable(ownerType).orElse(DefaultTypeWrapper.EMPTY).resolveVariableContext());
        if (field == null || implClass == null) {
            throw new IllegalArgumentException("field or declaringClass can not be null");
        }
        this.field = field;
        this.implClass = implClass;
        if (ownerType != null) {
            this.ownerType = ownerType;
        } else {
            // this.ownerType = new DefaultTypeWrapper(field.getDeclaringClass(), implClass);
            this.ownerType = TypeWrappers.getType(field.getDeclaringClass(), implClass);
        }
        if (actualType != null) {
            this.actualType = actualType;
        } else {
            // this.actualType = new DefaultTypeWrapper(field.getGenericType());
            this.actualType = TypeWrappers.getType(field.getGenericType());
        }
        resolveClass();
    }

    @Override
    public TypeWrapper getGeneric(int index) {
        // return new DefaultFieldTypeWrapper(field, implClass, ownerType, actualType.getGeneric(index));
        return TypeWrappers.getFieldType(field, implClass, ownerType, actualType.getGeneric(index));
    }
    @Override
    public TypeWrapper[] getGenerics() {
        return Arrays.stream(actualType.getGenerics()).map(value -> {
            // return new DefaultFieldTypeWrapper(field, implClass, ownerType, value);
            return TypeWrappers.getFieldType(field, implClass, ownerType, value);
        }).toArray(FieldTypeWrapper[]::new);
    }

    @Override
    public TypeWrapper getComponentType() {
        // return new DefaultFieldTypeWrapper(field, implClass, ownerType, actualType.getComponentType());
        return TypeWrappers.getFieldType(field, implClass, ownerType, actualType.getComponentType());
    }

    @Override
    public Type getType() {
        return actualType.getType();
    }

    @Override
    public boolean emptyType() {
        return actualType.emptyType();
    }

    @Override
    public Class<?> resolve() {
        return resolvedClass;
    }


    @Override
    public TypeWrapper resolveGeneric(TypeWrapper originGenericType) {
        // return new DefaultFieldTypeWrapper(field, implClass, ownerType, super.resolveGeneric(originGenericType));
        return TypeWrappers.getFieldType(field, implClass, ownerType, super.resolveGeneric(originGenericType));
    }

    @Override
    public TypeWrapper resolveComponentType(TypeWrapper originComponentType) {
        // return new DefaultFieldTypeWrapper(field, implClass, ownerType, super.resolveComponentType(originComponentType));
        return TypeWrappers.getFieldType(field, implClass, ownerType, super.resolveComponentType(originComponentType));
    }

    @Override
    public Class<?> getFieldTypeClass() {
        return field.getType();
    }

    @Override
    public Type getFieldGenericType() {
        return field.getGenericType();
    }

    private void resolveClass() {
        // 该字段类型为泛型
        if (actualType.getType() instanceof TypeVariable
                || actualType.getType() instanceof WildcardType) {
            Map<String, TypeWrapper> context = getVariableContext();
            TypeWrapper resolveType = context.get(actualType.getType().getTypeName());
            if (resolveType != null) {
                this.actualType = resolveType;
                this.resolvedClass = resolveType.resolve();
            }
        } else if (actualType.getType() instanceof Class) {
            this.resolvedClass = (Class<?>) actualType.getType();
        } else if (actualType.getType() instanceof ParameterizedType) {
            this.resolvedClass = (Class<?>) ((ParameterizedType) actualType.getType()).getRawType();
        } else if (actualType.getType() instanceof GenericArrayType) {
            Type componentType = TypeUtils.getComponentNonArrayType(actualType.getType());
            if (componentType instanceof Class) {
                this.resolvedClass = TypeUtils.getArrayClass((Class<?>) componentType, TypeUtils.getArrayTypeDimension(actualType.getType()));
            } else if (componentType instanceof ParameterizedType) {
                this.resolvedClass = TypeUtils.getArrayClass((Class<?>) ((ParameterizedType) componentType).getRawType(), TypeUtils.getArrayTypeDimension(actualType.getType()));
            }
        }
    }
}
