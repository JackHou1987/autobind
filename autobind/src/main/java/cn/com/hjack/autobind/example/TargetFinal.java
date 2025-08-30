/**
 *
 */
package cn.com.hjack.autobind.example;

import java.util.List;

import cn.com.hjack.autobind.TypeWrapper;
import cn.com.hjack.autobind.type.DefaultFieldTypeWrapper;
import org.springframework.util.ReflectionUtils;

/**
 * @ClassName: TargetFinal
 * @Description: TODO
 * @author houqq
 * @date: 2025年8月4日
 *
 */
public class TargetFinal<T> extends TargetSub<T, Integer> {

    private List<T>[][] a;

    public static void main(String[] args) {
        DefaultFieldTypeWrapper fieldType = new DefaultFieldTypeWrapper(ReflectionUtils.findField(Target.class, "src11"), Target.class);
        System.out.println(fieldType.getGeneric(1).resolve());
        TypeWrapper typeWrapper = fieldType.resolveGeneric(fieldType.getGeneric(1));
        //		Type type = TypeUtils.getComponentNonArrayType(ReflectionUtils.findField(Target.class, "src11").getGenericType());
        System.out.println(typeWrapper.resolve());
        // Type type1 = TypeUtils.getClassOrParameterizedType(TargetImpl.class, TargetFinal.class);
        //		TargetFinal finals = new TargetFinal();
        //		ResolvableType resolvableType = ResolvableType.forClass(TargetImpl.class, finals.getClass());
        //		Field field = ReflectionUtils.findField(TargetFinal.class, "t");
        //		Type type = ((GenericArrayType) field.getGenericType()).getGenericComponentType();
        //		ResolvableType resolvableType = ResolvableType.forField(ReflectionUtils.findField(TargetFinal.class, "t"), TargetFinal.class);
        //		TypeWrapper type = TypeWrappers.getType(TargetImpl.class, TargetFinal.class);
        //		FieldTypeWrapper wrapper = TypeWrappers.getFieldType(ReflectionUtils.findField(TargetImpl.class, "field"), TargetFinal.class, type);
        //		System.out.println(JSONObject.toJSONString(((ParameterizedType) type1).getActualTypeArguments()));
        //		Type type2 = TypeUtils.getClassOrParameterizedType(TargetSub.class, TargetFinal.class);
        //		System.out.println(JSONObject.toJSONString(((ParameterizedType) type2).getActualTypeArguments()));

    }
}
