package cn.com.hjack.autobind.converter;

import cn.com.hjack.autobind.FieldTypeWrapper;
import cn.com.hjack.autobind.ResolvableConverter;
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
public class ResolvableConverters {

    private static Map<Class<?>, ResolvableConverter> resolvableConverters = new ConcurrentHashMap<>();

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
        resolvableConverters.put(Collection.class, CollectionConverter.instance);
        resolvableConverters.put(List.class, CollectionConverter.instance);
        resolvableConverters.put(ArrayList.class, CollectionConverter.instance);
        resolvableConverters.put(LinkedList.class, CollectionConverter.instance);
        resolvableConverters.put(CopyOnWriteArrayList.class, CollectionConverter.instance);
        resolvableConverters.put(Set.class, CollectionConverter.instance);
        resolvableConverters.put(SortedSet.class, CollectionConverter.instance);
        resolvableConverters.put(NavigableSet.class, CollectionConverter.instance);
        resolvableConverters.put(LinkedHashSet.class, CollectionConverter.instance);
        resolvableConverters.put(TreeSet.class, CollectionConverter.instance);
        resolvableConverters.put(HashSet.class, CollectionConverter.instance);
        resolvableConverters.put(CopyOnWriteArraySet.class, CollectionConverter.instance);
        resolvableConverters.put(Map.class, MapConverter.instance);
        resolvableConverters.put(SortedMap.class, MapConverter.instance);
        resolvableConverters.put(NavigableMap.class, MapConverter.instance);
        resolvableConverters.put(TreeMap.class, MapConverter.instance);
        resolvableConverters.put(HashMap.class, MapConverter.instance);
        resolvableConverters.put(LinkedHashMap.class, MapConverter.instance);
        resolvableConverters.put(ConcurrentHashMap.class, MapConverter.instance);
        for (Class<?> dateType : dateTypes) {
            resolvableConverters.put(dateType, DateConverter.instance);
        }
        for (Class<?> dateType : sqlDateTypes) {
            resolvableConverters.put(dateType, SqlDateConverter.instance);
        }
        for (Class<?> numberType : numberTypes) {
            resolvableConverters.put(numberType, NumberConverter.instance);
        }
        for (Class<?> genericType : genericTypes) {
            resolvableConverters.put(genericType, ParameterizedTypeConverter.instance);
        }
        for (Class<?> stringType : stringTypes) {
            resolvableConverters.put(stringType, StringConverter.instance);
        }
        for (Class<?> normalType : normalTypes) {
            resolvableConverters.put(normalType, NormalConverter.instance);
        }
        for (Class<?> atomicType : atomicTypes) {
            resolvableConverters.put(atomicType, AtomicConverter.instance);
        }
        resolvableConverters.put(Object.class, ObjectConverter.instance);
    }
    /**
     * @Title: getResolver
     * @Description: 获得目标Type value resolver
     * @param: targetClass
     * @return: TypeValueResolver
     */
    public static ResolvableConverter getConverter(Class<?> targetClass) {
        if (targetClass == null) {
            return null;
        }
        ResolvableConverter valueResolver = resolvableConverters.get(targetClass);
        if (valueResolver != null) {
            return valueResolver;
        }
        if (TypeUtils.isArrayClass(targetClass)) {
            return ArrayConverter.instance;
        } else if (TypeUtils.isEnumClass(targetClass)) {
            return EnumConverter.instance;
        } else if (TypeUtils.isJavaBeanClass(targetClass)) {
            return resolvableConverters.computeIfAbsent(targetClass, (key) -> {
                return JavaBeanConverter.instance;
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
    public static ResolvableConverter getConverter(FieldTypeWrapper targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("target type can not be null");
        }
        if (targetType.getFieldGenericType() instanceof TypeVariable
                || targetType.getFieldGenericType() instanceof WildcardType) {
            return VariableConverter.instance;
        } else {
            Class<?> targetTypeClass = targetType.resolve();
            ResolvableConverter valueResolver = resolvableConverters.get(targetTypeClass);
            if (valueResolver != null) {
                return valueResolver;
            }
            if (TypeUtils.isArrayClass(targetTypeClass)) {
                return ArrayConverter.instance;
            } else if (TypeUtils.isEnumClass(targetTypeClass)) {
                return EnumConverter.instance;
            } else if (TypeUtils.isJavaBeanClass(targetTypeClass)) {
                return resolvableConverters.computeIfAbsent(targetTypeClass, (key) -> {
                    return JavaBeanConverter.instance;
                });
            } else {
                return null;
            }
        }
    }

    public static ResolvableConverter getConverter(Field field, Class<?> fieldClass) {
        if (field == null || fieldClass == null) {
            throw new IllegalArgumentException("field or class can not be null");
        }
        if (field.getGenericType() instanceof TypeVariable
                || field.getGenericType() instanceof WildcardType) {
            return VariableConverter.instance;
        } else {
            ResolvableConverter valueResolver = resolvableConverters.get(fieldClass);
            if (valueResolver != null) {
                return valueResolver;
            }
            if (TypeUtils.isArrayClass(fieldClass)) {
                return ArrayConverter.instance;
            } else if (TypeUtils.isEnumClass(fieldClass)) {
                return EnumConverter.instance;
            } else if (TypeUtils.isJavaBeanClass(fieldClass)) {
                return resolvableConverters.computeIfAbsent(fieldClass, (key) -> JavaBeanConverter.instance);
            } else {
                return null;
            }
        }
    }

    public static ResolvableConverter getLazyLoadValueResolver() {
        return LazyLoadValueResolver.instance;
    }

}
