/**
 *
 */
package cn.com.hjack.autobind.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import cn.com.hjack.autobind.AutoBindField;
import org.springframework.util.StringUtils;


/**
 * @ClassName: CastUtils
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月28日
 *
 */
public class CastUtils {

    public static Object formatDate(Object value, AutoBindField autoBind) {
        if (autoBind == null || value == null) {
            return value;
        }
        if (StringUtils.isEmpty(autoBind.format())) {
            return value;
        }
        if (!(value instanceof Date)) {
            return value;
        }
        SimpleDateFormat formattter = new SimpleDateFormat(autoBind.format());
        return formattter.format((Date) value);
    }

    public static Object formatDate(Object value, String format) {
        if (StringUtils.isEmpty(format) || value == null) {
            return value;
        }
        if (!(value instanceof Date)) {
            return value;
        }
        SimpleDateFormat formattter = new SimpleDateFormat(format);
        return formattter.format((Date) value);
    }

    public static Date parseDateTime(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        } else {
            String[] patterns = new String[] {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyyMMdd", "yyyyMMddHHmmss", "HHmmss", "HH:mm:ss"};
            for (String pattern : patterns) {
                SimpleDateFormat formattter = new SimpleDateFormat(pattern);
                Date date = null;
                try {
                    date = formattter.parse(str);
                    if (date != null) {
                        return date;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            throw new IllegalStateException("can not convert str to date");
        }
    }

    public static int toPrimIntegerValue(Integer value) {
        if (value == null) {
            return 0;
        } else {
            return value;
        }
    }


    public static Integer toWrapIntegerValue(int value) {
        return value;
    }

    public static int[] toPrimIntegerArrayValue(Integer[] value) {
        if (value == null) {
            return new int[0];
        } else {
            int[] intArray = new int[value.length];
            for (int i = 0; i < value.length; ++i) {
                if (value[i] == null) {
                    intArray[i] = 0;
                } else {
                    intArray[i] = value[i];
                }
            }
            return intArray;
        }
    }

    public static Integer[] toWrapIntegerArrayValue(int[] value) {
        if (value == null) {
            return new Integer[0];
        } else {
            Integer[] intArray = new Integer[value.length];
            for (int i = 0; i < value.length; ++i) {
                intArray[i] = value[i];
            }
            return intArray;
        }
    }

    public static byte toPrimByteValue(Byte value) {
        if (value == null) {
            return 0;
        } else {
            return value;
        }
    }

    public static Byte toWrapByteValue(byte value) {
        return value;
    }

    public static byte[] toPrimByteArrayValue(Byte[] value) {
        if (value == null) {
            return new byte[0];
        } else {
            byte[] bytes = new byte[value.length];
            for (int i = 0; i < value.length; ++i) {
                if (value[i] == null) {
                    bytes[i] = 0;
                } else {
                    bytes[i] = value[i];
                }
            }
            return bytes;
        }
    }

    public static Byte[] toWrapByteArrayValue(byte[] value) {
        if (value == null) {
            return new Byte[0];
        } else {
            Byte[] bytes = new Byte[value.length];
            for (int i = 0; i < value.length; ++i) {
                bytes[i] = value[i];
            }
            return bytes;
        }
    }

    public static char toPrimCharValue(Character value) {
        if (value == null) {
            return 0;
        } else {
            return value;
        }
    }

    public static Character toWrapCharValue(char value) {
        return value;
    }

    public static short toPrimShortValue(Short value) {
        if (value == null) {
            return 0;
        } else {
            return value;
        }
    }

    public static Short toWrapShortValue(short value) {
        return value;
    }

    public static long toPrimLongValue(Long value) {
        if (value == null) {
            return 0;
        } else {
            return value;
        }
    }

    public static Long toWrapLongValue(long value) {
        return value;
    }

    public static boolean toPrimBooleanValue(Boolean value) {
        if (value == null) {
            return false;
        } else {
            return value;
        }
    }

    public static Boolean toWrapBooleanValue(boolean value) {
        return value;
    }

    public static double toPrimDoubleValue(Double value) {
        if (value == null) {
            return 0d;
        } else {
            return value;
        }
    }

    public static Double toWrapDoubleValue(double value) {
        return value;
    }

    public static float toPrimFloatValue(Float value) {
        if (value == null) {
            return 0f;
        } else {
            return value;
        }
    }

    public static Float toWrapFloatValue(float value) {
        return value;
    }

    public static Object setNumberScale(Object value, AutoBindField autoBind) {
        if (autoBind == null || value == null) {
            return value;
        }
        if (autoBind.scale() > 0 && value instanceof BigDecimal) {
            BigDecimal bigDecimalValue = (BigDecimal) value;
            return bigDecimalValue.setScale(autoBind.scale(), autoBind.roundingMode());
        } else if (autoBind.scale() > 0 && value instanceof Double) {
            BigDecimal bigDecimalValue = BigDecimal.valueOf((Double) value);
            return bigDecimalValue.setScale(autoBind.scale(), autoBind.roundingMode()).doubleValue();
        } else if (autoBind.scale() > 0 && value instanceof Float) {
            BigDecimal bigDecimalValue = BigDecimal.valueOf((Float) value);
            return bigDecimalValue.setScale(autoBind.scale(), autoBind.roundingMode()).floatValue();
        } else {
            return value;
        }
    }

    public static Object setNumberScale(Object value, int scale, RoundingMode roundingMode) {
        if (value == null || scale < 0) {
            return value;
        }
        RoundingMode mode = Optional.ofNullable(roundingMode).orElse(RoundingMode.HALF_UP);
        if (value instanceof BigDecimal) {
            BigDecimal bigDecimalValue = (BigDecimal) value;
            return bigDecimalValue.setScale(scale, mode);
        } else if (value instanceof Double) {
            BigDecimal bigDecimalValue = BigDecimal.valueOf((Double) value);
            return bigDecimalValue.setScale(scale, mode).doubleValue();
        } else if (value instanceof Float) {
            BigDecimal bigDecimalValue = BigDecimal.valueOf((Float) value);
            return bigDecimalValue.setScale(scale, mode).floatValue();
        } else {
            return value;
        }
    }

    public static Object setNumberScale(Number value, int scale, RoundingMode roundingMode) {
        if (value == null || scale < 0) {
            return value;
        }
        RoundingMode mode = Optional.ofNullable(roundingMode).orElse(RoundingMode.HALF_UP);
        if (value instanceof BigDecimal) {
            BigDecimal bigDecimalValue = (BigDecimal) value;
            return bigDecimalValue.setScale(scale, mode);
        } else if (value instanceof Double) {
            BigDecimal bigDecimalValue = BigDecimal.valueOf((Double) value);
            return bigDecimalValue.setScale(scale, mode).doubleValue();
        } else if (value instanceof Float) {
            BigDecimal bigDecimalValue = BigDecimal.valueOf((Float) value);
            return bigDecimalValue.setScale(scale, mode).floatValue();
        } else {
            return value;
        }
    }

    public static boolean isCreatable(String str) {
        return org.apache.commons.lang3.math.NumberUtils.isCreatable(str);
    }

    public static String toHex(Number number) {
        if (number == null) {
            return null;
        } else if (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || number instanceof Long
                || number instanceof Double
                || number instanceof Float) {
            if (number instanceof Long) {
                return Long.toHexString(number.longValue());
            } else if (number instanceof Double) {
                return Double.toHexString(number.doubleValue());
            } else if (number instanceof Float) {
                return Float.toHexString(number.floatValue());
            } else {
                return Integer.toHexString(number.intValue());
            }
        } else {
            return null;
        }
    }

    public static String toPlain(Number number) {
        if (number == null) {
            return null;
        } else if (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || number instanceof Long) {
            return BigDecimal.valueOf(number.longValue()).toPlainString();
        } else {
            return BigDecimal.valueOf(number.doubleValue()).toPlainString();
        }
    }

    public static String toBinary(Number number) {
        if (number == null) {
            return null;
        } else if (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || number instanceof Long) {
            if (number instanceof Long) {
                return Long.toBinaryString(number.longValue());
            } else {
                return Integer.toBinaryString(number.intValue());
            }
        } else {
            return null;
        }
    }

    public static String toOctal(Number number) {
        if (number == null) {
            return null;
        } else if (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || number instanceof Long) {
            if (number instanceof Long) {
                return Long.toOctalString(number.longValue());
            } else {
                return Integer.toOctalString(number.intValue());
            }
        } else {
            return null;
        }
    }

    public static String decimalFormat(Number number, String format) {
        if (number == null || StringUtils.isEmpty(format)) {
            return null;
        } else {
            return new DecimalFormat(format).format(number);
        }
    }

    public static String getRoundingModeStr(RoundingMode roundingMode) {
        if (roundingMode == null) {
            return "java.math.RoundingMode.UP";
        } else if (roundingMode == RoundingMode.CEILING) {
            return "java.math.RoundingMode.CELLING";
        } else if (roundingMode == RoundingMode.DOWN) {
            return "java.math.RoundingMode.DOWN";
        } else if (roundingMode == RoundingMode.FLOOR) {
            return "java.math.RoundingMode.FLOOR";
        } else if (roundingMode == RoundingMode.HALF_DOWN) {
            return "java.math.RoundingMode.HALF_DOWN";
        } else if (roundingMode == RoundingMode.HALF_EVEN) {
            return "java.math.RoundingMode.HALF_EVEN";
        } else if (roundingMode == RoundingMode.HALF_UP) {
            return "java.math.RoundingMode.HALF_UP";
        } else if (roundingMode == RoundingMode.UNNECESSARY) {
            return "java.math.RoundingMode.UNNECESSARY";
        } else if (roundingMode == RoundingMode.UP) {
            return "java.math.RoundingMode.UP";
        } else {
            return "java.math.RoundingMode.UP";
        }
    }

    public static Boolean toBoolean(String value) {
        if (value == null) {
            return null;
        }
        if (Objects.equals(value, "1")) {
            return Boolean.TRUE;
        }
        if (Objects.equals(value, "0")) {
            return Boolean.FALSE;
        } else {
            return Boolean.valueOf(value);
        }
    }

    public static Boolean toBoolean(Number value) {
        if (value == null) {
            return null;
        }
        if (value.longValue() == 1L) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T castSafe(Object object) {
        return (T) object;
    }
}
