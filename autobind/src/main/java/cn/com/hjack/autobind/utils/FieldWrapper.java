package cn.com.hjack.autobind.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import cn.com.hjack.autobind.AutoBindField;

import com.google.common.base.Strings;



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
            if (!Strings.isNullOrEmpty(autoBind.recvFieldName())) {
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

    public String getFieldName() {
        return this.field.getName();
    }

    /**
     *    字段调用链节点，如java bean中，有field1字段，所属为java bean1，java bean1中有field2字段
     *    则field1->field2调用链为field1.getField1().getField2()
     * @author houqq
     * @date: 2025年9月3日
     */
    public static class FieldNodeSlot {

        /**
         * 调用链当前节点
         */
        private FieldWrapper current;

        /**
         * 调用链下一节点
         */
        private FieldNodeSlot next;

        public FieldNodeSlot getNext() {
            return next;
        }

        public void setNext(FieldNodeSlot next) {
            this.next = next;
        }

        public FieldWrapper getCurrent() {
            return current;
        }

        public void setCurrent(FieldWrapper current) {
            this.current = current;
        }

        public FieldNodeSlot(FieldWrapper current, FieldNodeSlot next) {
            this.current = current;
            this.next = next;
        }

        /**
         * @Title: getLeaf
         * @Description: 调用链叶子节点
         * @return: FieldWrapper
         */
        public FieldWrapper getLeafNode() {
            if (next != null) {
                return next.getLeafNode();
            } else {
                return current;
            }
        }

        /**
         * @Title: getFieldInvokeDesc
         * @Description: 得到字段调用链字符串
         * @return: String
         * @throws
         */
        public String getFieldInvokeDesc() {
            StringBuilder body = new StringBuilder();
            if (current != null) {
                body.append(this.current.getFieldName());
                if (next != null) {
                    String nextFieldStr = this.next.getFieldInvokeDesc();
                    if (Strings.isNullOrEmpty(nextFieldStr)) {
                        return body.toString();
                    } else {
                        body.append("." + nextFieldStr);
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

        public String generateInvokeStub(String methodName, String sourceObjectVarName) {
            if (Strings.isNullOrEmpty(methodName) || Strings.isNullOrEmpty(sourceObjectVarName)) {
                throw new IllegalStateException("method name or var name can not be null");
            } else {
                StringBuilder body = new StringBuilder();
                body.append(CastUtils.format("private Object %sValue(%s %s) {", methodName, TypeUtils.getCanonicalName(current.getClassWrapper().getBeanCls()), sourceObjectVarName));
                if (current == null) {
                    body.append(CastUtils.formatAndIndent2("return null;"));
                } else {
                    body.append(this.doGenerateInvokeStub(sourceObjectVarName));
                }
                body.append(CastUtils.format("}"));
                return body.toString();
            }
        }
        private String doGenerateInvokeStub(String sourceObjectVarName) {
            StringBuilder body = new StringBuilder();
            if (current.getFieldType().isPrimitive()) {
                String primitiveTypeName = TypeUtils.getPrimitiveClassWrapName(current.getFieldType());
                body.append(CastUtils.format("return CastUtils.toWrap%sValue(%s.%s());", primitiveTypeName, sourceObjectVarName, current.getReadMethod().getName()));
                return body.toString();
            } else {
                body.append(CastUtils.format("if (%s == null || %s.%s() == null) {", sourceObjectVarName, sourceObjectVarName, current.getReadMethod().getName()));
                if (this.current.autoBind != null && !Strings.isNullOrEmpty(this.current.autoBind.defaultValue())) {
                    body.append(CastUtils.formatAndIndent2("return %s;", current.autoBind.defaultValue()));
                } else {
                    body.append(CastUtils.formatAndIndent2("return null;"));
                }
                body.append(CastUtils.format("} else {"));
                body.append(CastUtils.formatAndIndent2("%s %sValue = %s.%s();", TypeUtils.getCanonicalName(current.getFieldType()), current.getFieldName(), sourceObjectVarName, current.getReadMethod().getName()));
                if (next != null) {
                    body.append(next.doGenerateInvokeStub(current.getFieldName() + "Value"));
                } else {
                    body.append(CastUtils.formatAndIndent2("return %sValue;", current.getFieldName()));
                }
                body.append(CastUtils.format("}"));
                return body.toString();
            }
        }

        public FieldNodeSlot() {
        }
    }

}
