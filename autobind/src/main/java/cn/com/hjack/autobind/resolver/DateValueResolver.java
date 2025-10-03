/**
 *
 */
package cn.com.hjack.autobind.resolver;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.validation.DefaultResult;
import org.springframework.util.StringUtils;


/**
 * @ClassName: DateValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年7月10日
 *
 */
public class DateValueResolver extends AbstractTypeValueResolver {

    public static DateValueResolver instance = new DateValueResolver();

    private DateValueResolver() {
        registerToDateConverters();
        registerToInstantConverters();
        registerToCalendarConverters();
        registerToLocalDateConverters();
        registerToLocalTimeConverters();
        registerToLocalDateTimeConverters();
        registerToZonedDateTimeConverters();
        registerToOffsetDateTimeConverters();
        registerToOffsetTimeConverters();
    }


    @SuppressWarnings("unchecked")
    @Override
    protected Result<Object> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config) throws Exception {
        if (targetType == null || targetType.resolve() == null) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "object can not be null");
        }
        if (source == null) {
            return DefaultResult.defaultSuccessResult(source);
        }
        Class<?> targetCls = targetType.resolve();
        DefaultResult<Object> result = new DefaultResult<>();
        try {
            InternalConverter<Object, ResolveConfig, Object> function = (InternalConverter<Object, ResolveConfig, Object>) this.getInternalConverter(source.getClass(), targetCls);
            if (function == null) {
                throw new IllegalStateException("can not convert source to number");
            }
            Object target = function.convert(source, config);
            result.setInstance(target);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setResultCode(Constants.FAIL_CODE);
            result.setResultMsg(e.getMessage());
        }
        return result;
    }

    private Date parseDate(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            throw new IllegalStateException("can not convert str to date");
        } else {
            String[] patterns = new String[] {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss",
                    "yyyyMMdd", "yyyyMMddHHmmss", "HHmmss", "HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS"};
            for (String pattern : patterns) {
                SimpleDateFormat formattter = new SimpleDateFormat(pattern);
                Date date = null;
                try {
                    date = formattter.parse(dateStr);
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

    private LocalDate parseLocalDate(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            throw new IllegalStateException("can not convert str to local date");
        } else {
            String[] patterns = new String[] {"yyyy-MM-dd", "yyyy-MM-dd", "yyyyMMdd", "yyyyMMddHHmmss"};
            for (String pattern : patterns) {
                DateTimeFormatter formattter = DateTimeFormatter.ofPattern(pattern);
                LocalDate localDate = null;
                try {
                    localDate = LocalDate.parse(dateStr, formattter);
                    if (localDate != null) {
                        return localDate;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            throw new IllegalStateException("can not convert str to local date");
        }
    }

    private LocalTime parseLocalTime(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            throw new IllegalStateException("can not convert str to local time");
        } else {
            String[] patterns = new String[] {"HH:mm:ss", "HH:mm", "hh:mma"};
            for (String pattern : patterns) {
                DateTimeFormatter formattter = DateTimeFormatter.ofPattern(pattern);
                LocalTime localTime = null;
                try {
                    localTime = LocalTime.parse(dateStr, formattter);
                    return localTime;
                } catch (Exception e) {
                }
            }
            throw new IllegalStateException("can not convert str to local time");
        }
    }
    private LocalDateTime parseLocalDateTime(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            throw new IllegalStateException("can not convert str to local date time");
        } else {
            String[] patterns = new String[] {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyyMMdd", "yyyyMMddHHmmss", "HHmmss", "HH:mm:ss"};
            for (String pattern : patterns) {
                DateTimeFormatter formattter = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime localDateTime = null;
                try {
                    localDateTime = LocalDateTime.parse(dateStr, formattter);
                    return localDateTime;
                } catch (Exception e) {
                }
            }
            throw new IllegalStateException("can not convert str to local date time");
        }
    }


    private void registerToDateConverters() {
        registerInternalConverter(String.class, Date.class, (value, config) -> {
            return parseDate((String) value);
        });
        registerInternalConverter(Date.class, Date.class, (value, config) -> {
            return value;
        });
        registerInternalConverter(Instant.class, Date.class, (value, config) -> {
            return Date.from((Instant) value);
        });
        registerInternalConverter(LocalDate.class, Date.class, (value, config) -> {
            return Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant());
        });
        registerInternalConverter(LocalTime.class, Date.class, (value, config) -> {
            Instant instant = LocalDate.now().atTime(value).atZone(ZoneId.systemDefault()).toInstant();
            return Date.from(instant);
        });
        registerInternalConverter(LocalDateTime.class, Date.class, (value, config) -> {
            ZonedDateTime zoneDateTime = value.atZone(ZoneId.systemDefault());
            return Date.from(zoneDateTime.toInstant());
        });
        registerInternalConverter(Calendar.class, Date.class, (value, config) -> {
            return value.getTime();
        });
        registerInternalConverter(ZonedDateTime.class, Date.class, (value, config) -> {
            return Date.from(value.toInstant());
        });
        registerInternalConverter(OffsetDateTime.class, Date.class, (value, config) -> {
            return Date.from(value.toInstant());
        });
        registerInternalConverter(OffsetTime.class, Date.class, (value, config) -> {
            Instant instant = LocalDate.now().atTime(value).toInstant();
            return Date.from(instant);
        });
        registerInternalConverter(Long.class, Date.class, (value, config) -> {
            return new Date(value);
        });
        registerInternalConverter(long.class, Date.class, (value, config) -> {
            return new Date(value);
        });
    }


    private void registerToInstantConverters() {
        registerInternalConverter(String.class, Instant.class, (value, config) -> {
            return parseDate(value).toInstant();
        });
        registerInternalConverter(Instant.class, Instant.class, (value, config) -> {
            return value;
        });
        registerInternalConverter(Date.class, Instant.class, (value, config) -> {
            return value.toInstant();
        });
        registerInternalConverter(LocalDate.class, Instant.class, (value, config) -> {
            return value.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        });
        registerInternalConverter(LocalTime.class, Instant.class, (value, config) -> {
            return LocalDate.now().atTime(value).atZone(ZoneId.systemDefault()).toInstant();
        });
        registerInternalConverter(LocalDateTime.class, Instant.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toInstant();
        });
        registerInternalConverter(Calendar.class, Instant.class, (value, config) -> {
            return value.getTime().toInstant();
        });
        registerInternalConverter(ZonedDateTime.class, Instant.class, (value, config) -> {
            return value.toInstant();
        });
        registerInternalConverter(OffsetDateTime.class, Instant.class, (value, config) -> {
            return value.toInstant();
        });
        registerInternalConverter(OffsetTime.class, Instant.class, (value, config) -> {
            return LocalDate.now().atTime(value).toInstant();
        });
    }
    private void registerToCalendarConverters() {
        registerInternalConverter(String.class, Calendar.class, (value, config) -> {
            Date date = parseDate(value);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        });
        registerInternalConverter(Instant.class, Calendar.class, (value, config) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(value));
            return calendar;
        });
        registerInternalConverter(Calendar.class, Calendar.class, (value, config) -> {
            return value;
        });
        registerInternalConverter(Date.class, Calendar.class, (value, config) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(value);
            return calendar;
        });
        registerInternalConverter(LocalDate.class, Calendar.class, (value, config) -> {
            Instant instant = value.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        });
        registerInternalConverter(LocalTime.class, Calendar.class, (value, config) -> {
            Instant instant = LocalDate.now().atTime(value)
                    .atZone(ZoneId.systemDefault()).toInstant();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        });
        registerInternalConverter(LocalDateTime.class, Calendar.class, (value, config) -> {
            Instant instant = value.atZone(ZoneId.systemDefault()).toInstant();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        });
        registerInternalConverter(ZonedDateTime.class, Calendar.class, (value, config) -> {
            Instant instant = value.toInstant();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        });
        registerInternalConverter(OffsetDateTime.class, Calendar.class, (value, config) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(value.toInstant()));
            return calendar;
        });
        registerInternalConverter(OffsetTime.class, Calendar.class, (value, config) -> {
            Instant instant = LocalDate.now().atTime(value).toInstant();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        });
        registerInternalConverter(Long.class, Calendar.class, (value, config) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(value);
            return calendar;
        });
        registerInternalConverter(long.class, Calendar.class, (value, config) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(value);
            return calendar;
        });
    }

    private void registerToLocalDateConverters() {
        registerInternalConverter(String.class, LocalDate.class, (value, config) -> {
            return this.parseLocalDate(value);
        });
        registerInternalConverter(Instant.class, LocalDate.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toLocalDate();
        });
        registerInternalConverter(Calendar.class, LocalDate.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        });
        registerInternalConverter(Date.class, LocalDate.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        });
        registerInternalConverter(LocalDate.class, LocalDate.class, (value, config) -> {
            return value;
        });
        registerInternalConverter(LocalTime.class, LocalDate.class, (value, config) -> {
            return LocalDate.now();
        });
        registerInternalConverter(LocalDateTime.class, LocalDate.class, (value, config) -> {
            return value.toLocalDate();
        });
        registerInternalConverter(ZonedDateTime.class, LocalDate.class, (value, config) -> {
            return value.toLocalDate();
        });
        registerInternalConverter(OffsetDateTime.class, LocalDate.class, (value, config) -> {
            return value.toLocalDate();
        });
        registerInternalConverter(OffsetTime.class, LocalDate.class, (value, config) -> {
            return LocalDate.now();
        });
    }
    private void registerToLocalTimeConverters() {
        registerInternalConverter(String.class, LocalTime.class, (value, config) -> {
            return this.parseLocalTime(value);
        });
        registerInternalConverter(Instant.class, LocalTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toLocalTime();
        });
        registerInternalConverter(Calendar.class, LocalTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        });
        registerInternalConverter(Date.class, LocalTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        });
        registerInternalConverter(LocalDate.class, LocalTime.class, (value, config) -> {
            return LocalDate.now().atStartOfDay().toLocalTime();
        });
        registerInternalConverter(LocalTime.class, LocalTime.class, (value, config) -> {
            return value;
        });
        registerInternalConverter(LocalDateTime.class, LocalTime.class, (value, config) -> {
            return value.toLocalTime();
        });
        registerInternalConverter(ZonedDateTime.class, LocalTime.class, (value, config) -> {
            return value.toLocalTime();
        });
        registerInternalConverter(OffsetDateTime.class, LocalTime.class, (value, config) -> {
            return value.toLocalTime();
        });
        registerInternalConverter(OffsetTime.class, LocalTime.class, (value, config) -> {
            return value.toLocalTime();
        });
    }

    private void registerToLocalDateTimeConverters() {
        registerInternalConverter(String.class, LocalDateTime.class, (value, config) -> {
            return this.parseLocalDateTime(value);
        });
        registerInternalConverter(Instant.class, LocalDateTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toLocalDateTime();
        });
        registerInternalConverter(Calendar.class, LocalDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        });
        registerInternalConverter(Date.class, LocalDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        });
        registerInternalConverter(LocalDate.class, LocalDateTime.class, (value, config) -> {
            return value.atStartOfDay();
        });
        registerInternalConverter(LocalTime.class, LocalDateTime.class, (value, config) -> {
            return LocalDate.now().atTime(value);
        });
        registerInternalConverter(LocalDateTime.class, LocalDateTime.class, (value, config) -> {
            return value;
        });
        registerInternalConverter(ZonedDateTime.class, LocalDateTime.class, (value, config) -> {
            return value.toLocalDateTime();
        });
        registerInternalConverter(OffsetDateTime.class, LocalDateTime.class, (value, config) -> {
            return value.toLocalDateTime();
        });
        registerInternalConverter(OffsetTime.class, LocalDateTime.class, (value, config) -> {
            return value.atDate(LocalDate.now()).toLocalDateTime();
        });
    }
    private void registerToZonedDateTimeConverters() {
        registerInternalConverter(String.class, ZonedDateTime.class, (value, config) -> {
            Date date = parseDate(value);
            Instant instant = date.toInstant();
            return instant.atZone(ZoneId.systemDefault());
        });
        registerInternalConverter(Instant.class, ZonedDateTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault());
        });
        registerInternalConverter(Calendar.class, ZonedDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault());
        });
        registerInternalConverter(Date.class, ZonedDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault());
        });
        registerInternalConverter(LocalDate.class, ZonedDateTime.class, (value, config) -> {
            return value.atStartOfDay(ZoneId.systemDefault());
        });
        registerInternalConverter(LocalTime.class, ZonedDateTime.class, (value, config) -> {
            return value.atDate(LocalDate.now()).atZone(ZoneId.systemDefault());
        });
        registerInternalConverter(LocalDateTime.class, ZonedDateTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault());
        });
        registerInternalConverter(ZonedDateTime.class, ZonedDateTime.class, (value, config) -> {
            return value;
        });
        registerInternalConverter(OffsetDateTime.class, ZonedDateTime.class, (value, config) -> {
            return value.toZonedDateTime();
        });
        registerInternalConverter(OffsetTime.class, ZonedDateTime.class, (value, config) -> {
            return value.atDate(LocalDate.now()).toZonedDateTime();
        });
    }

    private void registerToOffsetDateTimeConverters() {
        registerInternalConverter(String.class, OffsetDateTime.class, (value, config) -> {
            Date date = parseDate(value);
            return date.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        registerInternalConverter(Instant.class, OffsetDateTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        registerInternalConverter(Calendar.class, OffsetDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        registerInternalConverter(Date.class, OffsetDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        registerInternalConverter(LocalDate.class, OffsetDateTime.class, (value, config) -> {
            return value.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        });
        registerInternalConverter(LocalTime.class, OffsetDateTime.class, (value, config) -> {
            return value.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        registerInternalConverter(LocalDateTime.class, OffsetDateTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        registerInternalConverter(ZonedDateTime.class, OffsetDateTime.class, (value, config) -> {
            return value.toOffsetDateTime();
        });
        registerInternalConverter(OffsetDateTime.class, OffsetDateTime.class, (value, config) -> {
            return value;
        });
        registerInternalConverter(OffsetTime.class, OffsetDateTime.class, (value, config) -> {
            return value.atDate(LocalDate.now());
        });
    }

    private void registerToOffsetTimeConverters() {
        registerInternalConverter(String.class, OffsetTime.class, (value, config) -> {
            Date date = parseDate(value);
            return date.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        registerInternalConverter(Instant.class, OffsetTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        registerInternalConverter(Calendar.class, OffsetTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        registerInternalConverter(Date.class, OffsetTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        registerInternalConverter(LocalDate.class, OffsetTime.class, (value, config) -> {
            return value.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        registerInternalConverter(LocalTime.class, OffsetTime.class, (value, config) -> {
            return value.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        registerInternalConverter(LocalDateTime.class, OffsetTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        registerInternalConverter(ZonedDateTime.class, OffsetTime.class, (value, config) -> {
            return value.toOffsetDateTime().toOffsetTime();
        });
        registerInternalConverter(OffsetDateTime.class, OffsetTime.class, (value, config) -> {
            return value.toOffsetTime();
        });
        registerInternalConverter(OffsetTime.class, OffsetTime.class, (value, config) -> {
            return value;
        });
    }
}
