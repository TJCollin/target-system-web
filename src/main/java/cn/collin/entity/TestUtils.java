package cn.collin.entity;

/**
 * Created by Collin on 2017/4/17.
 */
public class TestUtils {
    private int testFrq = 0;
//    private String chaincodeId = "";
    private String testUrl = "";
    private String type;
    private int interval;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public TestUtils(int testFrq, String type, int interval) {

        this.testFrq = testFrq;
        this.type = type;
        this.interval = interval;
    }

    public TestUtils() {
    }

    public TestUtils(int testFrq) {
        this.testFrq = testFrq;
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
