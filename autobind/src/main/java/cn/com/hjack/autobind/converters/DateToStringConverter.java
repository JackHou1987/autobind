package cn.com.hjack.autobind.converters;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;


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
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return sdf.format(source);
    }

}
