package cn.com.hjack.autobind.example;

import cn.com.hjack.autobind.AutoBindField;
import cn.com.hjack.autobind.ConvertFeature;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


/**
 * @ClassName: TargetDTO
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月5日
 *
 */
public class Target<F> {

    // @Min(value = 600, message = "min value must larger than 100")
//	@AutoBindField(condition = "src2 > 600", errorMsg = "error", defaultValue = "100")
    private BigInteger src2;

//	@AutoBindField(recvFieldName = "src3")
//	private List<Integer> src3;

    @AutoBindField(recvFieldName = "src4")
    private Map<String, Object> src4;

//	@AutoBindField(recvFieldName = "src4")
//	private Map src4;

    private int[][] src13;

    @AutoBindField(recvFieldName = "src5")
    private Map<String, TargetSub1>[] src5;
    //
    @AutoBindField(recvFieldName = "src7")
    private F src7;

//	private ThreadLocal<TargetSub1> src8;

    @AutoBindField(recvFieldName = "src9")
    private Map<String, TargetSub1>[][] src9;

    @AutoBindField(format = "yyyyMMdd HH:mm:ss")
    private String src10;

    @AutoBindField(recvFieldName = "src11")
    private Map<String, Object> src11;

    @AutoBindField(recvFieldName = "src12")
    private TargetSub1 src12;

    private Stream<Integer> src15;

    @AutoBindField(features = {ConvertFeature.BINARY})
    private String src16;

    @AutoBindField(recvFieldName = "src16", features = {ConvertFeature.PLAIN_STRING})
    private String src17;

    @AutoBindField(recvFieldName = "src3")
    private List target13;

    @AutoBindField(recvFieldName = "src3")
    private Set<Integer> target14;

    private char src18;

    public static class TargetSub1 {

//        private String sub1;

        @AutoBindField(condition = "sub2 > 600", errorMsg = "error sub2", defaultValue = "200")
//        @Min(value = 600, message = "min value must larger than 600")
        private long sub2;

        private LocalDate sub3;

//		public String getSub1() {
//			return sub1;
//		}
//
//		public void setSub1(String sub1) {
//			this.sub1 = sub1;
//		}

        public long getSub2() {
            return sub2;
        }

        public void setSub2(long sub2) {
            this.sub2 = sub2;
        }

        public LocalDate getSub3() {
            return sub3;
        }

        public void setSub3(LocalDate sub3) {
            this.sub3 = sub3;
        }

    }

    public static class TargetSub2 {

        private TargetSub1 targetSub1;

        public TargetSub1 getTargetSub1() {
            return targetSub1;
        }

        public void setTargetSub1(TargetSub1 targetSub1) {
            this.targetSub1 = targetSub1;
        }

    }

    public static class TargetSub3<T, E> {

        private T sub1;

        //        @AutoBindField(condition = "sub2 > 600", errorMsg = "error sub2")
        private E sub2;

        public T getSub1() {
            return sub1;
        }

        public void setSub1(T sub1) {
            this.sub1 = sub1;
        }

        public E getSub2() {
            return sub2;
        }

        public void setSub2(E sub2) {
            this.sub2 = sub2;
        }

    }

    public BigInteger getSrc2() {
        return src2;
    }

    public void setSrc2(BigInteger src2) {
        this.src2 = src2;
    }

    public Map<String, Object> getSrc4() {
        return src4;
    }

    public void setSrc4(Map<String, Object> src4) {
        this.src4 = src4;
    }

    public int[][] getSrc13() {
        return src13;
    }

    public void setSrc13(int[][] src13) {
        this.src13 = src13;
    }

    public Map<String, TargetSub1>[] getSrc5() {
        return src5;
    }

    public void setSrc5(Map<String, TargetSub1>[] src5) {
        this.src5 = src5;
    }

    public F getSrc7() {
        return src7;
    }

    public void setSrc7(F src7) {
        this.src7 = src7;
    }

    public Map<String, TargetSub1>[][] getSrc9() {
        return src9;
    }

    public void setSrc9(Map<String, TargetSub1>[][] src9) {
        this.src9 = src9;
    }

    public Map<String, Object> getSrc11() {
        return src11;
    }
    public void setSrc11(Map<String, Object> src11) {
        this.src11 = src11;
    }

    public TargetSub1 getSrc12() {
        return src12;
    }

    public void setSrc12(TargetSub1 src12) {
        this.src12 = src12;
    }

    public Stream<Integer> getSrc15() {
        return src15;
    }

    public void setSrc15(Stream<Integer> src15) {
        this.src15 = src15;
    }

    public List getTarget13() {
        return target13;
    }

    public void setTarget13(List target13) {
        this.target13 = target13;
    }

    public Set<Integer> getTarget14() {
        return target14;
    }

    public void setTarget14(Set<Integer> target14) {
        this.target14 = target14;
    }

    public String getSrc16() {
        return src16;
    }

    public void setSrc16(String src16) {
        this.src16 = src16;
    }

    public String getSrc10() {
        return src10;
    }

    public void setSrc10(String src10) {
        this.src10 = src10;
    }

    public String getSrc17() {
        return src17;
    }

    public void setSrc17(String src17) {
        this.src17 = src17;
    }

    public char getSrc18() {
        return src18;
    }

    public void setSrc18(char src18) {
        this.src18 = src18;
    }

}
