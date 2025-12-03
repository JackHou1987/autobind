/**
 *
 */
package cn.com.hjack.autobind;

/**
 *    类型转换校验器，分为自定义校验器和基于EL表达式校验器，自定义校验器为全局校验器，EL表达式校验器为字段级校验器
 *    其中EL表达式在{@link cn.com.hjack.autobind.AutoBindField}中设置
 * @author houqq
 * @date: 2025年10月21日
 */
@FunctionalInterface
public interface Validator {

    /**
     * 校验Javabean字段值
     * @param object javabean对象
     * @param errors ValidationErrors
     */
    void validate(Object object, ValidationErrors errors);
}
