/**
 *
 */
package cn.com.hjack.autobind;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


/**
 * @ClassName: ResolveConfig
 * @Description: 解析参数配置，用于类型convert时需要的附加参数
 * @author houqq
 * @date: 2025年7月4日
 */
public class ResolveConfig implements Cloneable {

    public static ResolveConfig defaultConfig = new ResolveConfig();

    private boolean fastMode;

    private int scale;

    private String format;

    private String decimalFormat;

    private RoundingMode roundingMode;

    private String defaultValue;

    private String charset;

    private Converter<?, ?> customConverter;

    /**
     * 全局校验器
     */
    private Validator validator;

    private ConvertFeature[] features = new ConvertFeature[0];

    private MapConvertFeature[] mapConvertFeatures = new MapConvertFeature[0];

    private Map<String, Object> attributes = new HashMap<>();

    private ConvertInterceptor convertInterceptor;

    private ResolveConfig() {
    }

    private ResolveConfig(Builder builder) {
        this.fastMode = builder.fastMode;
        this.features = builder.features;
        this.mapConvertFeatures = builder.mapConvertFeatures;
        this.charset = builder.charset;
        this.decimalFormat = builder.decimalFormat;
        this.validator = builder.validator;
        this.convertInterceptor = builder.convertInterceptor;
    }

    private ResolveConfig(ResolveConfig config) {
        this.fastMode = config.fastMode;
        this.format = config.format;
        this.roundingMode = config.roundingMode;
        this.scale = config.scale;
        this.defaultValue = config.defaultValue;
        this.decimalFormat = config.decimalFormat;
        this.validator = config.validator;
    }

    @SuppressWarnings("unchecked")
    public <S, T> Converter<S, T> getCustomConverter() {
        return (Converter<S, T>) this.customConverter;
    }

    public ResolveConfig clone() {
        return new ResolveConfig(this);
    }

    public static ResolveConfig copy(ResolveConfig config, AutoBindField autoBind) {
        ResolveConfig resolveConfig;
        if (config == null) {
            resolveConfig = defaultConfig.clone();
        } else {
            resolveConfig = config.clone();
        }
        if (autoBind != null) {
            resolveConfig = resolveConfig.format(autoBind.format())
                    .scale(autoBind.scale())
                    .roundingMode(autoBind.roundingMode())
                    .defaultValue(autoBind.defaultValue())
                    .convertFeature(autoBind.features());
            if (autoBind.customConverter() != Void.class) {
                try {
                    Converter<?, ?> converter = (Converter<?, ?>) autoBind.customConverter().newInstance();
                    resolveConfig.customConverter = converter;
                } catch (Exception e) {
                }
            }
        }
        return resolveConfig;
    }

    public static ResolveConfig copy(ResolveConfig config) {
        return ResolveConfig.copy(config, null);
    }

    public static class Builder {

        private Builder() {

        }

        private boolean fastMode;

        private ConvertFeature[] features;

        private MapConvertFeature[] mapConvertFeatures;

        private String charset;

        private String decimalFormat;

        private Validator validator;

        private ConvertInterceptor convertInterceptor;

        public Builder fastMode(boolean fastMode) {
            this.fastMode = fastMode;
            return this;
        }

        public Builder convertFeature(ConvertFeature[] features) {
            this.features = features;
            return this;
        }
        public Builder mapConvertFeature(MapConvertFeature[] mapConvertFeatures) {
            this.mapConvertFeatures = mapConvertFeatures;
            return this;
        }

        public Builder charset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder decimalFormat(String decimalFormat) {
            this.decimalFormat = decimalFormat;
            return this;
        }

        public Builder validator(Validator validator) {
            this.validator = validator;
            return this;
        }

        public Builder interceptor(ConvertInterceptor interceptor) {
            this.convertInterceptor = interceptor;
            return this;
        }

        public ResolveConfig build() {
            return new ResolveConfig(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean fastMode() {
        return fastMode;
    }

    public ResolveConfig fastMode(boolean fastMode) {
        ResolveConfig config = this.clone();
        config.fastMode = fastMode;
        return config;
    }

    public int scale() {
        return scale;
    }

    public ResolveConfig scale(int scale) {
        ResolveConfig config = this.clone();
        config.scale = scale;
        return config;
    }

    public String format() {
        return format;
    }

    public ResolveConfig format(String format) {
        ResolveConfig config = this.clone();
        config.format = format;
        return config;
    }

    public String decimalFormat() {
        return decimalFormat;
    }

    public ResolveConfig decimalFormat(String decimalFormat) {
        ResolveConfig config = this.clone();
        config.decimalFormat = decimalFormat;
        return config;
    }

    public RoundingMode roundingMode() {
        return roundingMode;
    }

    public ResolveConfig roundingMode(RoundingMode roundingMode) {
        ResolveConfig config = this.clone();
        config.roundingMode = roundingMode;
        return config;
    }

    public String charset() {
        return charset;
    }

    public ResolveConfig charset(String charset) {
        ResolveConfig config = this.clone();
        config.charset = charset;
        return config;
    }

    public String defaultValue() {
        return this.defaultValue;
    }

    public ResolveConfig defaultValue(String defaultValue) {
        ResolveConfig config = this.clone();
        config.defaultValue = defaultValue;
        return config;
    }

    public ResolveConfig removeConvertFeature(ConvertFeature feature) {
        if (feature != null && this.features.length != 0) {
            ResolveConfig config = this.clone();
            ConvertFeature[] newFeatures = new ConvertFeature[this.features.length - 1];
            for (int i = 0; i < this.features.length; ++i) {
                if (features[i] != feature) {
                    newFeatures[i] = features[i];
                }
            }
            config.features = newFeatures;
            return config;
        } else {
            return this;
        }
    }

    public ResolveConfig convertFeature(ConvertFeature[] features) {
        ResolveConfig config = this.clone();
        config.features = features;
        return config;
    }

    public ConvertFeature[] convertFeature() {
        return this.features;
    }

    public MapConvertFeature[] mapConvertFeature() {
        return this.mapConvertFeatures;
    }

    public Validator validator() {
        return this.validator;
    }

    public ResolveConfig attributes(Map<String, Object> attributes) {
        ResolveConfig config = this.clone();
        config.attributes.putAll(attributes);
        return config;
    }

    public ResolveConfig attribute(String key, String value) {
        ResolveConfig config = this.clone();
        config.attributes.putAll(this.attributes);
        config.attributes.put(key, value);
        return config;
    }

    public Object attribute(String key) {
        return attributes.get(key);
    }

    public ResolveConfig interceptor(ConvertInterceptor interceptor) {
        ResolveConfig config = this.clone();
        config.convertInterceptor = interceptor;
        return config;
    }

    public ResolveConfig removeInterceptor() {
        return this.clone();
    }

    public ConvertInterceptor interceptor() {
        return this.convertInterceptor;
    }

}
