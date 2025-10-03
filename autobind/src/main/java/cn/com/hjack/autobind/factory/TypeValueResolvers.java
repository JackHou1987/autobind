/**
 *
 */
package cn.com.hjack.autobind.factory;

import cn.com.hjack.autobind.FieldTypeWrapper;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.resolver.*;
import cn.com.hjack.autobind.utils.TypeUtils;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Stream;


/**
 * @ClassName: TypeValueResolvers
 * @Description: resolver 工厂
 * @author houqq
 * @date: 2025年6月17日
 *
 */
public class TypeValueResolvers {

    private static Map<Class<?>, TypeValueResolver> typeValueResovers = new ConcurrentHashMap<>();

    private static Class<?>[] dateTypes = new Class<?> [] {Date.class, Instant.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class, Calendar.class,
            ZonedDateTime.class, OffsetDateTime.class, OffsetTime.class};

    private static Class<?>[] sqlDateTypes = new Class<?> [] {java.sql.Date.class, java.sql.Time.class,
            java.sql.Timestamp.class};

    private static Class<?>[] numberTypes = new Class<?> [] {byte.class, Byte.class,
            short.class, Short.class, int.class, Integer.class, long.class,
            Long.class, float.class, Float.class, double.class, Double.class,
            BigInteger.class, BigDecimal.class, OptionalInt.class,
            OptionalDouble.class, OptionalLong.class};

    private static Class<?>[] genericTypes = new Class<?> [] {Optional.class, ThreadLocal.class,
            SoftReference.class, WeakReference.class, AtomicReference.class, Stream.class};

    private static Class<?>[] stringTypes = new Class<?> [] {String.class, char[].class,
            StringBuffer.class, StringBuilder.class, char.class, Character.class};

    private static Class<?>[] normalTypes = new Class<?> [] {UUID.class, URL.class,
            URI.class, File.class, Charset.class, Locale.class,
            ZoneId.class, Class.class, Path.class, Boolean.class, boolean.class,
            ByteBuffer.class, TimeZone.class, EnumSet.class, EnumMap.class, BitSet.class};

    private static Class<?>[] atomicTypes = new Class<?> [] {AtomicIntegerArray.class,
            AtomicLongArray.class, AtomicReferenceArray.class,
            AtomicLong.class, AtomicInteger.class, AtomicBoolean.class};

