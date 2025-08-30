/**
 *
 */
package cn.com.hjack.autobind.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @ClassName: ClassLoadUtils
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月28日
 *
 */
public class ServiceLoaderUtils {

    public static <T> Set<T> loadClass(Class<T> type) {
        if (type == null) {
            return Collections.emptySet();
        } else {
            Set<T> sets = new HashSet<>();
            ServiceLoader<T> serviceLoader = ServiceLoader.load(type);
            for (T instance : serviceLoader) {
                sets.add(instance);
            }
            return sets;
        }
    }
}
