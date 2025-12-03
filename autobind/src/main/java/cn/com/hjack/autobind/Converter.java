/**
 *
 */
package cn.com.hjack.autobind;


/**
 *   自定义转换器，通过AutoBindField注解或者SPI加载，转换有如下优先级,从高到低
 * <ol>
 * <li>通过{@link cn.com.hjack.autobind.ResolveConfig.customConverter}配置的converter
 * <li>通过{@link cn.com.hjack.autobind.AutoBindField#customConverter}指定了单字段局部customer
 * <li>通过spi配置config converter，参考{@link cn.com.hjack.autobind.ConverterProvider#getConfigConverter}
 * <li>内部converter，参考 {@link cn.com.hjack.autobind.ResolvableConverter}
 * </ol>
 * @author houqq
 * @date: 2025年10月21日
 * @see cn.com.hjack.autobind.AutoBindField#customConverter
 * @see cn.com.hjack.autobind.ConverterProvider#getConfigConverter
 */
public interface Converter<S, T> {

    T convert(S source);
}
