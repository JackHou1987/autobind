/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.factory.TypeWrappers;
import cn.com.hjack.autobind.utils.Constants;
import cn.com.hjack.autobind.validation.DefaultResult;

import java.util.Objects;


/**
 * @ClassName: EnumValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年9月1日
 */
public class EnumValueResolver extends AbstractTypeValueResolver {

    public static EnumValueResolver instance = new EnumValueResolver();

    @Override
    protected Result<Object> doResolveValue(Object source, TypeWrapper targetType, ResolveConfig config)
            throws Exception {
        if (targetType == null || targetType.resolve() == null) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "target class can not be null");
        }
        if (source == null) {
            return DefaultResult.defaultSuccessResult(null);
        }
        Class<?> resolveClass = targetType.resolve();
        if (!resolveClass.isEnum()) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, "target should be enum");
        }
        try {
            if (source instanceof String) {
                String name = (String) source;
                return findMatchedEnum(resolveClass, name);
            } else {
                Result<Object> childResult = this.convertToStr(source, config);
                if (!childResult.success()) {
                    return this.findMatchedEnum(resolveClass, ((String) childResult.instance()));
                } else {
                    throw new IllegalStateException("can not convert source to enum, no matched name");
                }
            }
        } catch (Exception e) {
            return DefaultResult.errorResult(null, Constants.FAIL_CODE, e.getMessage());
        }
    }

    private Result<Object> findMatchedEnum(Class<?> enumClass, String name) {
        Enum[] enums = (Enum[]) enumClass.getEnumConstants();
        for (Enum anEnum : enums) {
            String enumName = anEnum.name();
            if (Objects.equals(name, enumName)) {
                return DefaultResult.defaultSuccessResult(anEnum);
            }
        }
        throw new IllegalStateException("can not convert source to enum, no matched name");
    }

    private Result<Object> convertToStr(Object source, ResolveConfig config) throws Exception {
        Result<Object> result = new DefaultResult<Object>();
        Result<Object> childResult = StringValueResolver.instance.resolve(source, TypeWrappers.getType(String.class), ResolveConfig.copy(config));
        if (!childResult.success()) {
            result.setResultCode(Constants.FAIL_CODE);
            result.setResultMsg(childResult.resultMsg());
            return result;
        } else {
            result.setResultCode(Constants.RESULT_CODE_KEY);
            result.setResultMsg(Constants.RESULT_MSG_KEY);
            result.instance(childResult.instance());
            return result;
        }
    }

}
