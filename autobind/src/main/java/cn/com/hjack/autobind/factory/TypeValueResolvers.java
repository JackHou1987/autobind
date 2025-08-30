package cn.com.hjack.autobind.factory;

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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.hjack.autobind.FieldTypeWrapper;
import cn.com.hjack.autobind.TypeValueResolver;
import cn.com.hjack.autobind.resolver.*;
import cn.com.hjack.autobind.utils.TypeUtils;
import com.google.common.util.concurrent.AtomicDouble;


/**
 * @ClassName: ValueResolverFactory
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月17日
 *
 */
public class TypeValueResolvers {

    private static Map<Class<?>, TypeValueResolver> typeValueResovers = new ConcurrentHashMap<>();

    private static Class<?>[] dateTypes = new Class<?> [] {Date.class, Instant.class,
            LocalDate.class, LocalTime.class, LocalDateTime.class, Calendar.class,
            ZonedDateTime.class, OffsetDateTime.class, OffsetTime.class};

    private static Class<?>[] numberTypes = new Class<?> [] {byte.class, Byte.class,
            short.class, Short.class, int.class, Integer.class, long.class,
            Long.class, float.class, Float.class, double.class, Double.class,
            BigInteger.class, BigDecimal.class, AtomicInteger.class,
            AtomicLong.class, AtomicDouble.class, OptionalInt.class,
            OptionalDouble.class, OptionalLong.class};

    private static Class<?>[] genericTypes = new Class<?> [] {Optional.class, ThreadLocal.class,
            SoftReference.class, WeakReference.class, AtomicReference.class};

    private static Class<?>[] stringTypes = new Class<?> [] {String.class, char[].class,
            StringBuffer.class, StringBuilder.class, char.class, Character.class};

    private static Class<?>[] normalTypes = new Class<?> [] {UUID.class, URL.class,
            URI.class, File.class, Charset.class, Locale.class, AtomicBoolean.class,
            ZoneId.class, Class.class, Path.class, Boolean.class, boolean.class, ByteBuffer.class, TimeZone.class};

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
        typeValueResovers.put(EnumSet.class, CollectionValueResolver.instance);
        typeValueResovers.put(CopyOnWriteArraySet.class, CollectionValueResolver.instance);
        typeValueResovers.put(Map.class, MapValueResolver.instance);
        typeValueResovers.put(SortedMap.class, MapValueResolver.instance);
        typeValueResovers.put(NavigableMap.class, MapValueResolver.instance);
        typeValueResovers.put(TreeMap.class, MapValueResolver.instance);
        typeValueResovers.put(HashMap.class, MapValueResolver.instance);
        typeValueResovers.put(LinkedHashMap.class, MapValueResolver.instance);
        typeValueResovers.put(EnumMap.class, MapValueResolver.instance);
        typeValueResovers.put(ConcurrentHashMap.class, MapValueResolver.instance);
        for (Class<?> dateType : dateTypes) {
            typeValueResovers.put(dateType, DateValueResolver.instance);
        }
        for (Class<?> numberType : numberTypes) {
            typeValueResovers.put(numberType, NumberValueResolver.instance);
        }
        for (Class<?> genericType : genericTypes) {
            typeValueResovers.put(genericType, GenericTypeValueResolver.instance);
        }
        for (Class<?> stringType : stringTypes) {
            typeValueResovers.put(stringType, StringValueResolver.instance);
        }
        typeValueResovers.put(Object.class, ObjectValueResolver.instance);
    }

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
        } else if (TypeUtils.isJavaBeanClass(targetClass)) {
            return typeValueResovers.computeIfAbsent(targetClass, (key) -> {
                return JavaBeanValueResolver.instance;
            });
        } else {
            return null;
        }
    }

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
            Class<?> targetTypeClass = fieldClass;
            TypeValueResolver valueResolver = typeValueResovers.get(targetTypeClass);
            if (valueResolver != null) {
                return valueResolver;
            }
            if (TypeUtils.isArrayClass(targetTypeClass)) {
                return ArrayValueResolver.instance;
            } else if (TypeUtils.isJavaBeanClass(targetTypeClass)) {
                return typeValueResovers.computeIfAbsent(targetTypeClass, (key) -> {
                    return JavaBeanValueResolver.instance;
                });
            } else {
                return null;
            }
        }
    }

}
