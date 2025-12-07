package cn.com.hjack.autobind;

import java.math.RoundingMode;

import cn.com.hjack.autobind.utils.CastUtils;
import com.google.common.base.Strings;

/**
 * 运行时配置，分为全局配置和单字段配置,当两者都有时,局部配置会覆盖全局配置
 * @author houqq
 * @date: 2025年7月4日
 * @see cn.com.hjack.autobind.AutoBindField
 * @see cn.com.hjack.autobind.ResolveConfig#merge
 */
public class ResolveConfig {

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

    private boolean deepSeek;

    private ConvertFeature[] features = new ConvertFeature[0];

    private MapConvertFeature[] mapConvertFeatures = new MapConvertFeature[0];

    private ResolveConfig() {
    }

    private ResolveConfig(Builder builder) {
        this.fastMode = builder.fastMode;
        if (builder.features != null && builder.features.length != 0) {
            this.features = new ConvertFeature[builder.features.length];
            System.arraycopy(builder.features, 0, this.features, 0, builder.features.length);
        }
        if (builder.mapConvertFeatures != null && builder.mapConvertFeatures.length != 0) {
            this.mapConvertFeatures = new MapConvertFeature[builder.mapConvertFeatures.length];
            System.arraycopy(builder.mapConvertFeatures, 0, this.mapConvertFeatures, 0, builder.mapConvertFeatures.length);
        }
        this.charset = builder.charset;
        this.decimalFormat = builder.decimalFormat;
        this.validator = builder.validator;
        this.deepSeek = builder.deepSeek;
    }

    private ResolveConfig(ResolveConfig config) {
        this.fastMode = config.fastMode;
        if (config.features != null && config.features.length != 0) {
            this.features = new ConvertFeature[config.features.length];
            System.arraycopy(config.features, 0, this.features, 0, config.features.length);
        }
        if (config.mapConvertFeatures != null && config.mapConvertFeatures.length != 0) {
            this.mapConvertFeatures = new MapConvertFeature[config.mapConvertFeatures.length];
            System.arraycopy(config.mapConvertFeatures, 0, this.mapConvertFeatures, 0, config.mapConvertFeatures.length);
        }
        this.format = config.format;
        this.roundingMode = config.roundingMode;
        this.scale = config.scale;
        this.defaultValue = config.defaultValue;
        this.decimalFormat = config.decimalFormat;
        this.validator = config.validator;
        this.customConverter = config.customConverter;
        this.charset = config.charset;
        this.deepSeek = config.deepSeek;
    }

    public <S, T> Converter<S, T> getCustomConverter() {
        return CastUtils.castSafe(this.customConverter);
    }

    public ResolveConfig customConverter(Class<?> cls) {
        ResolveConfig config = this.copy();
        if (cls != null && cls != Void.class) {
            try {
                config.customConverter = (Converter<?, ?>) cls.newInstance();
            } catch (Exception ignored) {
            }
        }
        return config;
    }

    private ResolveConfig copy() {
        return new ResolveConfig(this);
    }

