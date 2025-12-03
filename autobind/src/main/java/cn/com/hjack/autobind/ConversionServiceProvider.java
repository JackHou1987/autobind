package cn.com.hjack.autobind;


import java.util.ArrayList;
import java.util.List;

import cn.com.hjack.autobind.converters.BooleanToStringConverter;
import cn.com.hjack.autobind.converters.DateToStringConverter;
import cn.com.hjack.autobind.converters.StringToDateConverter;
import cn.com.hjack.autobind.utils.ServiceLoaderUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;


/**
 *
 * @ClassName: ConversionServiceProvider
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月4日
 *
 */
public class ConversionServiceProvider {

    private static DefaultConversionService conversionService = new DefaultConversionService();

    private static List<Converter<?, ?>> converters = new ArrayList<>();

    static {
        converters.add(new BooleanToStringConverter());
        converters.add(new DateToStringConverter());
        converters.add(new StringToDateConverter());
        converters.forEach(converter -> {
            conversionService.addConverter(converter);
        });
        ServiceLoaderUtils.loadClass(Converter.class).forEach(converter -> {
            converters.add(converter);
        });
    }

    public static ConversionService getGlobalConversionService() {
        return conversionService;
    }

    public static DefaultConversionService getConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        converters.forEach(converter -> {
            conversionService.addConverter(converter);
        });
        return conversionService;
    }

    public static DefaultConversionService getConversionService(ResolveConfig config) {
        DefaultConversionService conversionService = new DefaultConversionService();
        converters.forEach(converter -> {
            conversionService.addConverter(converter);
        });
        return conversionService;
    }

}