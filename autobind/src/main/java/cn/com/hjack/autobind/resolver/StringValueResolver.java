/**
 *
 */
package cn.com.hjack.autobind.resolver;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import cn.com.hjack.autobind.ConvertFeature;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.utils.TypeUtils;
import cn.com.hjack.autobind.validation.DefaultResult;
import org.springframework.util.StringUtils;


/**
 * @ClassName: StringValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年7月16日
 */
public class StringValueResolver extends AbstractTypeValueResolver {

    public static StringValueResolver instance = new StringValueResolver();

    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private StringValueResolver() {
        super.registerInternalConverter(Date.class, String.class, (value, config) -> {
            if (!StringUtils.isEmpty(config.format())) {
                SimpleDateFormat format = new SimpleDateFormat(config.format());
                return format.format(value);
            } else {
                SimpleDateFormat format = new SimpleDateFormat(DEFAULT_FORMAT);
                return format.format(value);
            }
        });
        super.registerInternalConverter(Calendar.class, String.class, (value, config) -> {
            if (!StringUtils.isEmpty(config.format())) {
                SimpleDateFormat format = new SimpleDateFormat(config.format());
                return format.format(value.getTime());
            } else {
                SimpleDateFormat format = new SimpleDateFormat(DEFAULT_FORMAT);
                return format.format(value);
            }
        });
        super.registerInternalConverter(File.class, String.class, (value, config)-> {
            if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.ABSOLUTE_PATH)) {
                return value.getAbsolutePath();
            } else {
                return value.getPath();
            }
        });
        super.registerInternalConverter(Number.class, String.class, (value, config)-> {
            if (!StringUtils.isEmpty(config.decimalFormat())) {
                return CastUtils.decimalFormat(value, config.format());
            }
            if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.CURRENCY_FORMAT)) {
                NumberFormat currency = NumberFormat.getCurrencyInstance();
                return currency.format(currency);
            }
            if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.BINARY)) {
                return CastUtils.toBinary(value);
            }
            if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.OCT)) {
                return CastUtils.toOctal(value);
            }
            if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.HEX)) {
                return CastUtils.toHex(value);
            }
            if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.PLAIN_STRING)) {
                return CastUtils.toPlain(value);
            }
            return String.valueOf(value);
        });
        super.registerInternalConverter(Enum.class, String.class, (value, config)-> {
            return value.name();
        });
        super.registerInternalConverter(Throwable.class, String.class, (value, config)-> {
            StringWriter sw = new StringWriter();
            value.printStackTrace(new PrintWriter(sw));
            return value.toString();
        });
        super.registerInternalConverter(byte[].class, String.class, (value, config)-> {
            if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.HEX)) {
                StringBuilder sb = new StringBuilder();
                for (byte b : value) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } else {
                return new String(value, config.charset());
            }
        });
        super.registerInternalConverter(Byte[].class, String.class, (value, config)-> {
            byte[] bytes = new byte[value.length];
            for (int i = 0; i < value.length; ++i) {
                if (value[i] == null) {
                    bytes[i] = 0;
                } else {
                    bytes[i] = value[i];
                }
            }
            return new String(bytes, config.charset());
        });
        super.registerInternalConverter(String.class, String.class, (value, config) -> {
            if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.BASE64_ENCODE_NORAML)) {
                return Base64.getEncoder().encodeToString(value.getBytes(Charset.forName(config.charset())));
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.BASE64_ENCODE_URL)) {
                return Base64.getUrlEncoder().encodeToString(value.getBytes(Charset.forName(config.charset())));
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.BASE64_ENCODE_MIME)) {
                return Base64.getMimeEncoder().encodeToString(value.getBytes(Charset.forName(config.charset())));
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.BASE64_DECODE_NORMAL)) {
                byte[] bytes = Base64.getDecoder().decode(value);
                return new String(bytes, config.charset());
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.BASE64_DECODE_URL)) {
                byte[] bytes = Base64.getUrlDecoder().decode(value);
                return new String(bytes, config.charset());
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.BASE64_DECODE_MIME)) {
                byte[] bytes = Base64.getMimeDecoder().decode(value);
                return new String(bytes, config.charset());
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.URL_ENCODE)) {
                return URLEncoder.encode(value, config.charset());
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.URL_DECODE)) {
                return URLDecoder.decode(value, config.charset());
            } else {
                return value;
            }
        });
        super.registerInternalConverter(Class.class, String.class, (value, config)-> {
            return TypeUtils.getCanonicalName(value);
        });
    }
    @Override
    protected Result<Object> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config)
            throws Exception {
        if (source == null) {
            return DefaultResult.defaultSuccessResult(null);
        }
        Result<Object> result = new DefaultResult<>();
        result.setResultCode(Constants.RESULT_CODE_KEY);
        try {
            Class<?> targetClass = targetType.resolve();
            if (targetClass == char[].class) {
                result.instance(resolveToStr(source, config).toCharArray());
            } else if (targetClass == StringBuilder.class) {
                result.instance(new StringBuilder(source.toString()));
            } else if (targetClass == StringBuffer.class) {
                result.instance(new StringBuffer(source.toString()));
            } else if (targetClass == char.class) {
                if (source instanceof Character) {
                    result.instance(source);
                } else if (source instanceof String) {
                    result.instance(((String) source).charAt(0));
                }
            } else if (targetClass == Character.class) {
                if (source instanceof Character) {
                    result.instance(source);
                } else if (source instanceof String) {
                    result.instance(((String) source).charAt(0));
                }
            } else {
                result.instance(this.resolveToStr(source, config));
            }
        } catch (Exception e) {
            result.setResultCode(Constants.FAIL_CODE);
            result.setResultMsg(e.getMessage());
        }
        return result;
    }
    private String resolveToStr(Object source, ResolveConfig config) throws Exception {
        if (source instanceof Collection) {
            Collection<?> object = (Collection<?>) source;
            return object.stream().map(String::valueOf).collect(Collectors.joining(","));
        } else if (TypeUtils.isArrayClass(source.getClass())) {
            int dimension = TypeUtils.getArrayTypeDimension(source.getClass());
            if (dimension == 1) {
                return Arrays.toString((Object[]) source);
            } else {
                return Arrays.deepToString((Object[]) source);
            }
        } else if (source instanceof LocalDate
                || source instanceof LocalTime
                || source instanceof LocalDateTime
                || source instanceof ZonedDateTime
                || source instanceof OffsetDateTime
                || source instanceof OffsetTime) {
            if (!StringUtils.isEmpty(config.format())) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(config.format());
                return formatter.format((TemporalAccessor) source);
            } else {
                return String.valueOf(source);
            }
        } else if (source instanceof Number) {
            InternalConverter<Number, ResolveConfig, String> function = super.getInternalConverter(Number.class, String.class);
            if (function == null) {
                throw new IllegalStateException("can not convert source to number");
            }
            return function.convert((Number) source, config);
        } else if (source instanceof Throwable) {
            InternalConverter<Throwable, ResolveConfig, String> function = super.getInternalConverter(Throwable.class, String.class);
            if (function == null) {
                throw new IllegalStateException("can not convert source to throwable");
            }
            return function.convert((Throwable) source, config);
        } else if (source.getClass().isEnum()) {
            InternalConverter<Enum, ResolveConfig, String> function = super.getInternalConverter(Enum.class, String.class);
            if (function == null) {
                throw new IllegalStateException("can not convert source to enum");
            }
            return function.convert((Enum) source, config);
        } else {
            return String.valueOf(source);
        }
    }

}