    static {
        typeValueResovers.put(Collection.class, CollectionValueResolver.instance);
        typeValueResovers.put(List.class, CollectionValueResolver.instance);
        typeValueResovers.put(ArrayList.class, CollectionValueResolver.instance);
        typeValueResovers.put(LinkedList.class, CollectionValueResolver.instance);
        typeValueResovers.put(CopyOnWriteArrayList.class, CollectionValueResolver.instance);
        typeValueResovers.put(Set.class, CollectionValueResolver.instance);
        typeValueResovers.put(SortedSet.class, CollectionValueResolver.instance);
        typeValueResovers.put(NavigableSet.class, CollectionValueResolver.instance);
        typeValueResovers.put(LinkedHashSet.class, CollectionValueResolver.instance);
        typeValueResovers.put(TreeSet.class, CollectionValueResolver.instance);
        typeValueResovers.put(HashSet.class, CollectionValueResolver.instance);
        typeValueResovers.put(CopyOnWriteArraySet.class, CollectionValueResolver.instance);
        typeValueResovers.put(Map.class, MapValueResolver.instance);
        typeValueResovers.put(SortedMap.class, MapValueResolver.instance);
        typeValueResovers.put(NavigableMap.class, MapValueResolver.instance);
        typeValueResovers.put(TreeMap.class, MapValueResolver.instance);
        typeValueResovers.put(HashMap.class, MapValueResolver.instance);
        typeValueResovers.put(LinkedHashMap.class, MapValueResolver.instance);
        typeValueResovers.put(ConcurrentHashMap.class, MapValueResolver.instance);
        for (Class<?> dateType : dateTypes) {
            typeValueResovers.put(dateType, DateValueResolver.instance);
        }
        for (Class<?> dateType : sqlDateTypes) {
            typeValueResovers.put(dateType, SqlDateValueResolver.instance);
        }
        for (Class<?> numberType : numberTypes) {
            typeValueResovers.put(numberType, NumberValueResolver.instance);
        }
        for (Class<?> genericType : genericTypes) {
            typeValueResovers.put(genericType, ParameterizedTypeValueResolver.instance);
        }
        for (Class<?> stringType : stringTypes) {
            typeValueResovers.put(stringType, StringValueResolver.instance);
        }
        for (Class<?> normalType : normalTypes) {
            typeValueResovers.put(normalType, NormalValueResolver.instance);
        }
        for (Class<?> atomicType : atomicTypes) {
            typeValueResovers.put(atomicType, AtomicValueResolver.instance);
        }
        typeValueResovers.put(Object.class, ObjectValueResolver.instance);
    }
    /**
     * @Title: getResolver
     * @Description: 获得目标Type value resolver
     * @param: targetClass
     * @return: TypeValueResolver
     */
    public static TypeValueResolver getResolver(Class<?> targetClass) {
        if (targetClass == null) {
            return null;
        }
        TypeValueResolver valueResolver = typeValueResovers.get(targetClass);
        if (valueResolver != null) {
            return valueResolver;
        }
        if (TypeUtils.isArrayClass(targetClass)) {
            return ArrayValueResolver.instance;
        } else if (TypeUtils.isEnumClass(targetClass)) {
            return EnumValueResolver.instance;
        } else if (TypeUtils.isJavaBeanClass(targetClass)) {
            return typeValueResovers.computeIfAbsent(targetClass, (key) -> {
                return JavaBeanValueResolver.instance;
            });
        } else {
            return null;
        }
    }

    /**
     * @Title: getResolver
     * @Description: 获得目标Type value resolver,如果该field type为泛型，则返回VariableValueResolver
     * @param: targetClass
     * @return: TypeValueResolver
     */
    public static TypeValueResolver getResolver(FieldTypeWrapper targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("target type can not be null");
        }
        if (targetType.getFieldGenericType() instanceof TypeVariable
                || targetType.getFieldGenericType() instanceof WildcardType) {
            return VariableValueResolver.instance;
        } else {
            Class<?> targetTypeClass = targetType.resolve();
            TypeValueResolver valueResolver = typeValueResovers.get(targetTypeClass);
            if (valueResolver != null) {
                return valueResolver;
            }
            if (TypeUtils.isArrayClass(targetTypeClass)) {
                return ArrayValueResolver.instance;
            } else if (TypeUtils.isEnumClass(targetTypeClass)) {
                return EnumValueResolver.instance;
            } else if (TypeUtils.isJavaBeanClass(targetTypeClass)) {
                return typeValueResovers.computeIfAbsent(targetTypeClass, (key) -> {
                    return JavaBeanValueResolver.instance;
                });
            } else {
                return null;
            }
        }
    }

    public static TypeValueResolver getResolver(Field field, Class<?> fieldClass) {
        if (field == null || fieldClass == null) {
            throw new IllegalArgumentException("field or class can not be null");
        }
        if (field.getGenericType() instanceof TypeVariable
                || field.getGenericType() instanceof WildcardType) {
            return VariableValueResolver.instance;
        } else {
            TypeValueResolver valueResolver = typeValueResovers.get(fieldClass);
            if (valueResolver != null) {
                return valueResolver;
            }
            if (TypeUtils.isArrayClass(fieldClass)) {
                return ArrayValueResolver.instance;
            } else if (TypeUtils.isEnumClass(fieldClass)) {
                return EnumValueResolver.instance;
            } else if (TypeUtils.isJavaBeanClass(fieldClass)) {
                return typeValueResovers.computeIfAbsent(fieldClass, (key) -> {
                    return JavaBeanValueResolver.instance;
                });
            } else {
                return null;
            }
        }
    }

}
