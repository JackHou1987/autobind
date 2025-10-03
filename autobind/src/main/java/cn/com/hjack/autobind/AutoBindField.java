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
 * @ClassName: AutoBindField
 * @Description: TODO
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
     * 精确位数
     */
    int scale() default 0;

    /**
     * 精确模式
     */
    RoundingMode roundingMode() default RoundingMode.HALF_UP;

    /**
     * 指示当前字段结果集是否展开，当字段类型为map或java bean时，
     * 该属性为true时则当前map或java bean的key或者字段设置为外层map的key和value
     */
    @Deprecated
    boolean fieldExpand() default false;

    String format() default "yyyy-MM-dd HH:mm:ss";

    String decimalFormat() default "";

    boolean exclude() default false;

    /**
     * @Title: typeConverted
     * @Description: 是否进行java对象字段—>出参字段类型转换
     * @param: @return
     * @return: boolean
     * {@link see convertToStr}
     */
    @Deprecated
    boolean typeConvert() default false;

    @Deprecated
    boolean convertToStr() default false;

    /**
     * 默认值
     */
    String defaultValue() default "";

    /**
     * @Title: customConverter
     * @Description: class for cn.com.yitong.actions.autobind.BeanBinder.Converter
     * @return: Class<?>
     */
    Class<?> customConverter() default Void.class;

    ConvertFeature[] features() default {};

    MapConvertFeature[] mapConvertFeatures() default {};

    String charset() default "UTF-8";

    String[] attributeMap() default "";
}
