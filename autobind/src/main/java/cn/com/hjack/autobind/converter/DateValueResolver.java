package cn.com.hjack.autobind.converter;

import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.utils.CastUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


/**
 *   日期converter
 * @author houqq
 * @date: 2025年7月10日
 */
public class DateValueResolver extends AbstractResolvableConverter {

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


    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        if (source == null) {
            return DefaultResult.successResult();
        } else {
            Class<?> targetClass = targetType.resolve();
            try {
                InternalConverter<Object, ResolveConfig, Object> converter = CastUtils.castSafe(getInternalConverter(source.getClass(), targetClass));
                if (converter == null) {
                    throw new IllegalStateException("can not convert source to number");
                } else {
                    return DefaultResult.successResult(CastUtils.castSafe(converter.convert(source, config)));
                }
            } catch (Exception e) {
                return DefaultResult.errorResult(e.getMessage());
            }
        }
    }

    private void registerToDateConverters() {
        registerInternalConverter(String.class, Date.class, (value, config) -> {
            return CastUtils.parseDateTime((String) value);
        });
        registerInternalConverter(Date.class, Date.class, (value, config) -> {
            return value;
        });
        registerInternalConverter(Instant.class, Date.class, (value, config) -> {
            return Date.from(value);
        });
        registerInternalConverter(LocalDate.class, Date.class, (value, config) -> {
            return Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant());
        });
        registerInternalConverter(LocalTime.class, Date.class, (value, config) -> {
            Instant instant = LocalDate.now().atTime(value).atZone(ZoneId.systemDefault()).toInstant();
            return Date.from(instant);
        });
        registerInternalConverter(LocalDateTime.class, Date.class, (value, config) -> {
            return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
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
            return Objects.requireNonNull(CastUtils.parseDateTime(value)).toInstant();
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
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Objects.requireNonNull(CastUtils.parseDateTime(value)));
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
            Instant instant = LocalDate.now().atTime(value).atZone(ZoneId.systemDefault()).toInstant();
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
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(value.toInstant()));
            return calendar;
        });
        registerInternalConverter(OffsetDateTime.class, Calendar.class, (value, config) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(value.toInstant()));
            return calendar;
        });
        registerInternalConverter(OffsetTime.class, Calendar.class, (value, config) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(LocalDate.now().atTime(value).toInstant()));
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
            return CastUtils.parseLocalDate(value);
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
            return CastUtils.parseLocalTime(value);
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
            return CastUtils.parseLocalDateTime(value);
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
            Date date = CastUtils.parseDateTime(value);
            if (date == null) {
                return null;
            } else {
                Instant instant = date.toInstant();
                return instant.atZone(ZoneId.systemDefault());
            }
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
            Date date = CastUtils.parseDateTime(value);
            if (date == null) {
                return null;
            } else {
                return date.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
            }
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
            Date date = CastUtils.parseDateTime(value);
            if (date == null) {
                return null;
            } else {
                return date.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toOffsetTime();
            }
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
