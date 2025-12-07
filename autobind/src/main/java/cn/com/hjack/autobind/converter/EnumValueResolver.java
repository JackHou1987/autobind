/**
 *
 */
package cn.com.hjack.autobind.converter;

import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.binder.DefaultResult;
import cn.com.hjack.autobind.binder.TypeWrappers;
import cn.com.hjack.autobind.utils.CastUtils;

import java.util.Objects;


/**
 * @ClassName: EnumValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年9月1日
 */
public class EnumValueResolver extends AbstractResolvableConverter {

    public static EnumValueResolver instance = new EnumValueResolver();

    private EnumValueResolver() {

    }

    @Override
    protected <T> Result<T> doConvert(Object source, TypeWrapper targetType, ResolveConfig config) {
        if (targetType == null || targetType.resolve() == null) {
            return DefaultResult.errorResult("target class can not be null");
        }
        if (source == null) {
            return DefaultResult.successResult();
        }
        Class<?> resolveClass = targetType.resolve();
        if (!resolveClass.isEnum()) {
            return DefaultResult.errorResult("target should be enum");
        }
        try {
            if (source instanceof String) {
                return findMatchedEnum(resolveClass, (String) source);
            } else {
                Result<Object> childResult = convertToString(source, config);
                if (childResult.success()) {
                    return findMatchedEnum(resolveClass, ((String) childResult.instance()));
                } else {
                    throw new IllegalStateException("can not convert source to enum, no matched name");
                }
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(e.getMessage());
        }
    }

    private <T> Result<T> findMatchedEnum(Class<?> enumClass, String name) {
        Enum<?>[] enums = (Enum[]) enumClass.getEnumConstants();
        for (Enum<?> anEnum : enums) {
            String enumName = anEnum.name();
            if (Objects.equals(name, enumName)) {
                return CastUtils.castSafe(DefaultResult.successResult(anEnum));
            }
        }
        throw new IllegalStateException("can not convert source to enum, no matched name");
    }

    private Result<Object> convertToString(Object source, ResolveConfig config) throws Exception {
        Result<Object> result = ResolvableConverters.getConverter(String.class).convert(source, TypeWrappers.getType(String.class), config);
        if (!result.success()) {
            return DefaultResult.errorResult(result.resultMsg());
        } else {
            return DefaultResult.successResult(result.instance());
        }
    }

}
