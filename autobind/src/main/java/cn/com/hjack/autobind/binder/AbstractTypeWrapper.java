package cn.com.hjack.autobind.binder;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.utils.TypeUtils;
import com.google.common.base.Strings;


/**
 * @ClassName: AbstractTypeWrapper
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月30日
 *
 */
public abstract class AbstractTypeWrapper implements TypeWrapper {

    private Map<String, TypeWrapper> variableContext = new HashMap<>();

    public AbstractTypeWrapper(Map<String, TypeWrapper> variableContext) {
        if (variableContext == null) {
            throw new IllegalArgumentException("variableContext can not be null");
        }
        this.variableContext = variableContext;
    }

    public AbstractTypeWrapper() {

    }

    @Override
    public Map<String, TypeWrapper> resolveVariableContext() {
        // 类类型字段跳过初始化
        if (getType() == null || getType() instanceof Class) {
            return new HashMap<>();
        }
        // 泛型字段跳过初始化
        Class<?> cls = resolve();
        if (cls == null) {
            return new HashMap<>();
        }
        // 获取当前类型的参数化类型数组
        TypeVariable<?>[] typeParameters = cls.getTypeParameters();
        Map<String, TypeWrapper> variableTypeNameClsMap = new HashMap<>();
        // 获取当前类型参数化类型数组
        TypeWrapper[] generics = getGenerics();
        if (generics != null && generics.length != 0 &&
                typeParameters != null && typeParameters.length != 0 &&
                typeParameters.length == generics.length) {
            for (int i = 0; i < generics.length; i++) {
                TypeWrapper genericType = getGeneric(i);
                if (genericType == null) {
                    continue;
                }
                // 如果当前字段泛型可以解析，则存储泛型变量名称和实际类型，否则尝试从父类泛型Map中查找
                if (genericType.resolve() != null) {
                    variableTypeNameClsMap.put(typeParameters[i].getTypeName(), genericType);
                } else {
                    Type type = genericType.getType();
                    if (type == null) {
                        continue;
                    }
                    TypeWrapper parentGenricType = variableContext.get(type.getTypeName());
                    if (parentGenricType != null && parentGenricType.resolve() != null) {
                        variableTypeNameClsMap.put(typeParameters[i].getTypeName(), parentGenricType);
                    } else {
                        // 解析不了，默认为Object
                        variableTypeNameClsMap.put(typeParameters[i].getTypeName(), TypeWrappers.object());
                    }
                }
            }
        }
        return variableTypeNameClsMap;
    }

    @Override
    public TypeWrapper resolveComponentType(TypeWrapper componentType) {
        if (componentType == null) {
            throw new IllegalArgumentException("component type can not be null");
        }
        while (!componentType.emptyType()) {
            if (componentType.getComponentType() == null) {
                throw new IllegalStateException("component type can not be null");
            }
            if (componentType.getComponentType().emptyType()) {
                break;
            } else {
                componentType = componentType.getComponentType();
            }
        }
        if (componentType.resolve() != null) {
            return componentType;
        } else {
            TypeWrapper actualComponentType = variableContext.get(componentType.getType().getTypeName());
            if (actualComponentType == null) {
                return TypeWrappers.object();
            } else {
                if (actualComponentType.resolve() == null) {
                    return TypeWrappers.object();
                } else {
                    return actualComponentType;
                }
            }
        }
    }

    @Override
    public TypeWrapper resolveGeneric(TypeWrapper genericType) {
        if (genericType == null) {
            throw new IllegalArgumentException("generic type can not be empty");
        }
        if (getType() instanceof Class) {
            return TypeWrappers.object();
        } else {
            return doResolveGenericType(genericType);
        }
    }

    private TypeWrapper doResolveGenericType(TypeWrapper genericType) {
        Class<?> paramterizedType = genericType.resolve();
        if (paramterizedType != null) {
            return genericType;
        }
        String typeName = genericType.getType().getTypeName();
        if (Strings.isNullOrEmpty(typeName)) {
            return TypeWrappers.object();
        } else {
            TypeWrapper actualType = variableContext.get(typeName);
            if (actualType == null) {
                return TypeWrappers.object();
            } else {
                if (actualType.resolve() != null) {
                    return actualType;
                } else {
                    return TypeWrappers.object();
                }
            }
        }
    }

    @Override
    public Class<?> resolveOrObject() {
        Class<?> resolveClass = this.resolve();
        if (resolveClass == null) {
            if (this.getType() instanceof GenericArrayType) {
                return TypeUtils.getArrayClass(Object.class,  TypeUtils.getArrayTypeDimension(getType()));
            } else {
                return Object.class;
            }
        } else {
            return resolveClass;
        }
    }

    @Override
    public Class<?> resolveOrDefault(Class<?> defaultClass) {
        Class<?> resolveClass = this.resolve();
        if (resolveClass == null) {
            return defaultClass;
        } else {
            return resolveClass;
        }
    }

    @Override
    public Class<?> resolveOrThrow() {
        Class<?> resolveClass = this.resolve();
        if (resolveClass == null) {
            throw new IllegalStateException("resolve class is null");
        } else {
            return resolveClass;
        }
    }

    protected Map<String, TypeWrapper> getVariableContext() {
        return variableContext;
    }

}
