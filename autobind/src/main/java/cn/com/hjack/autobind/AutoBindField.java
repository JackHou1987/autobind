/**
 *
 */
package cn.com.hjack.autobind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.RoundingMode;

/**
 *   用在javabean字段上，定义如何对一个字段(source -> target)进行转换，该注解对source field和target field均有效。
 *    不配置该注解则使用默认配置进行转换。该注解提供多种常用配置如字段名称、日期或数值格式等。
 *    指定
 * @author houqq
 * @date: 2025年6月4日
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AutoBindField {

    /**
     * 接收字段名
     */
    String recvFieldName() default "";

    /**
     * 输出字段名
     */
    String[] sendFieldName() default {};

    /**
     * el表达式校验
     */
    String condition() default "";

    /**
     * 当校验失败时，返回的error msg
     */
    String errorMsg() default "";

    /**
     * 数值保留小数位数
     */
    int scale() default 0;

    /**
     * 精确模式
     */
    RoundingMode roundingMode() default RoundingMode.HALF_UP;

    /**
     *   指示当前字段结果集是否展开，当字段类型为map或java bean时，
     *   该属性为true时则当前map或java bean的key或者字段设置为外层map的key和value
     */
    @Deprecated
    boolean fieldExpand() default false;

    /**
     *  日期字段格式
     */
    String format() default "yyyy-MM-dd HH:mm:ss";

    String decimalFormat() default "";

    boolean exclude() default false;

    @Deprecated
    boolean typeConvert() default false;

    @Deprecated
    boolean convertToStr() default false;

    /**
     *  默认值
     */
    String defaultValue() default "";

    /**
     *  默认使用该字段自定义converter,其次用内部converter
     */
    Class<?> customConverter() default Void.class;

    ConvertFeature[] features() default {};

    MapConvertFeature[] mapConvertFeatures() default {};

    /**
     * 字符集编码格式
     */
    String charset() default "UTF-8";

    /**
     * 是否嵌套查找，当javabean字段在源map中找不到相应的key时，尝试从map结构包含value为map的结构中嵌套查找
     */
    boolean deepSeek() default false;
}