    /**
     * 配置合并
     * @param: 全局配置
     * @param: 局部配置
     * @return: 合并后的配置
     */
    public static ResolveConfig merge(ResolveConfig config, AutoBindField autoBind) {
        ResolveConfig resolveConfig;
        if (config == null) {
            resolveConfig = defaultConfig.copy();
        } else {
            resolveConfig = config.copy();
        }
        if (autoBind != null) {
            resolveConfig = resolveConfig.format(autoBind.format())
                    .scale(autoBind.scale())
                    .deepSeek(autoBind.deepSeek())
                    .charset(autoBind.charset())
                    .roundingMode(autoBind.roundingMode())
                    .defaultValue(autoBind.defaultValue())
                    .decimalFormat(autoBind.decimalFormat())
                    .convertFeature(autoBind.features())
                    .mapConvertFeature(autoBind.mapConvertFeatures())
                    .customConverter(autoBind.customConverter());
        }
        return resolveConfig;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean fastMode() {
        return fastMode;
    }

    public ResolveConfig fastMode(boolean fastMode) {
        ResolveConfig config = this.copy();
        if (fastMode) {
            config.fastMode = true;
        }
        return config;
    }

    public boolean deepSeek() {
        return deepSeek;
    }

    public ResolveConfig deepSeek(boolean deepSeek) {
        ResolveConfig config = this.copy();
        if (deepSeek) {
            config.deepSeek = true;
        }
        return config;
    }

    public int scale() {
        return scale;
    }

    public ResolveConfig scale(int scale) {
        ResolveConfig config = this.copy();
        if (scale != 0) {
            config.scale = scale;
        }
        return config;
    }
    public String format() {
        return format;
    }

    public ResolveConfig format(String format) {
        ResolveConfig config = this.copy();
        if (!Strings.isNullOrEmpty(format)) {
            config.format = format;
        }
        return config;
    }

    public String decimalFormat() {
        return decimalFormat;
    }

    public ResolveConfig decimalFormat(String decimalFormat) {
        ResolveConfig config = this.copy();
        if (!Strings.isNullOrEmpty(decimalFormat)) {
            config.decimalFormat = decimalFormat;
        }
        return config;
    }

    public RoundingMode roundingMode() {
        return roundingMode;
    }

    public ResolveConfig roundingMode(RoundingMode roundingMode) {
        ResolveConfig config = this.copy();
        if (roundingMode != null) {
            config.roundingMode = roundingMode;
        }
        return config;
    }

    public String charset() {
        return charset;
    }

    public ResolveConfig charset(String charset) {
        ResolveConfig config = this.copy();
        if (!Strings.isNullOrEmpty(charset)) {
            config.charset = charset;
        }
        return config;
    }

    public String defaultValue() {
        return this.defaultValue;
    }

    public ResolveConfig defaultValue(String defaultValue) {
        ResolveConfig config = this.copy();
        if (!Strings.isNullOrEmpty(defaultValue)) {
            config.defaultValue = defaultValue;
        }
        return config;
    }

    public ResolveConfig removeConvertFeature(ConvertFeature feature) {
        if (feature != null && this.features.length != 0) {
            ResolveConfig config = this.copy();
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

    public ResolveConfig convertFeature(ConvertFeature... features) {
        ResolveConfig config = this.copy();
        if (features != null && features.length != 0) {
            ConvertFeature[] convertFeatures = new ConvertFeature[config.features.length + features.length];
            System.arraycopy(config.features, 0, convertFeatures, 0, config.features.length);
            System.arraycopy(features, 0, convertFeatures, config.features.length, features.length);
            config.features = convertFeatures;
        }
        return config;
    }

    public ConvertFeature[] convertFeature() {
        return this.features;
    }

    public MapConvertFeature[] mapConvertFeature() {
        return this.mapConvertFeatures;
    }

    public ResolveConfig mapConvertFeature(MapConvertFeature... features) {
        ResolveConfig config = this.copy();
        if (features != null && features.length != 0) {
            MapConvertFeature[] convertFeatures = new MapConvertFeature[config.features.length + features.length];
            System.arraycopy(config.mapConvertFeatures, 0, convertFeatures, 0, config.features.length);
            System.arraycopy(features, 0, convertFeatures, config.features.length, features.length);
            config.mapConvertFeatures = convertFeatures;
        }
        return config;
    }

    public Validator validator() {
        return this.validator;
    }

    public ResolveConfig validator(Validator validator) {
        ResolveConfig config = this.copy();
        if (validator != null) {
            config.validator = validator;
        }
        return config;
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

        private boolean deepSeek;

        public Builder fastMode(boolean fastMode) {
            this.fastMode = fastMode;
            return this;
        }

        public Builder convertFeature(ConvertFeature... features) {
            this.features = features;
            return this;
        }

        public Builder mapConvertFeature(MapConvertFeature... mapConvertFeatures) {
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

        public Builder deepSeek(boolean deepSeek) {
            this.deepSeek = deepSeek;
            return this;
        }

        public ResolveConfig build() {
            return new ResolveConfig(this);
        }

    }


}
