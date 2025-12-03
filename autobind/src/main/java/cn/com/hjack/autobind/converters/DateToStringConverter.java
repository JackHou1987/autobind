package cn.com.hjack.autobind.converters;

import java.util.Date;

import org.springframework.core.convert.converter.Converter;

import cn.hutool.core.date.DateUtil;

/**
 * @ClassName: DateToStrConverter
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月5日
 */
public class DateToStringConverter implements Converter<Date, String> {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public String convert(Date source) {
        if (source == null) {
            return null;
        } else {
            return DateUtil.format(source, DEFAULT_DATE_FORMAT);
        }
    }

}
