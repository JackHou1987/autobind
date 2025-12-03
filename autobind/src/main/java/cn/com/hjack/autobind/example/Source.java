/**
 *
 */
package cn.com.hjack.autobind.example;

import cn.com.hjack.autobind.AutoBindField;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @ClassName: SourceDTO
 * @Description: TODO
 * @author houqq
 * @date: 2025年6月5日
 *
 */
public class Source {

    private String src2;

    private List<String> src3;

    private SourceSub src4;

    private List<Map<String, SourceSub>> src5;

    private SourceSub src6;

    private SourceSub src7;

    @AutoBindField
    private SourceSub src8;

    private List<List<Map<String, SourceSub>>> src9;

    private Date src10;

//	private Map<String, SourceSub> src11;

    private Map<String, Object> src12;

    private SourceParent src11;

    private List<List<Integer>> src13;

    private String src14;

    private Collection<String> src15;

    private Integer src16;

    private byte src18;

    public String getSrc2() {
        return src2;
    }

    public void setSrc2(String src2) {
        this.src2 = src2;
    }

    public List<String> getSrc3() {
        return src3;
    }

    public void setSrc3(List<String> src3) {
        this.src3 = src3;
    }

    public SourceSub getSrc4() {
        return src4;
    }

    public void setSrc4(SourceSub src4) {
        this.src4 = src4;
    }

    public List<Map<String, SourceSub>> getSrc5() {
        return src5;
    }

    public void setSrc5(List<Map<String, SourceSub>> src5) {
        this.src5 = src5;
    }

    public SourceSub getSrc6() {
        return src6;
    }

    public void setSrc6(SourceSub src6) {
        this.src6 = src6;
    }

    public SourceSub getSrc7() {
        return src7;
    }

    public void setSrc7(SourceSub src7) {
        this.src7 = src7;
    }

    public SourceSub getSrc8() {
        return src8;
    }

    public void setSrc8(SourceSub src8) {
        this.src8 = src8;
    }

    public List<List<Map<String, SourceSub>>> getSrc9() {
        return src9;
    }

    public void setSrc9(List<List<Map<String, SourceSub>>> src9) {
        this.src9 = src9;
    }

    public Date getSrc10() {
        return src10;
    }

    public void setSrc10(Date src10) {
        this.src10 = src10;
    }

    public SourceParent getSrc11() {
        return src11;
    }

    public void setSrc11(SourceParent src11) {
        this.src11 = src11;
    }

    public Map<String, Object> getSrc12() {
        return src12;
    }

    public void setSrc12(Map<String, Object> src12) {
        this.src12 = src12;
    }

    public static class SourceSub {

        private String sub1;

        @AutoBindField(scale = 2, roundingMode = RoundingMode.HALF_UP)
        private double sub2;

        private Date sub3;

        public String getSub1() {
            return sub1;
        }

        public void setSub1(String sub1) {
            this.sub1 = sub1;
        }

        public double getSub2() {
            return sub2;
        }

        public void setSub2(double sub2) {
            this.sub2 = sub2;
        }

        public Date getSub3() {
            return sub3;
        }

        public void setSub3(Date sub3) {
            this.sub3 = sub3;
        }

    }

    public static class SourceParent {

        private SourceSub targetSub1;

        public SourceSub getTargetSub1() {
            return targetSub1;
        }

        public void setTargetSub1(SourceSub targetSub1) {
            this.targetSub1 = targetSub1;
        }

    }

    public List<List<Integer>> getSrc13() {
        return src13;
    }

    public void setSrc13(List<List<Integer>> src13) {
        this.src13 = src13;
    }

    public String getSrc14() {
        return src14;
    }

    public void setSrc14(String src14) {
        this.src14 = src14;
    }

    public Collection<String> getSrc15() {
        return src15;
    }

    public void setSrc15(Collection<String> src15) {
        this.src15 = src15;
    }

    public Integer getSrc16() {
        return src16;
    }

    public void setSrc16(Integer src16) {
        this.src16 = src16;
    }

    public byte getSrc18() {
        return src18;
    }

    public void setSrc18(byte src18) {
        this.src18 = src18;
    }


}
