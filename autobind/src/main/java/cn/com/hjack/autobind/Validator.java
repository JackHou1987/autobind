package cn.com.hjack.autobind;


@FunctionalInterface
public interface Validator {

    void validate(Object instance, ValidationErrors errors);
}
