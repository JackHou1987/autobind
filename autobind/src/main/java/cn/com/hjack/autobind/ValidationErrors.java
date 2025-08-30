package cn.com.hjack.autobind;


public interface ValidationErrors {
    /**
     * @Title: 收集error信息
     * @Description: TODO
     * @param: @param fieldName
     * @param: @param msg
     * @return: void
     * @throws
     */
    void collectError(String fieldName, String msg);
}
