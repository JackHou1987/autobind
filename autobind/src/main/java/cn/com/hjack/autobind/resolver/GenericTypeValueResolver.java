/**
 *
 */
package cn.com.hjack.autobind.resolver;

import cn.com.hjack.autobind.resolver.AbstractGenericTypeValueResolver;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


/**
 * @ClassName: GenericTypeValueResolver
 * @Description: TODO
 * @author houqq
 * @date: 2025年7月15日
 *
 */
public class GenericTypeValueResolver extends AbstractGenericTypeValueResolver {

    public static GenericTypeValueResolver instance = new GenericTypeValueResolver();

    private GenericTypeValueResolver() {
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T createObjectAndSetGenericValue(Class<?> targetType, Object value) {
        if (targetType == Optional.class) {
            return (T) Optional.of(value);
        } else if (targetType == ThreadLocal.class) {
            ThreadLocal<Object> threadLocal = new ThreadLocal<>();
            threadLocal.set(value);
            return (T) threadLocal;
        } else if (targetType == CompletableFuture.class) {
            CompletableFuture<Object> future = new CompletableFuture<>();
            future.complete(value);
            return (T) future;
        } else if (targetType == SoftReference.class) {
            return (T) new SoftReference<>(value);
        } else if (targetType == WeakReference.class) {
            return (T) new WeakReference<>(value);
        } else {
            throw new IllegalStateException("unkown generic type");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T createObject(Class<?> targetType) {
        if (targetType == Optional.class) {
            return (T) Optional.empty();
        } else if (targetType == ThreadLocal.class) {
            return (T) new ThreadLocal<>();
        } else if (targetType == CompletableFuture.class) {
            return (T) new CompletableFuture<>();
        } else if (targetType == SoftReference.class) {
            return (T) new SoftReference<>(null);
        } else if (targetType == WeakReference.class) {
            return (T) new WeakReference<>(null);
        } else {
            throw new IllegalStateException("unkown generic type");
        }
    }

}
