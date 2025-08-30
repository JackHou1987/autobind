/**
 *
 */
package cn.com.hjack.autobind;


/**
 * @ClassName: ConvertFeature
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月21日
 *
 */
public enum ConvertFeature {

    BASE64_DECODE_NORMAL,

    BASE64_DECODE_URL,

    BASE64_DECODE_MIME,

    BASE64_ENCODE_NORAML,

    BASE64_ENCODE_URL,

    BASE64_ENCODE_MIME,

    ABSOLUTE_PATH,

    CANONICAL_PATH,

    /**
     * 货币格式
     */
    CURRENCY_FORMAT,

    PLAIN_STRING,

    /**
     * 二进制
     */
    BINARY,

    /**
     * 十六进制
     */
    HEX,

    /**
     * 八进制
     */
    OCT,

    /**
     * url编码
     */
    URL_ENCODE,

    /**
     * url解码
     */
    URL_DECODE,

    /**
     * 堆外内存
     */
    NATIVE_BUFFER,

    /**
     * 开启EL表达式校验
     */
    EL_VALIDATE_ENABLE,

    /**
     * 关闭java bean到map代理特效
     */
    BEAN_TO_MAP_PROXY_DISABLE,

    /**
     * 懒加载模式(Collection、Map和java bean中的值不会预先设置，只有当用到其中的方法时才会初始化)
     */
    LAZY_MODE;

    ConvertFeature() {
        mask = (1 << ordinal());
    }

    public final int mask;

    public final int getMask() {
        return mask;
    }

    public static boolean isEnabled(int features, ConvertFeature feature) {
        return (features & feature.mask) != 0;
    }

    public static boolean isEnabled(ConvertFeature[] features, ConvertFeature feature) {
        return (ConvertFeature.of(features) & feature.mask) != 0;
    }

    public static int of(ConvertFeature[] features) {
        if (features == null) {
            return 0;
        }
        int value = 0;
        for (ConvertFeature feature: features) {
            value |= feature.mask;
        }
        return value;
    }
}
