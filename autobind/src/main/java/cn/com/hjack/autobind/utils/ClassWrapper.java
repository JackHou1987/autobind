/**
 *
 */
package cn.com.hjack.autobind.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.com.hjack.autobind.AutoBindField;
import cn.com.hjack.autobind.ResolveConfig;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import com.google.common.base.Strings;


/**
 * @ClassName: ClassCache
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月19日
 *
 */
public class ClassWrapper {

    /**
     * key->field name value->field wrapper
     */
    private Map<String, FieldWrapper> fields = new HashMap<>();

    private Map<String, ClassWrapper> javaBeanFieldClasses = new HashMap<>();

    private Map<String, FieldWrapper> javaBeanFields = new HashMap<>();

    private static Map<Class<?>, ClassWrapper> classCache = new ConcurrentHashMap<>();

    private Class<?> objectClass;

    public static ClassWrapper forClass(Class<?> objectClass) {
        if (objectClass == null) {
            throw new IllegalStateException("service class can not be null");
        }
        return classCache.computeIfAbsent(objectClass, ClassWrapper::new);
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
        return fields;
    }

    public Map<String, FieldWrapper> getSendFieldNameMap() {
        if (MapUtils.isEmpty(fields)) {
            return new HashMap<>();
        } else {
            Map<String, FieldWrapper> result = new HashMap<>();
            fields.forEach((fieldName, fieldWrapper) -> {
                Map<String, FieldWrapper> map = fieldWrapper.getSendFieldNameMap();
                if (!MapUtils.isEmpty(map)) {
                    result.putAll(fieldWrapper.getSendFieldNameMap());
                }
            });
            return result;
        }
    }

    public Map<String, FieldWrapper> getJavaBeanFieldMap() {
        return javaBeanFields;
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
                AutoBindField autoBind = field.getAnnotation(AutoBindField.class);
                if (autoBind != null && autoBind.exclude()) {
                    return;
                }
                FieldWrapper fieldWrapper = new FieldWrapper(clsDesc, field, getterMethod, setterMethod);
                if (TypeUtils.isJavaBeanClass(field.getType())) {
                    javaBeanFieldClasses.put(field.getName(), ClassWrapper.forClass(field.getType()));
                    javaBeanFields.put(field.getName(), fieldWrapper);
                }
                fields.put(field.getName(), fieldWrapper);
            }
        });
    }
    public Field findField(String fieldName) {
        if (Strings.isNullOrEmpty(fieldName)) {
            return null;
        } else {
            FieldWrapper desc = fields.get(fieldName);
            if (desc == null) {
                return null;
            } else {
                return desc.getField();
            }
        }
    }

    public FieldWrapper getField(String fieldName) {
        if (Strings.isNullOrEmpty(fieldName)) {
            return null;
        } else {
            return fields.get(fieldName);
        }
    }
    public FieldWrapper.FieldNodeSlot getFieldSlotByFieldName(String fieldName) {
        return this.getFieldSlotByFieldName(fieldName, new HashMap<>(), ResolveConfig.defaultConfig);
    }

    public FieldWrapper.FieldNodeSlot getFieldSlotByFieldName(String fieldName, ResolveConfig config) {
        return this.getFieldSlotByFieldName(fieldName, new HashMap<>(), Optional.ofNullable(config).orElse(ResolveConfig.defaultConfig));
    }

    private FieldWrapper.FieldNodeSlot getFieldSlotByFieldName(String fieldName, Map<Class<?>, Class<?>> circularDetectClassMap, ResolveConfig config) {
        if (Strings.isNullOrEmpty(fieldName)) {
            return new FieldWrapper.FieldNodeSlot();
        }
        FieldWrapper.FieldNodeSlot fieldNodeSlot = new FieldWrapper.FieldNodeSlot();
        FieldWrapper fieldWrapper = fields.get(fieldName);
        if (fieldWrapper == null) {
            if (!config.deepSeek()) {
                return fieldNodeSlot;
            } else {
                if (javaBeanFieldClasses == null || javaBeanFieldClasses.isEmpty()) {
                    return fieldNodeSlot;
                } else {
                    Set<Map.Entry<String, ClassWrapper>> entries = javaBeanFieldClasses.entrySet();
                    for (Map.Entry<String, ClassWrapper> entry : entries) {
                        if (entry.getValue() == null || entry.getKey() == null) {
                            continue;
                        }
                        if (circularDetectClassMap.get(entry.getValue().getBeanCls()) == null) {
                            circularDetectClassMap.put(entry.getValue().getBeanCls(), getBeanCls());
                            FieldWrapper.FieldNodeSlot next = entry.getValue().getFieldSlotByFieldName(fieldName, circularDetectClassMap, config);
                            FieldWrapper current = this.fields.get(entry.getKey());
                            if (current == null) {
                                throw new IllegalStateException("can not find matched field");
                            }
                            fieldNodeSlot.setCurrent(current);
                            fieldNodeSlot.setNext(next);
                            return fieldNodeSlot;
                        }
                    }
                    return fieldNodeSlot;
                }
            }
        } else {
            fieldNodeSlot.setCurrent(fieldWrapper);
            return fieldNodeSlot;
        }
    }
    public Class<?> getBeanCls() {
        return this.objectClass;
    }

}
