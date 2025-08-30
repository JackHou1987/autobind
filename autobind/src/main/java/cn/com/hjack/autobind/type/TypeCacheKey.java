/**
 *
 */
package cn.com.hjack.autobind.type;

import java.lang.reflect.Type;

import org.springframework.util.ObjectUtils;

/**
 * @ClassName: TypeCacheKey
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月13日
 *
 */
public class TypeCacheKey {

    private Type type;

    private Class<?> baseClass;

    private Class<?> implClass;

    public TypeCacheKey() {
    }

    public TypeCacheKey(Type type) {
        this.type = type;
    }

    public TypeCacheKey(Class<?> baseClass, Class<?> implClass) {
        this.baseClass = baseClass;
        this.implClass = implClass;
    }

    public TypeCacheKey(Class<?> baseClass) {
        this.baseClass = baseClass;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;

        if (this.implClass != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.implClass);
        }
        if (this.baseClass != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(baseClass);
        }
        if (this.type != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(type);
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TypeCacheKey)) {
            return false;
        }

        TypeCacheKey otherType = (TypeCacheKey) other;
        if (this.baseClass != otherType.baseClass && !ObjectUtils.nullSafeEquals(this.baseClass, otherType.baseClass)) {
            return false;
        }
        if (this.implClass != otherType.implClass && !ObjectUtils.nullSafeEquals(implClass, otherType.implClass)) {
            return false;
        }
        if (this.type != otherType.type && !ObjectUtils.nullSafeEquals(type, otherType.type)) {
            return false;
        }
        return true;
    }
}
