/**
 *
 */
package cn.com.hjack.autobind.type;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import cn.com.hjack.autobind.FieldTypeWrapper;
import cn.com.hjack.autobind.TypeWrapper;
import org.springframework.core.ResolvableType;


/**
 * @ClassName: FieldTypeWrapperImpl
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月23日
 *
 */
public class FieldTypeWrapperImpl extends AbstractTypeWrapper implements FieldTypeWrapper {

    private final ResolvableType fieldResolvableType;

    private final Field field;

    private final Class<?> implClass;

    /**
     * 该fieldtype为泛型时,该actualType可能不为空
     */
    private TypeWrapper actualType;

    public FieldTypeWrapperImpl(Field field, Class<?> implClass, Map<String, TypeWrapper> variableContext) {
        super(variableContext);
        if (field == null || implClass == null) {
            throw new IllegalArgumentException("field or impl class or variable context can not be null");
        }
        this.field = field;
        this.implClass = implClass;
        this.fieldResolvableType = ResolvableType.forField(field, implClass);
        // 该字段类型为泛型
        if (fieldResolvableType.getType() instanceof TypeVariable || fieldResolvableType.getType() instanceof WildcardType) {
            // 无法解析
            if (fieldResolvableType.resolve() == null) {
                Map<String, TypeWrapper> context = getVariableContext();
                actualType = context.get(fieldResolvableType.getType().getTypeName());
            }
        }
    }

    /**
     * 调用此构造方法的场景 1. 该字段类型为泛型， typeWrapper 参数为运行时泛型上下文中的实例
     * <br> 2. 该字段类型为泛型的参数化类型，typeWrapper 为实际参数化类型
     * @param field
     * @param implClass
     * @param typeWrapper
     */
    private FieldTypeWrapperImpl(Field field, Class<?> implClass, TypeWrapper type) {
        if (field == null || implClass == null) {
            throw new IllegalArgumentException("field or declaringClass can not be null");
        }
        this.field = field;
        this.implClass = implClass;
        this.fieldResolvableType = ResolvableType.forField(field, implClass);
        this.actualType = type;
    }

    /**
     * 当该field type wrapper不是一个泛型字段, 生成该字段的一个参数化类型type wrapper对象时，调用此构造方法
     * @param field
     * @param implClass
     * @param targetType
     */
    private FieldTypeWrapperImpl(Field field, Class<?> implClass, ResolvableType resolvableType) {
        if (field == null || implClass == null || resolvableType == null) {
            throw new IllegalArgumentException("field or declaringClass or resolvableType can not be null");
        }
        this.fieldResolvableType = resolvableType;
        this.field = field;
        this.implClass = implClass;
    }

    @Override
    public Type getType() {
        if (actualType != null) {
            return actualType.getType();
        } else {
            return fieldResolvableType.getType();
        }
    }
    @Override
    public TypeWrapper getGeneric(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("illegal argument index");
        }
        if (actualType != null) {
            return new FieldTypeWrapperImpl(field, implClass, actualType.getGeneric(index));
        } else {
            if (fieldResolvableType == null) {
                throw new IllegalStateException("resolvableType can not be null");
            }
            return new FieldTypeWrapperImpl(field, implClass, fieldResolvableType.getGeneric(index));
        }
    }

    @Override
    public TypeWrapper[] getGenerics() {
        if (actualType != null) {
            return Arrays.stream(actualType.getGenerics()).map(value -> {
                return new FieldTypeWrapperImpl(field, implClass, value);
            }).collect(Collectors.toList()).toArray(new FieldTypeWrapperImpl[0]);
        } else {
            return Arrays.stream(fieldResolvableType.getGenerics()).map(value -> {
                return new FieldTypeWrapperImpl(field, implClass, value);
            }).collect(Collectors.toList()).toArray(new FieldTypeWrapperImpl[0]);
        }
    }

    @Override
    public TypeWrapper getComponentType() {
        if (actualType != null) {
            return new FieldTypeWrapperImpl(field, this.implClass, actualType.getComponentType());
        } else {
            return new FieldTypeWrapperImpl(field, this.implClass, fieldResolvableType.getComponentType());
        }
    }

    @Override
    public Class<?> resolve() {
        if (this.actualType != null) {
            return actualType.resolve();
        } else {
            return fieldResolvableType.resolve();
        }
    }

    @Override
    public boolean emptyType() {
        if (actualType != null) {
            return actualType.emptyType();
        } else {
            return this.fieldResolvableType == ResolvableType.NONE;
        }
    }

    @Override
    public Class<?> getFieldTypeClass() {
        ResolvableType resolvableType = ResolvableType.forField(field, implClass);
        Class<?> cls = resolvableType.resolve();
        if (cls == null) {
            return field.getType();
        } else {
            return cls;
        }
    }

    @Override
    public Type getFieldGenericType() {
        return field.getGenericType();
    }

    @Override
    public TypeWrapper resolveComponentType(TypeWrapper componentType) {
        return new FieldTypeWrapperImpl(field, implClass, super.resolveComponentType(componentType));
    }

}
