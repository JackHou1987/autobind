/**
 *
 */
package cn.com.hjack.autobind.binder;

import java.lang.reflect.Type;
import java.util.Arrays;

import cn.com.hjack.autobind.TypeWrapper;
import org.springframework.core.ResolvableType;


/**
 * @ClassName: TypeWrapperImpl
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月23日
 *
 */
public class TypeWrapperImpl extends AbstractTypeWrapper {

    private ResolvableType resolvableType;

    public TypeWrapperImpl() {
        resolvableType = ResolvableType.NONE;
    }

    public TypeWrapperImpl(Class<?> baseClass, Class<?> implClass) {
        if (baseClass == null || implClass == null) {
            throw new IllegalArgumentException("base type or impl type can not be null");
        }
        this.resolvableType = ResolvableType.forClass(baseClass, implClass);
    }

    public TypeWrapperImpl(Class<?> baseClass) {
        if (baseClass == null) {
            throw new IllegalArgumentException("base type can not be null");
        }
        this.resolvableType = ResolvableType.forClass(baseClass);
    }


    private TypeWrapperImpl(ResolvableType resolvableType) {
        if (resolvableType == null) {
            throw new IllegalArgumentException("resolvable type can not be null");
        }
        this.resolvableType = resolvableType;
    }


    @Override
    public Type getType() {
        return resolvableType.getType();
    }

    @Override
    public TypeWrapper getGeneric(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("illegal argument index");
        }
        return new TypeWrapperImpl(this.resolvableType.getGeneric(index));
    }

    @Override
    public TypeWrapper[] getGenerics() {
        return Arrays.stream(resolvableType.getGenerics()).map(TypeWrapperImpl::new).toArray(TypeWrapperImpl[]::new);
    }

    @Override
    public Class<?> resolve() {
        return resolvableType.resolve();
    }

    @Override
    public TypeWrapper getComponentType() {
        return new TypeWrapperImpl(resolvableType.getComponentType());
    }

    @Override
    public boolean emptyType() {
        return resolvableType == ResolvableType.NONE;
    }

}
