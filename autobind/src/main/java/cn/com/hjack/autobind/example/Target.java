/**
 *
 */
package cn.com.hjack.autobind.example;

import cn.com.hjack.autobind.AutoBindField;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;


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

    @AutoBindField(recvFieldName = "src3")
    private List<Integer> src3;

    //	@AutoBindField(recvFieldName = "src4")
    //	private Map<String, Object> src4;

    @AutoBindField(recvFieldName = "src4")
    private Map src4;

    private int[][] src13;
    //
    @AutoBindField(recvFieldName = "src5")
    private Map<String, TargetSub1>[] src5;
    //

    @AutoBindField(recvFieldName = "src7")
    private F src7;

    //	private ThreadLocal<TargetSub1> src8;

    @AutoBindField(recvFieldName = "src9")
    private Map<String, TargetSub1>[][] src9;

    @AutoBindField(recvFieldName = "src10", customConverter = DateToZonedDateTimeConverter.class)
    private ZonedDateTime src10;

    @AutoBindField(recvFieldName = "src11")
    private Map<String, TargetSub1> src11;
    //
    @AutoBindField(recvFieldName = "src12")
    private TargetSub1 src12;

    //	@AutoBindField(recvFieldName = "src3")
    //	private List target13;
    //
    //	@AutoBindField(recvFieldName = "src3")
    //	private Set<Integer> target14;

    public static class TargetSub1 {

        //        private String sub1;

        //        @AutoBindField(condition = "sub2 > 600", errorMsg = "error sub2", defaultValue = "200")
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

    //	public F getSrc7() {
    //		return src7;
    //	}
    //
    //	public void setSrc7(F src7) {
    //		this.src7 = src7;
    //	}

    //	public Map<String, TargetSub1>[][] getSrc9() {
    //		return src9;
    //	}
    //
    //	public void setSrc9(Map<String, TargetSub1>[][] src9) {
    //		this.src9 = src9;
    //	}

    public Map<String, TargetSub1> getSrc11() {
        return src11;
    }

    public void setSrc11(Map<String, TargetSub1> src11) {
        this.src11 = src11;
    }

    public TargetSub1 getSrc12() {
        return src12;
    }

    public void setSrc12(TargetSub1 src12) {
        this.src12 = src12;
    }

    public ZonedDateTime getSrc10() {
        return src10;
    }

    public void setSrc10(ZonedDateTime src10) {
        this.src10 = src10;
    }

    public List<Integer> getSrc3() {
        return src3;
    }

    public void setSrc3(List<Integer> src3) {
        this.src3 = src3;
    }

    public Map getSrc4() {
        return src4;
    }

    public void setSrc4(Map src4) {
        this.src4 = src4;
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

}
