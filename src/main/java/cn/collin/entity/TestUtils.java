package cn.collin.entity;

/**
 * Created by Collin on 2017/4/17.
 */
public class TestUtils {
    private int testFrq = 0;
//    private String chaincodeId = "";
    private String testUrl = "";

    public TestUtils() {
    }

    public TestUtils(int testFrq, String testUrl) {
        this.testFrq = testFrq;
        this.testUrl = testUrl;
    }



    public String getTestUrl() {
        return testUrl;
    }

    public void setTestUrl(String testUrl) {
        this.testUrl = testUrl;
    }

//    public String getChaincodeId() {
//
//        return chaincodeId;
//    }
//
//    public void setChaincodeId(String chaincodeId) {
//        this.chaincodeId = chaincodeId;
//    }


    public int getTestFrq() {
        return testFrq;
    }

    public void setTestFrq(int testFrq) {
        this.testFrq = testFrq;
    }




}
