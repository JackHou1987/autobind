/**
 *
 */
package cn.com.hjack.autobind;


/**
 * 本地代码生成接口，通过javaassit字节码工具直接生成本地代码避免反射，提升性能
 * @author houqq
 * @see cn.com.hjack.autobind.generater.Generaters
 */
public interface Generater<T> {

    T generate();

}
