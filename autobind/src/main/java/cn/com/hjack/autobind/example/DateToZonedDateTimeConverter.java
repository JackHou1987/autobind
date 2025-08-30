/**
 *
 */
package cn.com.hjack.autobind.example;

import cn.com.hjack.autobind.Converter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;


/**
 * @ClassName: DateToZonedDateTimeConverter
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月29日
 *
 */
public class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(Date source) {
        return source.toInstant().atZone(ZoneId.systemDefault());
    }

}
