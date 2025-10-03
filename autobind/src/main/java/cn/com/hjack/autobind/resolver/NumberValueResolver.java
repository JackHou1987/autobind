/**
 *
 */
package cn.com.hjack.autobind.resolver;


import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.validation.DefaultResult;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;


/**
 * @ClassName: NumberValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年7月11日
 *
 */
public class NumberValueResolver extends AbstractTypeValueResolver {

    public static NumberValueResolver instance = new NumberValueResolver();

    private NumberValueResolver() {
        super.registerInternalConverter(Number.class, Byte.class, (value, config) -> value.byteValue());
        super.registerInternalConverter(String.class, Byte.class, (value, config) -> Byte.parseByte(value));
        super.registerInternalConverter(Number.class, byte.class, (value, config) -> {
            return value.byteValue();
        });
        super.registerInternalConverter(String.class, byte.class, (value, config) -> {
            return Byte.parseByte(value);
        });
        super.registerInternalConverter(OptionalInt.class, byte.class, (value, config) -> {
            return (byte) value.orElse(0);
        });
        super.registerInternalConverter(OptionalInt.class, Byte.class, (value, config) -> {
            return (byte) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, byte.class, (value, config) -> {
            return (byte) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, Byte.class, (value, config) -> {
            return (byte) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, byte.class, (value, config) -> {
            return (byte) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, Byte.class, (value, config) -> {
            return (byte) value.orElse(0);
        });
        super.registerInternalConverter(Number.class, Short.class, (value, config) -> {
            return value.shortValue();
        });
        super.registerInternalConverter(String.class, Short.class, (value, config) -> {
            return Short.parseShort(value);
        });
        super.registerInternalConverter(Number.class, short.class, (value, config) -> {
            return value.shortValue();
        });
        super.registerInternalConverter(String.class, short.class, (value, config) -> {
            return Short.parseShort(value);
        });
        super.registerInternalConverter(OptionalInt.class, Short.class, (value, config) -> {
            return (short) value.orElse(0);
        });
        super.registerInternalConverter(OptionalInt.class, short.class, (value, config) -> {
            return (short) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, Short.class, (value, config) -> {
            return (short) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, short.class, (value, config) -> {
            return (short) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, short.class, (value, config) -> {
            return (short) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, Short.class, (value, config) -> {
            return (short) value.orElse(0);
        });
        super.registerInternalConverter(Number.class, int.class, (value, config) -> {
            return value.intValue();
        });
        super.registerInternalConverter(String.class, int.class, (value, config) -> {
            return Integer.parseInt(value);
        });
        super.registerInternalConverter(Number.class, Integer.class, (value, config) -> {
            return value.intValue();
        });
        super.registerInternalConverter(String.class, Integer.class, (value, config) -> {
            return Integer.parseInt(value);
        });
        super.registerInternalConverter(OptionalInt.class, int.class, (value, config) -> {
            return (int) value.orElse(0);
        });
        super.registerInternalConverter(OptionalInt.class, Integer.class, (value, config) -> {
            return (int) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, int.class, (value, config) -> {
            return (int) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, Integer.class, (value, config) -> {
            return (int) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, int.class, (value, config) -> {
            return (int) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, Integer.class, (value, config) -> {
            return (int) value.orElse(0);
        });
        super.registerInternalConverter(Number.class, long.class, (value, config) -> {
            return value.longValue();
        });
        super.registerInternalConverter(String.class, long.class, (value, config) -> {
            return Long.parseLong(value);
        });
        super.registerInternalConverter(Number.class, Long.class, (value, config) -> {
            return value.longValue();
        });
        super.registerInternalConverter(String.class, Long.class, (value, config) -> {
            return Long.parseLong(value);
        });
        super.registerInternalConverter(OptionalInt.class, long.class, (value, config) -> {
            return (long) value.orElse(0);
        });
        super.registerInternalConverter(OptionalInt.class, Long.class, (value, config) -> {
            return (long) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, long.class, (value, config) -> {
            return (long) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, Long.class, (value, config) -> {
            return (long) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, long.class, (value, config) -> {
            return (long) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, Long.class, (value, config) -> {
            return (long) value.orElse(0);
        });
        super.registerInternalConverter(Number.class, float.class, (value, config) -> {
            if (config != null && config.scale() > 0) {
                BigDecimal decimal = BigDecimal.valueOf(value.doubleValue());
                return decimal.setScale(config.scale(), Optional.ofNullable(config.roundingMode()).orElse(RoundingMode.HALF_UP)).floatValue();
            } else {
                return value.floatValue();
            }
        });
        super.registerInternalConverter(String.class, float.class, (value, config) -> {
            return Float.parseFloat(value);
        });
        super.registerInternalConverter(Number.class, Float.class, (value, config) -> {
            if (config != null && config.scale() > 0) {
                BigDecimal decimal = BigDecimal.valueOf(value.doubleValue());
                return decimal.setScale(config.scale(), Optional.ofNullable(config.roundingMode()).orElse(RoundingMode.HALF_UP)).floatValue();
            } else {
                return value.floatValue();
            }
        });
        super.registerInternalConverter(String.class, Float.class, (value, config) -> {
            return Float.parseFloat(value);
        });
        super.registerInternalConverter(OptionalInt.class, float.class, (value, config) -> {
            return (float) value.orElse(0);
        });
        super.registerInternalConverter(OptionalInt.class, Float.class, (value, config) -> {
            return (float) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, float.class, (value, config) -> {
            return (float) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, Float.class, (value, config) -> {
            return (float) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, float.class, (value, config) -> {
            return (float) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, Float.class, (value, config) -> {
            return (float) value.orElse(0);
        });
        super.registerInternalConverter(Number.class, double.class, (value, config) -> {
            return value.doubleValue();
        });
        super.registerInternalConverter(String.class, double.class, (value, config) -> {
            return Double.parseDouble(value);
        });
        super.registerInternalConverter(Number.class, Double.class, (value, config) -> {
            return value.doubleValue();
        });
        super.registerInternalConverter(String.class, Double.class, (value, config) -> {
            return Double.parseDouble(value);
        });
        super.registerInternalConverter(OptionalInt.class, double.class, (value, config) -> {
            return (double) value.orElse(0);
        });
        super.registerInternalConverter(OptionalInt.class, Double.class, (value, config) -> {
            return (double) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, double.class, (value, config) -> {
            return (double) value.orElse(0);
        });
        super.registerInternalConverter(OptionalLong.class, Double.class, (value, config) -> {
            return (double) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, double.class, (value, config) -> {
            return (double) value.orElse(0);
        });
        super.registerInternalConverter(OptionalDouble.class, Double.class, (value, config) -> {
            return (double) value.orElse(0);
        });
        super.registerInternalConverter(Number.class, BigInteger.class, (value, config) -> {
            return BigInteger.valueOf(value.longValue());
        });
        super.registerInternalConverter(String.class, BigInteger.class, (value, config) -> new BigInteger(value));
        super.registerInternalConverter(OptionalInt.class, BigInteger.class, (value, config) -> {
            return BigInteger.valueOf(value.orElse(0));
        });
        super.registerInternalConverter(OptionalLong.class, BigInteger.class, (value, config) -> {
            return BigInteger.valueOf(value.orElse(0));
        });
        super.registerInternalConverter(OptionalDouble.class, BigInteger.class, (value, config) -> {
            return BigInteger.valueOf((long) value.orElse(0));
        });
        super.registerInternalConverter(Number.class, BigDecimal.class, (value, config) -> {
            return BigDecimal.valueOf(value.doubleValue());
        });
        super.registerInternalConverter(String.class, BigDecimal.class, (value, config) -> {
            return new BigDecimal(value);
        });
        super.registerInternalConverter(OptionalInt.class, BigDecimal.class, (value, config) -> {
            return BigDecimal.valueOf(value.orElse(0));
        });
        super.registerInternalConverter(OptionalLong.class, BigDecimal.class, (value, config) -> {
            return BigDecimal.valueOf(value.orElse(0));
        });
        super.registerInternalConverter(OptionalDouble.class, BigDecimal.class, (value, config) -> {
            return BigDecimal.valueOf(value.orElse(0));
        });
        super.registerInternalConverter(String.class, OptionalInt.class, (value, config) -> {
            return OptionalInt.of(Integer.parseInt(value));
        });
        super.registerInternalConverter(Number.class, OptionalInt.class, (value, config) -> {
            return OptionalInt.of(value.intValue());
        });
        super.registerInternalConverter(OptionalLong.class, OptionalInt.class, (value, config) -> {
            return OptionalInt.of((int) value.orElse(0));
        });
        super.registerInternalConverter(OptionalDouble.class, OptionalInt.class, (value, config) -> {
            return OptionalInt.of((int)value.orElse(0));
        });
        super.registerInternalConverter(OptionalInt.class, OptionalInt.class, (value, config) -> {
            return value;
        });
        super.registerInternalConverter(String.class, OptionalLong.class, (value, config) -> {
            return OptionalLong.of(Long.parseLong(value));
        });
        super.registerInternalConverter(Number.class, OptionalLong.class, (value, config) -> {
            return OptionalLong.of(value.longValue());
        });
        super.registerInternalConverter(OptionalInt.class, OptionalLong.class, (value, config) -> {
            return OptionalLong.of(value.orElse(0));
        });
        super.registerInternalConverter(OptionalDouble.class, OptionalLong.class, (value, config) -> {
            return OptionalLong.of((long) value.orElse(0));
        });
        super.registerInternalConverter(OptionalLong.class, OptionalLong.class, (value, config) -> {
            return value;
        });
        super.registerInternalConverter(String.class, OptionalDouble.class, (value, config) -> {
            return OptionalDouble.of(Double.parseDouble(value));
        });
        super.registerInternalConverter(Number.class, OptionalDouble.class, (value, config) -> {
            return OptionalDouble.of(value.doubleValue());
        });
        super.registerInternalConverter(OptionalLong.class, OptionalDouble.class, (value, config) -> {
            return OptionalDouble.of(value.orElse(0));
        });
        super.registerInternalConverter(OptionalInt.class, OptionalDouble.class, (value, config) -> {
            return OptionalDouble.of(value.orElse(0));
        });
        super.registerInternalConverter(OptionalDouble.class, OptionalDouble.class, (value, config) -> {
            return value;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> Result<T> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config) throws Exception {
        if (targetType == null || targetType.resolve() == null) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "source or target type can not be null");
        }
        Class<?> targetCls = targetType.resolve();
        DefaultResult<T> result = new DefaultResult<>();
        try {
            if (source instanceof Number) {
                InternalConverter<Number, ResolveConfig, Object> function = (InternalConverter<Number, ResolveConfig, Object>) super.getInternalConverter(Number.class, targetCls);
                if (function == null) {
                    throw new IllegalStateException("can not convert source to number");
                }
                Object target = function.convert((Number) source, config);
                result.setInstance(CastUtils.castSafe(target));
            } else if (source instanceof String) {
                InternalConverter<String, ResolveConfig, Object> function = (InternalConverter<String, ResolveConfig, Object>) super.getInternalConverter(String.class, targetCls);
                if (function == null) {
                    throw new IllegalStateException("can not convert source to number");
                }
                Object target = function.convert((String) source, config);
                result.setInstance(CastUtils.castSafe(target));
            } else {
                throw new IllegalStateException("can not convert source to number");
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setResultCode(Constants.FAIL_CODE);
            result.setResultMsg(e.getMessage());
        }
        return result;
    }

}
