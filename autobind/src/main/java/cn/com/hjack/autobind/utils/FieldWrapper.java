package cn.com.hjack.autobind.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import cn.com.hjack.autobind.AutoBindField;
import org.springframework.util.StringUtils;



/**
 * @ClassName: FieldDesc
 * @Description: TODO
 * @author houqq
 * @date: 2025年7月1日
 *
 */
public class FieldWrapper {

    private Field field;

    private AutoBindField autoBind;

    private Method writeMethod;

    private Method readMethod;

    private String recvFieldName;

    private String[] sendFieldName;

    /**
     * declaring class
     */
    private ClassWrapper declaringClassWrapper;

    public FieldWrapper(ClassWrapper classWrapper, Field field, Method getterMethod, Method setterMethod) {
        if (field == null || getterMethod == null || setterMethod == null || classWrapper == null) {
            throw new IllegalArgumentException("field or property desc can not be null");
        }
        this.declaringClassWrapper = classWrapper;
        this.field = field;
        this.autoBind = field.getAnnotation(AutoBindField.class);
        readMethod = getterMethod;
        writeMethod = setterMethod;
        if (autoBind != null) {
            if (!StringUtils.isEmpty(autoBind.recvFieldName())) {
                recvFieldName = autoBind.recvFieldName();
            } else {
                recvFieldName = field.getName();
            }
            if (autoBind.sendFieldName() != null && autoBind.sendFieldName().length != 0) {
                sendFieldName = new String[autoBind.sendFieldName().length];
                System.arraycopy(autoBind.sendFieldName(), 0, sendFieldName, 0, autoBind.sendFieldName().length);
            } else {
                sendFieldName = new String[] {field.getName()};
            }
        } else {
            recvFieldName = field.getName();
            sendFieldName = new String[] {field.getName()};
        }
    }

    public Field getField() {
        return field;
    }

    public AutoBindField getAutoBind() {
        return autoBind;
    }

    public Method getWriteMethod() {
        return writeMethod;
    }

    public Method getReadMethod() {
        return readMethod;
    }

    public String getRecvFieldName() {
        return recvFieldName;
    }

    public String[] getSendFieldName() {
        return sendFieldName;
    }

    public Map<String, FieldWrapper> getSendFieldNameMap() {
        if (sendFieldName == null || sendFieldName.length == 0) {
            return new HashMap<>();
        } else {
            return Arrays.stream(sendFieldName).collect(Collectors.toMap((value) -> {
                return value;
            }, (value) -> {return this;}));
        }
    }

    public ClassWrapper getClassWrapper() {
        return declaringClassWrapper;
    }

    public Class<?> getFieldType() {
        return this.field.getType();
    }

    public String getFieldNmae() {
        return this.field.getName();
    }

    public static class FieldChainNode {

        private FieldWrapper current;

        private FieldChainNode next;

        public FieldChainNode getNext() {
            return next;
        }

        public void setNext(FieldChainNode next) {
            this.next = next;
        }

        public FieldWrapper getCurrent() {
            return current;
        }

        public void setCurrent(FieldWrapper current) {
            this.current = current;
        }

        public FieldChainNode(FieldWrapper current, FieldChainNode next) {
            this.current = current;
            this.next = next;
        }

        public FieldWrapper getLeaf() {
            if (next != null) {
                return next.getLeaf();
            } else {
                return current;
            }
        }

        public String getFieldInvokeDesc() {
            StringBuilder body = new StringBuilder();
            if (current != null) {
                body.append(this.current.getFieldNmae());
                if (next != null) {
                    String nextFieldStr = this.next.getFieldInvokeDesc();
                    if (StringUtils.isEmpty(nextFieldStr)) {
                        return body.toString();
                    } else {
                        body.append(".").append(nextFieldStr);
                        return body.toString();
                    }
                } else {
                    return body.toString();
                }

            } else {
                if (next != null) {
                    throw new IllegalStateException("bad field invoke node");
                }
                return body.toString();
            }
        }

        public FieldChainNode() {
        }
    }
}
