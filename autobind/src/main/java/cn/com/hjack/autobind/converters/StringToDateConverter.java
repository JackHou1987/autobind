/**
 *
 */
package cn.com.hjack.autobind.converters;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

/**
 * @ClassName: StringToDateConverter
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月5日
 */
public class StringToDateConverter implements Converter<String, Date> {

    @Override
    public Date convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        } else {
            return parseDateTime(source);
        }
    }

    private static Date parseDateTime(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        } else {
            String[] patterns = new String[] {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyyMMdd", "yyyyMMddHHmmss", "HHmmss", "HH:mm:ss"};
            for (String pattern : patterns) {
                SimpleDateFormat formattter = new SimpleDateFormat(pattern);
                Date date = null;
                try {
                    date = formattter.parse(str);
                    if (date != null) {
                        return date;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            throw new IllegalStateException("can not convert str to date");
        }
    }

}
