package cn.com.hjack.autobind.converter;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.utils.CastUtils;
import com.google.common.base.Strings;


/**
 *   数值类型转换器
 * @author houqq
 * @date: 2025年7月11日
 */
public class NumberValueResolver extends AbstractResolvableConverter {

    public static NumberValueResolver instance = new NumberValueResolver();

    private NumberValueResolver() {
        registerInternalConverter(Number.class, Byte.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return value.byteValue();
            }
        });
        registerInternalConverter(String.class, Byte.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            } else {
                return Byte.parseByte(value);
            }

        });
        registerInternalConverter(OptionalInt.class, Byte.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (byte) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalLong.class, Byte.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (byte) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalDouble.class, Byte.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (byte) value.orElse(0);
            }
        });
        registerInternalConverter(Number.class, Short.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return value.shortValue();
            }
        });
        registerInternalConverter(String.class, Short.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            } else {
                return Short.parseShort(value);
            }
        });
        registerInternalConverter(OptionalInt.class, Short.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (short) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalLong.class, Short.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (short) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalDouble.class, Short.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (short) value.orElse(0);
            }
        });
        registerInternalConverter(Number.class, int.class, (value, config) -> {
            if (value == null) {
                return 0;
            } else {
                return value.intValue();
            }
        });
        registerInternalConverter(String.class, int.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return 0;
            } else {
                return Integer.parseInt(value);
            }
        });
        registerInternalConverter(Number.class, Integer.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return value.intValue();
            }
        });
        registerInternalConverter(String.class, Integer.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            } else {
                return Integer.parseInt(value);
            }
        });
        registerInternalConverter(OptionalInt.class, int.class, (value, config) -> {
            if (value == null) {
                return 0;
            } else {
                return value.orElse(0);
            }
        });
        registerInternalConverter(OptionalInt.class, Integer.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return value.orElse(0);
            }
        });
        registerInternalConverter(OptionalLong.class, int.class, (value, config) -> {
            if (value == null) {
                return 0;
            } else {
                return (int) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalLong.class, Integer.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (int) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalDouble.class, int.class, (value, config) -> {
            if (value == null) {
                return 0;
            } else {
                return (int) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalDouble.class, Integer.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (int) value.orElse(0);
            }
        });
        registerInternalConverter(Number.class, long.class, (value, config) -> {
            if (value == null) {
                return 0L;
            } else {
                return value.longValue();
            }
        });
        registerInternalConverter(String.class, long.class, (value, config) -> {
            if (value == null) {
                return 0L;
            } else {
                return Long.parseLong(value);
            }
        });
        registerInternalConverter(Number.class, Long.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return value.longValue();
            }
        });
        registerInternalConverter(String.class, Long.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            } else {
                return Long.parseLong(value);
            }
        });
        registerInternalConverter(OptionalInt.class, long.class, (value, config) -> {
            if (value == null) {
                return 0L;
            } else {
                return (long) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalInt.class, Long.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (long) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalLong.class, long.class, (value, config) -> {
            if (value == null) {
                return 0L;
            } else {
                return (long) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalLong.class, Long.class, (value, config) -> {
            if (value == null) {
                return 0L;
            } else {
                return (long) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalDouble.class, long.class, (value, config) -> {
            if (value == null) {
                return 0L;
            } else {
                return (long) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalDouble.class, Long.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (long) value.orElse(0);
            }
        });
        registerInternalConverter(Number.class, float.class, (value, config) -> {
            if (value == null) {
                return 0f;
            } else {
                if (config != null && config.scale() > 0) {
                    BigDecimal decimal = BigDecimal.valueOf(value.doubleValue());
                    return decimal.setScale(config.scale(), Optional.ofNullable(config.roundingMode()).orElse(RoundingMode.HALF_UP)).floatValue();
                } else {
                    return value.floatValue();
                }
            }
        });
        registerInternalConverter(String.class, float.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return 0f;
            } else {
                return Float.parseFloat(value);
            }
        });
        registerInternalConverter(Number.class, Float.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                if (config != null && config.scale() > 0) {
                    BigDecimal decimal = BigDecimal.valueOf(value.doubleValue());
                    return decimal.setScale(config.scale(), Optional.ofNullable(config.roundingMode()).orElse(RoundingMode.HALF_UP)).floatValue();
                } else {
                    return value.floatValue();
                }
            }
        });
        registerInternalConverter(String.class, Float.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            } else {
                return Float.parseFloat(value);
            }
        });
        registerInternalConverter(OptionalInt.class, float.class, (value, config) -> {
            if (value == null) {
                return 0f;
            } else {
                return (float) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalInt.class, Float.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (float) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalLong.class, float.class, (value, config) -> {
            if (value == null) {
                return 0f;
            } else {
                return (float) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalLong.class, Float.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (float) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalDouble.class, float.class, (value, config) -> {
            if (value == null) {
                return 0f;
            } else {
                return (float) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalDouble.class, Float.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (float) value.orElse(0);
            }
        });
        registerInternalConverter(Number.class, double.class, (value, config) -> {
            if (value == null) {
                return 0d;
            } else {
                return value.doubleValue();
            }
        });
        registerInternalConverter(String.class, double.class, (value, config) -> {
            if (value == null) {
                return 0d;
            } else {
                return Double.parseDouble(value);
            }
        });
        registerInternalConverter(Number.class, Double.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return value.doubleValue();
            }
        });
        registerInternalConverter(String.class, Double.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            } else {
                return Double.parseDouble(value);
            }
        });
        registerInternalConverter(OptionalInt.class, double.class, (value, config) -> {
            if (value == null) {
                return 0d;
            } else {
                return (double) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalInt.class, Double.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (double) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalLong.class, double.class, (value, config) -> {
            if (value == null) {
                return 0d;
            } else {
                return (double) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalLong.class, Double.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (double) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalDouble.class, double.class, (value, config) -> {
            if (value == null) {
                return 0d;
            } else {
                return (double) value.orElse(0);
            }
        });
        registerInternalConverter(OptionalDouble.class, Double.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return (double) value.orElse(0);
            }
        });
        registerInternalConverter(Number.class, BigInteger.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return BigInteger.valueOf(value.longValue());
            }
        });
        registerInternalConverter(String.class, BigInteger.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            } else {
                return new BigInteger(value);
            }
        });
        registerInternalConverter(OptionalInt.class, BigInteger.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return BigInteger.valueOf(value.orElse(0));
            }
        });
        registerInternalConverter(OptionalLong.class, BigInteger.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return BigInteger.valueOf(value.orElse(0));
            }
        });
        registerInternalConverter(OptionalDouble.class, BigInteger.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return BigInteger.valueOf((long) value.orElse(0));
            }
        });
        registerInternalConverter(Number.class, BigDecimal.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return BigDecimal.valueOf(value.doubleValue());
            }
        });
        registerInternalConverter(String.class, BigDecimal.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            } else {
                return new BigDecimal(value);
            }
        });
        registerInternalConverter(OptionalInt.class, BigDecimal.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return BigDecimal.valueOf(value.orElse(0));
            }
        });
        registerInternalConverter(OptionalLong.class, BigDecimal.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return BigDecimal.valueOf(value.orElse(0));
            }
        });
        registerInternalConverter(OptionalDouble.class, BigDecimal.class, (value, config) -> {
            if (value == null) {
                return null;
            } else {
                return BigDecimal.valueOf(value.orElse(0));
            }
        });
        registerInternalConverter(String.class, OptionalInt.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return OptionalInt.empty();
            } else {
                return OptionalInt.of(Integer.parseInt(value));
            }
        });
        registerInternalConverter(Number.class, OptionalInt.class, (value, config) -> {
            if (value == null) {
                return OptionalInt.empty();
            } else {
                return OptionalInt.of(value.intValue());
            }
        });
        registerInternalConverter(OptionalLong.class, OptionalInt.class, (value, config) -> {
            if (value == null) {
                return OptionalInt.empty();
            } else {
                return OptionalInt.of((int) value.orElse(0));
            }
        });
        registerInternalConverter(OptionalDouble.class, OptionalInt.class, (value, config) -> {
            if (value == null) {
                return OptionalInt.empty();
            } else {
                return OptionalInt.of((int) value.orElse(0));
            }
        });
        registerInternalConverter(OptionalInt.class, OptionalInt.class, (value, config) -> {
            if (value == null) {
                return OptionalInt.empty();
            } else {
                return value;
            }
        });
        registerInternalConverter(String.class, OptionalLong.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return OptionalLong.empty();
            } else {
                return OptionalLong.of(Long.parseLong(value));
            }
        });
        registerInternalConverter(Number.class, OptionalLong.class, (value, config) -> {
            if (value == null) {
                return OptionalLong.empty();
            } else {
                return OptionalLong.of(value.longValue());
            }
        });
        registerInternalConverter(OptionalInt.class, OptionalLong.class, (value, config) -> {
            if (value == null) {
                return OptionalLong.empty();
            } else {
                return OptionalLong.of(value.orElse(0));
            }
        });
        registerInternalConverter(OptionalDouble.class, OptionalLong.class, (value, config) -> {
            if (value == null) {
                return OptionalLong.empty();
            } else {
                return OptionalLong.of((long) value.orElse(0));
            }
        });
        registerInternalConverter(OptionalLong.class, OptionalLong.class, (value, config) -> {
            if (value == null) {
                return OptionalLong.empty();
            } else {
                return value;
            }
        });
        registerInternalConverter(String.class, OptionalDouble.class, (value, config) -> {
            if (Strings.isNullOrEmpty(value)) {
                return OptionalDouble.empty();
            } else {
                return OptionalDouble.of(Double.parseDouble(value));
            }
        });
        registerInternalConverter(Number.class, OptionalDouble.class, (value, config) -> {
            if (value == null) {
                return OptionalDouble.empty();
            } else {
                return OptionalDouble.of(value.doubleValue());
            }
        });
        registerInternalConverter(OptionalLong.class, OptionalDouble.class, (value, config) -> {
            if (value == null) {
                return OptionalDouble.empty();
            } else {
                return OptionalDouble.of(value.orElse(0));
            }
        });
        registerInternalConverter(OptionalInt.class, OptionalDouble.class, (value, config) -> {
            if (value == null) {
                return OptionalDouble.empty();
            } else {
                return OptionalDouble.of(value.orElse(0));
            }
        });
        registerInternalConverter(OptionalDouble.class, OptionalDouble.class, (value, config) -> {
            if (value == null) {
                return OptionalDouble.empty();
            } else {
                return value;
            }
        });
    }

    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        Class<?> targetCls = targetType.resolve();
        try {
            if (source instanceof Number) {
                InternalConverter<Number, ResolveConfig, Object> converter = CastUtils.castSafe(getInternalConverter(Number.class, targetCls));
                if (converter == null) {
                    throw new IllegalStateException("can not convert source to number");
                }
                Object target = converter.convert((Number) source, config);
                return DefaultResult.successResult(CastUtils.castSafe(target));
            } else if (source instanceof String) {
                InternalConverter<String, ResolveConfig, Object> converter = CastUtils.castSafe(getInternalConverter(String.class, targetCls));
                if (converter == null) {
                    throw new IllegalStateException("can not convert source to number");
                }
                Object target = converter.convert((String) source, config);
                return DefaultResult.successResult(CastUtils.castSafe(target));
            } else {
                InternalConverter<Object, ResolveConfig, Object> converter = CastUtils.castSafe(getInternalConverter(source.getClass(), targetCls));
                if (converter == null) {
                    throw new IllegalStateException("can not convert source to number");
                } else {
                    Object target = converter.convert(source, config);
                    return DefaultResult.successResult(CastUtils.castSafe(target));
                }
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(e.getMessage());
        }
    }

}
