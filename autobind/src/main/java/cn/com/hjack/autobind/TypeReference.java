package cn.com.hjack.autobind;

public abstract class TypeReference<T> {

    private T instance;

    protected TypeReference(T instance) {
        this.instance = instance;
    }

    protected TypeReference() {
    }

    public T getInstance() {
        return instance;
    }
}
