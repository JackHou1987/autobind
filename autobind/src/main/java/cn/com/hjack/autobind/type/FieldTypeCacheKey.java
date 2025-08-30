/**
 *
 */
package cn.com.hjack.autobind.type;

import java.lang.reflect.Field;
import java.util.Map;

import cn.com.hjack.autobind.TypeWrapper;
import org.springframework.util.ObjectUtils;


/**
 * @ClassName: TypeCacheKey
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月5日
 *
 */
public class FieldTypeCacheKey {

    private Field field;

    private Class<?> implClass;

    private Map<String, TypeWrapper> variableContext;

    private TypeWrapper ownerType;

    private TypeWrapper actualType;

    private FieldTypeCacheKey(Builder builder) {
        this.field = builder.field;
        this.implClass = builder.implClass;
        this.variableContext = builder.variableContext;
        this.actualType = builder.actualType;
        this.ownerType = builder.ownerType;
    }

    public static class Builder {

        private Builder() {

        }

        private Field field;

        private Class<?> implClass;

        private Map<String, TypeWrapper> variableContext;

        private TypeWrapper ownerType;

        private TypeWrapper actualType;

        public Builder field(Field field) {
            this.field = field;
            return this;
        }

        public Builder implClass(Class<?> implClass) {
            this.implClass = implClass;
            return this;
        }

        public Builder variableContext(Map<String, TypeWrapper> variableContext) {
            this.variableContext = variableContext;
            return this;
        }

        public Builder actualType(TypeWrapper actualType) {
            this.actualType = actualType;
            return this;
        }

        public Builder ownerType(TypeWrapper ownerType) {
            this.ownerType = ownerType;
            return this;
        }

        public FieldTypeCacheKey build() {
            return new FieldTypeCacheKey(this);
        }

        public static Builder create() {
            return new Builder();
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (this.field != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(field);
        }
        if (this.implClass != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.implClass);
        }
        if (this.actualType != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(actualType);
        }
        if (this.ownerType != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(ownerType);
        }
        if (this.variableContext != null) {
            hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(variableContext);
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FieldTypeCacheKey)) {
            return false;
        }

        FieldTypeCacheKey otherType = (FieldTypeCacheKey) other;
        if (this.field != otherType.field && !ObjectUtils.nullSafeEquals(this.field, otherType.field)) {
            return false;
        }
        if (this.implClass != otherType.implClass && !ObjectUtils.nullSafeEquals(implClass, otherType.implClass)) {
            return false;
        }
        if (this.ownerType != otherType.ownerType && !ObjectUtils.nullSafeEquals(ownerType, otherType.ownerType)) {
            return false;
        }
        if (this.actualType != otherType.actualType && !ObjectUtils.nullSafeEquals(actualType, otherType.actualType)) {
            return false;
        }
        if (this.variableContext != otherType.variableContext && !ObjectUtils.nullSafeEquals(variableContext, otherType.variableContext)) {
            return false;
        }
        return true;
    }
}
