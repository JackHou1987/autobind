/**
 *
 */
package cn.com.hjack.autobind.converters;

import java.util.Objects;

import org.springframework.core.convert.converter.Converter;

/**
 * @ClassName: BooleanToStringConverter
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月5日
 */
public class BooleanToStringConverter implements Converter<Boolean, String> {

    @Override
    public String convert(Boolean source) {
        if (Objects.equals(source, true)) {
            return "1";
        } else {
            return "0";
        }
    }

}
