/**
 *
 */
package cn.com.hjack.autobind;


/**
 * @ClassName: MapConvertFeature
 * @Description: TODO
 * @author houqq
 * @date: 2025年10月21日
 *
 */
public enum MapConvertFeature {

    NOOP,

    /**
     * 大写驼峰
     */
    KEY_UPPER_CAMEL,

    /**
     *  小写驼峰
     */
    KEY_LOWER_CAMEL,

    /**
     *  小写下划线
     */
    KEY_LOWER_UNDERSCORE,

    /**
     * 小写横线
     */
    KEY_LOWER_HYPHEN,

    /**
     * 大写下划线
     */
    KEY_UPPER_UNDERSCORE,

    /**
     *  转换为文本类型
     */
    VALUE_CONVERT_TO_STRING,

    /**
     *  转换为对象实际类型
     */
    VALUE_CONVERT_TO_OBJ,

    KEY_EXPAND;

    MapConvertFeature(){
        mask = (1 << ordinal());
    }

    public final int mask;

    public final int getMask() {
        return mask;
    }
    public static boolean isEnabled(int features, MapConvertFeature feature) {
        return (features & feature.mask) != 0;
    }
    public static int of(MapConvertFeature[] features) {
        if (features == null) {
            return 0;
        }
        int value = 0;
        for (MapConvertFeature feature: features) {
            value |= feature.mask;
        }
        return value;
    }
}
