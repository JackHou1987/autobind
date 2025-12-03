package cn.com.hjack.autobind.converter;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.utils.CastUtils;
import cn.com.hjack.autobind.utils.TypeUtils;
import com.google.common.base.Strings;


/**
 * string对象转换器
 * @author houqq
 * @date: 2025年7月16日
 */
public class StringValueResolver extends AbstractResolvableConverter {

    public static StringValueResolver instance = new StringValueResolver();

    private StringValueResolver() {
        registerInternalConverter(Date.class, String.class, (value, config) -> {
            if (!Strings.isNullOrEmpty(config.format())) {
                return new SimpleDateFormat(config.format()).format(value);
            } else {
                return String.valueOf(value);
            }
        });
        registerInternalConverter(Calendar.class, String.class, (value, config) -> {
            if (!Strings.isNullOrEmpty(config.format())) {
                return new SimpleDateFormat(config.format()).format(value.getTime());
            } else {
                return String.valueOf(value);
            }
        });
        registerInternalConverter(TemporalAccessor.class, String.class, (value, config) -> {
            if (!Strings.isNullOrEmpty(config.format())) {
                return DateTimeFormatter.ofPattern(config.format()).format(value);
            } else {
                return String.valueOf(value);
            }
        });
        registerInternalConverter(File.class, String.class, (value, config)-> {
            if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.ABSOLUTE_PATH)) {
                return value.getAbsolutePath();
            } else {
                return value.getPath();
            }
        });
        registerInternalConverter(Number.class, String.class, (value, config)-> {
            if (!Strings.isNullOrEmpty(config.decimalFormat())) {
                return CastUtils.decimalFormat(value, config.decimalFormat());
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.CURRENCY_FORMAT)) {
                return NumberFormat.getCurrencyInstance().format(value);
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.BINARY)) {
                return CastUtils.toBinary(value);
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.OCT)) {
                return CastUtils.toOctal(value);
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.HEX)) {
                return CastUtils.toHex(value);
            } else if (ConvertFeature.isEnabled(ConvertFeature.of(config.convertFeature()), ConvertFeature.PLAIN_STRING)) {
                return CastUtils.toPlain(value);
            } else {
                return String.valueOf(value);
            }
        });
        registerInternalConverter(Enum.class, String.class, (value, config)-> {
            return value.name();
        });
        registerInternalConverter(Throwable.class, String.class, (value, config)-> {
            value.printStackTrace(new PrintWriter(new StringWriter()));
            return value.toString();
        });
        registerInternalConverter(String.class, String.class, (value, config) -> {
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
        registerInternalConverter(Class.class, String.class, (value, config)-> {
            return TypeUtils.getCanonicalName(value);
        });
    }
    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        if (source == null) {
            return DefaultResult.successResult();
        }
        try {
            Class<?> targetClass = targetType.resolve();
            if (targetClass == char[].class) {
                return DefaultResult.successResult(CastUtils.castSafe(convertToString(source, config).toCharArray()));
            } else if (targetClass == StringBuilder.class) {
                return DefaultResult.successResult(CastUtils.castSafe(new StringBuilder(source.toString())));
            } else if (targetClass == StringBuffer.class) {
                return DefaultResult.successResult(CastUtils.castSafe(new StringBuffer(source.toString())));
            } else if (targetClass == char.class) {
                if (source instanceof Character) {
                    return DefaultResult.successResult(CastUtils.castSafe(source));
                } else if (source instanceof String) {
                    return DefaultResult.successResult(CastUtils.castSafe(((String) source).charAt(0)));
                } else if (source instanceof Number) {
                    return DefaultResult.successResult(CastUtils.castSafe((char) ((Number) source).byteValue()));
                } else {
                    return DefaultResult.errorResult("unkown source");
                }
            } else if (targetClass == Character.class) {
                if (source instanceof Character) {
                    return DefaultResult.successResult(CastUtils.castSafe(source));
                } else if (source instanceof String) {
                    return DefaultResult.successResult(CastUtils.castSafe(((String) source).charAt(0)));
                } else if (source instanceof Number) {
                    return DefaultResult.successResult(CastUtils.castSafe((char) ((Number) source).byteValue()));
                } else {
                    return DefaultResult.errorResult("unkown source");
                }
            } else {
                return DefaultResult.successResult(CastUtils.castSafe(convertToString(source, config)));
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(e.getMessage());
        }
    }

    private String convertToString(Object source, ResolveConfig config) throws Exception {
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
        } else if (source instanceof TemporalAccessor) {
            InternalConverter<TemporalAccessor, ResolveConfig, String> converter = CastUtils.castSafe(getInternalConverter(TemporalAccessor.class, String.class));
            if (converter == null) {
                throw new IllegalStateException("can not convert temporalaccessor to string");
            }
            return converter.convert((TemporalAccessor) source, config);
        } else if (source instanceof Number) {
            InternalConverter<Number, ResolveConfig, String> converter = CastUtils.castSafe(getInternalConverter(Number.class, String.class));
            if (converter == null) {
                throw new IllegalStateException("can not convert number to string");
            }
            return converter.convert((Number) source, config);
        } else if (source instanceof Throwable) {
            InternalConverter<Throwable, ResolveConfig, String> converter = CastUtils.castSafe(getInternalConverter(Throwable.class, String.class));
            if (converter == null) {
                throw new IllegalStateException("can not convert throwable to string");
            }
            return converter.convert((Throwable) source, config);
        } else if (source.getClass().isEnum()) {
            InternalConverter<Enum<?>, ResolveConfig, String> converter = CastUtils.castSafe(getInternalConverter(Enum.class, String.class));
            if (converter == null) {
                throw new IllegalStateException("can not convert enum to string");
            }
            return converter.convert((Enum<?>) source, config);
        } else {
            InternalConverter<Object, ResolveConfig, String> converter = CastUtils.castSafe(getInternalConverter(source.getClass(), String.class));
            if (converter != null) {
                return converter.convert(source, config);
            } else {
                return String.valueOf(source);
            }
        }
    }

}
