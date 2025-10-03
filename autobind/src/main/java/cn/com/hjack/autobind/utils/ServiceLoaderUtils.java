/**
 *
 */
package cn.com.hjack.autobind.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
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

    private static final String prefix = "META-INF/services/";

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

    @SuppressWarnings("unchecked")
    public static <T> Set<T> load(Class<T> type, String[] keyNames) {
        if (keyNames == null || keyNames.length == 0 || type == null) {
            return Collections.emptySet();
        } else {
            String path = prefix + type.getSimpleName();
            try {
                Set<String> results = new HashSet<>();
                Enumeration<URL> urls = TypeUtils.getClassLoader().getResources(path);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    results.addAll(load(url, keyNames));
                }
                Set<T> classes = new HashSet<>();
                for (String result : results) {
                    Class<?> cls = TypeUtils.getClassLoader().loadClass(result);
                    classes.add((T) cls.newInstance());
                }
                return classes;
            } catch (Throwable ex) {
                return Collections.emptySet();
            }
        }
    }

    public static Set<String> load(URL url, String[] keyNames) throws IOException {
        Set<String> result = new HashSet<>();
        if (keyNames == null || keyNames.length == 0) {
            return result;
        }
        try (InputStream is = url.openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            for (String keyName : keyNames) {
                for (;;) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String lineKey = null;
                    int index = line.indexOf('=');
                    if (index >= 0) {
                        lineKey = line.substring(0, index).trim();
                    } else {
                        continue;
                    }
                    if (lineKey.isEmpty()) {
                        continue;
                    }
                    if (!Objects.equals(lineKey, keyName)) {
                        continue;
                    }
                    result.add(line.substring(index + 1));
                }
            }
        }
        return result;
    }
}
