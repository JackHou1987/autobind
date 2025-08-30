package cn.com.hjack.autobind.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.com.hjack.autobind.utils.FieldWrapper.FieldChainNode;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import org.springframework.util.StringUtils;



/**
 * @ClassName: ClassCache
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月19日
 *
 */
public class ClassWrapper {

    private Map<String, FieldWrapper> fieldNameMap = new HashMap<>();

    private Map<String, ClassWrapper> javaBeanFieldNameClassMap = new HashMap<>();

    private Map<String, FieldWrapper> javaBeanFieldMap = new HashMap<>();

    private static Map<Class<?>, ClassWrapper> classCache = new ConcurrentHashMap<>();

    private Class<?> objectClass;

    public static ClassWrapper forClass(Class<?> objectClass) {
        if (objectClass == null) {
            throw new IllegalStateException("service class can not be null");
        }
        return classCache.computeIfAbsent(objectClass, (key) -> {
            return new ClassWrapper(key);
        });
    }

    private ClassWrapper(Class<?> objectClass) {
        try {
            this.objectClass = objectClass;
            initFields(objectClass);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public Map<String, FieldWrapper> getFieldNameMap() {
        return fieldNameMap;
    }

    public Map<String, FieldWrapper> getSendFieldNameMap() {
        if (MapUtils.isEmpty(fieldNameMap)) {
            return new HashMap<>();
        } else {
            Map<String, FieldWrapper> result = new HashMap<>();
            fieldNameMap.forEach((fieldName, fieldDesc) -> {
                Map<String, FieldWrapper> map = fieldDesc.getSendFieldNameMap();
                if (!MapUtils.isEmpty(map)) {
                    result.putAll(fieldDesc.getSendFieldNameMap());
                }
            });
            return result;
        }
    }

    public Map<String, FieldWrapper> getJavaBeanFieldMap() {
        return javaBeanFieldMap;
    }

    private void initFields(Class<?> beanClass) throws Exception {
        ClassWrapper clsDesc = this;
        ReflectionUtils.doWithFields(beanClass, new FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (Modifier.isStatic(field.getModifiers())) {
                    return;
                }
                String getterMethodName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                String setterMethodName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Method setterMethod = null;
                Method getterMethod = null;
                try {
                    setterMethod = ReflectionUtils.findMethod(beanClass, setterMethodName, field.getType());
                    getterMethod = ReflectionUtils.findMethod(beanClass, getterMethodName);
                } catch (Exception e) {
                    return;
                }
                if (setterMethod == null || getterMethod == null) {
                    return;
                }
                FieldWrapper fieldWrapper = new FieldWrapper(clsDesc, field, getterMethod, setterMethod);
                if (TypeUtils.isJavaBeanClass(field.getType())) {
                    javaBeanFieldNameClassMap.put(field.getName(), ClassWrapper.forClass(field.getType()));
                    javaBeanFieldMap.put(field.getName(), fieldWrapper);
                }
                fieldNameMap.put(field.getName(),fieldWrapper);
            }
        });
    }

    public Field findField(String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            return null;
        } else {
            FieldWrapper desc = fieldNameMap.get(fieldName);
            if (desc == null) {
                return null;
            } else {
                return desc.getField();
            }
        }
    }

    public FieldChainNode findFieldChainByFieldName(String fieldName, Map<Class<?>, Class<?>> javaBeanFieldClass) {
        if (StringUtils.isEmpty(fieldName)) {
            return new FieldChainNode();
        } else {
            FieldChainNode fieldSlot = new FieldChainNode();
            FieldWrapper fieldWrapper = fieldNameMap.get(fieldName);
            if (fieldWrapper == null) {
                if (javaBeanFieldNameClassMap == null || javaBeanFieldNameClassMap.isEmpty()) {
                    return new FieldChainNode();
                } else {
                    Set<Map.Entry<String, ClassWrapper>> entries = javaBeanFieldNameClassMap.entrySet();
                    for (Map.Entry<String, ClassWrapper> entry : entries) {
                        if (entry.getValue() == null || entry.getKey() == null) {
                            continue;
                        }
                        if (javaBeanFieldClass.get(entry.getValue().getBeanCls()) != null) {
                            continue;
                        } else {
                            javaBeanFieldClass.put(entry.getValue().getBeanCls(), this.getBeanCls());
                        }
                        FieldChainNode next = entry.getValue().findFieldChainByFieldName(fieldName, javaBeanFieldClass);
                        if (next != null) {
                            FieldWrapper current = this.fieldNameMap.get(entry.getKey());
                            if (current == null) {
                                throw new IllegalStateException("can not find matched field");
                            }
                            if (current.getAutoBind() != null && current.getAutoBind().exclude()) {
                                continue;
                            }
                            fieldSlot.setCurrent(current);
                            fieldSlot.setNext(next);
                            return fieldSlot;
                        }
                    }
                    return fieldSlot;
                }
            } else {
                if (fieldWrapper.getAutoBind() != null && fieldWrapper.getAutoBind().exclude()) {
                    return fieldSlot;
                }
                fieldSlot.setCurrent(fieldWrapper);
                return fieldSlot;
            }
        }
    }

    public Class<?> getBeanCls() {
        return this.objectClass;
    }

}
