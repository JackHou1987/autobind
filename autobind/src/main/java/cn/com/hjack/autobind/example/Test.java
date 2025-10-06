package cn.com.hjack.autobind.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.com.hjack.autobind.TypeReference;
import cn.com.hjack.autobind.ResolveConfig;
import cn.com.hjack.autobind.Result;
import cn.com.hjack.autobind.example.Target.TargetSub1;
import cn.com.hjack.autobind.utils.ConvertUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.SpringProperties;
import org.springframework.validation.DataBinder;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;

import cn.hutool.core.convert.Convert;


/**
 * @ClassName: DataBindUtilsTest
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月3日
 *
 */
public class Test {

    private static final Log logger = LogFactory.getLog(DataBinder.class);

    private static final Log loggers = LogFactory.getLog(SpringProperties.class);

    public static void main(String[] args) {
        //		testToList();
        testToObject();
    }


//    private static void testToList() {
//        Map<String, Source.SourceSub> map5 = new HashMap<>();
//        Source.SourceSub sourceSub5 = new Source.SourceSub();
//        sourceSub5.setSub1("sub5");
//        sourceSub5.setSub2(0);
//        sourceSub5.setSub3(new Date());
//        map5.put("key", sourceSub5);
//        List<Map<String, Source.SourceSub>> list = new ArrayList<>();
//        list.add(map5);
//
//        Result<List<Map<String, TargetSub1>>> result = ConvertUtils.toList(list, new TypeReference<List<Map<String, TargetSub1>>>() {}, null);
//        System.out.println(JSONObject.toJSONString(result.instance(), SerializerFeature.SortField));
//        List<Map<String, Target.TargetSub1>> target = Convert.convert(new cn.hutool.core.lang.TypeReference<List<Map<String, Target.TargetSub1>>>() {}, list);
//        System.out.println(JSONObject.toJSONString(target, SerializerFeature.SortField));
//
//        List<List<Map<String, Source.SourceSub>>> src9 = new ArrayList<>();
//        Map<String, Source.SourceSub> map9 = new HashMap<>();
//        Source.SourceSub sourceSub9 = new Source.SourceSub();
//        sourceSub9.setSub1("sub6");
//        sourceSub9.setSub2(0);
//        sourceSub9.setSub3(new Date());
//        map9.put("sub9map", sourceSub9);
//        src9.add(Lists.newArrayList(map9));
//
//        ResolveConfig config = ResolveConfig.builder().build();
//        Result<List<List<Map<String, Target.TargetSub1>>>> result2 = ConvertUtils.toList(src9, new TypeReference<List<List<Map<String, Target.TargetSub1>>>>() {}, config);
//        System.out.println(JSONObject.toJSONString(result2.instance(), SerializerFeature.SortField));
//        List<List<Map<String, Target.TargetSub1>>> target2 = Convert.convert(new cn.hutool.core.lang.TypeReference<List<List<Map<String, Target.TargetSub1>>>>() {}, src9);
//        System.out.println(JSONObject.toJSONString(target2, SerializerFeature.SortField));
//
//    }

    @SuppressWarnings("unchecked")
    private static void testToObject() {
        Source source = new Source();
        source.setSrc2("1000");
        source.setSrc3(Lists.newArrayList("311", "322"));
        Source.SourceSub sourceSub = new Source.SourceSub();
        sourceSub.setSub1("sub1");
        sourceSub.setSub2(700.98);
        sourceSub.setSub3(new Date());
        source.setSrc4(sourceSub);
        Map<String, Source.SourceSub> map = new HashMap<>();
        map.put("key", sourceSub);
        List<Map<String, Source.SourceSub>> list = new LinkedList<>();
        list.add(map);
        source.setSrc5(list);
        source.setSrc6(sourceSub);
        source.setSrc7(sourceSub);
        source.setSrc8(sourceSub);
        source.setSrc10(new Date());
        List<List<Map<String, Source.SourceSub>>> src9 = new ArrayList<>();
        src9.add(Lists.newArrayList(map));
        source.setSrc9(src9);
        Map<String, Object> src12Map = new HashMap<>();
        src12Map.put("sub1", "testsub1");
        src12Map.put("sub2", 100);
        source.setSrc12(src12Map);
        Source.SourceParent parent = new Source.SourceParent();
        parent.setTargetSub1(sourceSub);
        source.setSrc11(parent);
        List<Integer> innerList = Lists.newArrayList(10, 20);
        List<List<Integer>> src13 = new ArrayList<>();
        src13.add(innerList);
        source.setSrc13(src13);
        ResolveConfig config = ResolveConfig.builder().fastMode(true).build();
        Result<Target<Target.TargetSub3<String, Integer>>> result = ConvertUtils.toObject(source, new TypeReference<Target<Target.TargetSub3<String, Integer>>>() {}, config);
        System.out.println(JSONObject.toJSONString(result.instance(), SerializerFeature.SortField));
    }

}
