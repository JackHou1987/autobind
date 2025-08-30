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

import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
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
        this.registerToDateConverters();
        this.registerToInstantConverters();
        this.registerToCalendarConverters();
        this.registerToLocalDateConverters();
        this.registerToLocalTimeConverters();
        this.registerToLocalDateTimeConverters();
        this.registerToZonedDateTimeConverters();
        this.registerToOffsetDateTimeConverters();
        this.registerToOffsetTimeConverters();
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
            String[] patterns = new String[] {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyyMMdd", "yyyyMMddHHmmss", "HHmmss", "HH:mm:ss"};
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
                    return localDate;
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
                    continue;
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
                    continue;
                }
            }
            throw new IllegalStateException("can not convert str to local date time");
        }
    }


    private void registerToDateConverters() {
        super.registerInternalConverter(String.class, Date.class, (value, config) -> {
            return parseDate((String) value);
        });
        super.registerInternalConverter(Date.class, Date.class, (value, config) -> {
            return value;
        });
        super.registerInternalConverter(Instant.class, Date.class, (value, config) -> {
            return Date.from((Instant) value);
        });
        super.registerInternalConverter(LocalDate.class, Date.class, (value, config) -> {
            return Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant());
        });
        super.registerInternalConverter(LocalTime.class, Date.class, (value, config) -> {
            Instant instant = LocalDate.now().atTime(value).atZone(ZoneId.systemDefault()).toInstant();
            return Date.from(instant);
        });
        super.registerInternalConverter(LocalDateTime.class, Date.class, (value, config) -> {
            ZonedDateTime zoneDateTime = value.atZone(ZoneId.systemDefault());
            return Date.from(zoneDateTime.toInstant());
        });
        super.registerInternalConverter(Calendar.class, Date.class, (value, config) -> {
            return value.getTime();
        });
        super.registerInternalConverter(ZonedDateTime.class, Date.class, (value, config) -> {
            return Date.from(value.toInstant());
        });
        super.registerInternalConverter(OffsetDateTime.class, Date.class, (value, config) -> {
            return Date.from(value.toInstant());
        });
        super.registerInternalConverter(OffsetTime.class, Date.class, (value, config) -> {
            Instant instant = LocalDate.now().atTime(value).toInstant();
            return Date.from(instant);
        });
    }

    private void registerToInstantConverters() {
        super.registerInternalConverter(String.class, Instant.class, (value, config) -> {
            return parseDate(value).toInstant();
        });
        super.registerInternalConverter(Instant.class, Instant.class, (value, config) -> {
            return value;
        });
        super.registerInternalConverter(Date.class, Instant.class, (value, config) -> {
            return value.toInstant();
        });
        super.registerInternalConverter(LocalDate.class, Instant.class, (value, config) -> {
            return value.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        });
        super.registerInternalConverter(LocalTime.class, Instant.class, (value, config) -> {
            return LocalDate.now().atTime(value).atZone(ZoneId.systemDefault()).toInstant();
        });
        super.registerInternalConverter(LocalDateTime.class, Instant.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toInstant();
        });
        super.registerInternalConverter(Calendar.class, Instant.class, (value, config) -> {
            return value.getTime().toInstant();
        });
        super.registerInternalConverter(ZonedDateTime.class, Instant.class, (value, config) -> {
            return value.toInstant();
        });
        super.registerInternalConverter(OffsetDateTime.class, Instant.class, (value, config) -> {
            return value.toInstant();
        });
        super.registerInternalConverter(OffsetTime.class, Instant.class, (value, config) -> {
            return LocalDate.now().atTime(value).toInstant();
        });
    }

    private void registerToCalendarConverters() {
        super.registerInternalConverter(String.class, Calendar.class, (value, config) -> {
            Date date = parseDate(value);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        });
        super.registerInternalConverter(Instant.class, Calendar.class, (value, config) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(value));
            return calendar;
        });
        super.registerInternalConverter(Calendar.class, Calendar.class, (value, config) -> {
            return value;
        });
        super.registerInternalConverter(Date.class, Calendar.class, (value, config) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(value);
            return calendar;
        });
        super.registerInternalConverter(LocalDate.class, Calendar.class, (value, config) -> {
            Instant instant = value.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        });
        super.registerInternalConverter(LocalTime.class, Calendar.class, (value, config) -> {
            Instant instant = LocalDate.now().atTime(value)
                    .atZone(ZoneId.systemDefault()).toInstant();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        });
        super.registerInternalConverter(LocalDateTime.class, Calendar.class, (value, config) -> {
            Instant instant = value.atZone(ZoneId.systemDefault()).toInstant();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        });
        super.registerInternalConverter(ZonedDateTime.class, Calendar.class, (value, config) -> {
            Instant instant = value.toInstant();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        });
        super.registerInternalConverter(OffsetDateTime.class, Calendar.class, (value, config) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(value.toInstant()));
            return calendar;
        });
        super.registerInternalConverter(OffsetTime.class, Calendar.class, (value, config) -> {
            Instant instant = LocalDate.now().atTime(value).toInstant();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(instant));
            return calendar;
        });
    }

    private void registerToLocalDateConverters() {
        super.registerInternalConverter(String.class, LocalDate.class, (value, config) -> {
            return this.parseLocalDate(value);
        });
        super.registerInternalConverter(Instant.class, LocalDate.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toLocalDate();
        });
        super.registerInternalConverter(Calendar.class, LocalDate.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        });
        super.registerInternalConverter(Date.class, LocalDate.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        });
        super.registerInternalConverter(LocalDate.class, LocalDate.class, (value, config) -> {
            return value;
        });
        super.registerInternalConverter(LocalTime.class, LocalDate.class, (value, config) -> {
            return LocalDate.now();
        });
        super.registerInternalConverter(LocalDateTime.class, LocalDate.class, (value, config) -> {
            return value.toLocalDate();
        });
        super.registerInternalConverter(ZonedDateTime.class, LocalDate.class, (value, config) -> {
            return value.toLocalDate();
        });
        super.registerInternalConverter(OffsetDateTime.class, LocalDate.class, (value, config) -> {
            return value.toLocalDate();
        });
        super.registerInternalConverter(OffsetTime.class, LocalDate.class, (value, config) -> {
            return LocalDate.now();
        });
    }

    private void registerToLocalTimeConverters() {
        super.registerInternalConverter(String.class, LocalTime.class, (value, config) -> {
            return this.parseLocalTime(value);
        });
        super.registerInternalConverter(Instant.class, LocalTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toLocalTime();
        });
        super.registerInternalConverter(Calendar.class, LocalTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        });
        super.registerInternalConverter(Date.class, LocalTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        });
        super.registerInternalConverter(LocalDate.class, LocalTime.class, (value, config) -> {
            return LocalDate.now().atStartOfDay().toLocalTime();
        });
        super.registerInternalConverter(LocalTime.class, LocalTime.class, (value, config) -> {
            return value;
        });
        super.registerInternalConverter(LocalDateTime.class, LocalTime.class, (value, config) -> {
            return value.toLocalTime();
        });
        super.registerInternalConverter(ZonedDateTime.class, LocalTime.class, (value, config) -> {
            return value.toLocalTime();
        });
        super.registerInternalConverter(OffsetDateTime.class, LocalTime.class, (value, config) -> {
            return value.toLocalTime();
        });
        super.registerInternalConverter(OffsetTime.class, LocalTime.class, (value, config) -> {
            return value.toLocalTime();
        });
    }

    private void registerToLocalDateTimeConverters() {
        super.registerInternalConverter(String.class, LocalDateTime.class, (value, config) -> {
            return this.parseLocalDateTime(value);
        });
        super.registerInternalConverter(Instant.class, LocalDateTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toLocalDateTime();
        });
        super.registerInternalConverter(Calendar.class, LocalDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        });
        super.registerInternalConverter(Date.class, LocalDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        });
        super.registerInternalConverter(LocalDate.class, LocalDateTime.class, (value, config) -> {
            return value.atStartOfDay();
        });
        super.registerInternalConverter(LocalTime.class, LocalDateTime.class, (value, config) -> {
            return LocalDate.now().atTime(value);
        });
        super.registerInternalConverter(LocalDateTime.class, LocalDateTime.class, (value, config) -> {
            return value;
        });
        super.registerInternalConverter(ZonedDateTime.class, LocalDateTime.class, (value, config) -> {
            return value.toLocalDateTime();
        });
        super.registerInternalConverter(OffsetDateTime.class, LocalDateTime.class, (value, config) -> {
            return value.toLocalDateTime();
        });
        super.registerInternalConverter(OffsetTime.class, LocalDateTime.class, (value, config) -> {
            return value.atDate(LocalDate.now()).toLocalDateTime();
        });
    }

    private void registerToZonedDateTimeConverters() {
        super.registerInternalConverter(String.class, ZonedDateTime.class, (value, config) -> {
            Date date = parseDate(value);
            Instant instant = date.toInstant();
            return instant.atZone(ZoneId.systemDefault());
        });
        super.registerInternalConverter(Instant.class, ZonedDateTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault());
        });
        super.registerInternalConverter(Calendar.class, ZonedDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault());
        });
        super.registerInternalConverter(Date.class, ZonedDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault());
        });
        super.registerInternalConverter(LocalDate.class, ZonedDateTime.class, (value, config) -> {
            return value.atStartOfDay(ZoneId.systemDefault());
        });
        super.registerInternalConverter(LocalTime.class, ZonedDateTime.class, (value, config) -> {
            return value.atDate(LocalDate.now()).atZone(ZoneId.systemDefault());
        });
        super.registerInternalConverter(LocalDateTime.class, ZonedDateTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault());
        });
        super.registerInternalConverter(ZonedDateTime.class, ZonedDateTime.class, (value, config) -> {
            return value;
        });
        super.registerInternalConverter(OffsetDateTime.class, ZonedDateTime.class, (value, config) -> {
            return value.toZonedDateTime();
        });
        super.registerInternalConverter(OffsetTime.class, ZonedDateTime.class, (value, config) -> {
            return value.atDate(LocalDate.now()).toZonedDateTime();
        });
    }

    private void registerToOffsetDateTimeConverters() {
        super.registerInternalConverter(String.class, OffsetDateTime.class, (value, config) -> {
            Date date = parseDate(value);
            return date.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        super.registerInternalConverter(Instant.class, OffsetDateTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        super.registerInternalConverter(Calendar.class, OffsetDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        super.registerInternalConverter(Date.class, OffsetDateTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        super.registerInternalConverter(LocalDate.class, OffsetDateTime.class, (value, config) -> {
            return value.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        });
        super.registerInternalConverter(LocalTime.class, OffsetDateTime.class, (value, config) -> {
            return value.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        super.registerInternalConverter(LocalDateTime.class, OffsetDateTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        });
        super.registerInternalConverter(ZonedDateTime.class, OffsetDateTime.class, (value, config) -> {
            return value.toOffsetDateTime();
        });
        super.registerInternalConverter(OffsetDateTime.class, OffsetDateTime.class, (value, config) -> {
            return value;
        });
        super.registerInternalConverter(OffsetTime.class, OffsetDateTime.class, (value, config) -> {
            return value.atDate(LocalDate.now());
        });
    }

    private void registerToOffsetTimeConverters() {
        super.registerInternalConverter(String.class, OffsetTime.class, (value, config) -> {
            Date date = parseDate(value);
            return date.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        super.registerInternalConverter(Instant.class, OffsetTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        super.registerInternalConverter(Calendar.class, OffsetTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        super.registerInternalConverter(Date.class, OffsetTime.class, (value, config) -> {
            return value.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        super.registerInternalConverter(LocalDate.class, OffsetTime.class, (value, config) -> {
            return value.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        super.registerInternalConverter(LocalTime.class, OffsetTime.class, (value, config) -> {
            return value.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        super.registerInternalConverter(LocalDateTime.class, OffsetTime.class, (value, config) -> {
            return value.atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
        });
        super.registerInternalConverter(ZonedDateTime.class, OffsetTime.class, (value, config) -> {
            return value.toOffsetDateTime().toOffsetTime();
        });
        super.registerInternalConverter(OffsetDateTime.class, OffsetTime.class, (value, config) -> {
            return value.toOffsetTime();
        });
        super.registerInternalConverter(OffsetTime.class, OffsetTime.class, (value, config) -> {
            return value;
        });
    }
}

