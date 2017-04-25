package cn.collin.utils;

/**
 * Created by Collin on 2017/4/16.
 */
public class JsonData {
    public String getJsonData (String method, String chaincodeID, String function, String args, String id) {
        String jsonString = "{"+
            "\"jsonrpc\": \"2.0\","+
            "\"method\":\"" + method + "\","+
            "\"params\": {"+
                "\"type\": 1,"+
                "\"chaincodeID\":{" + chaincodeID + "}," +
                "\"ctorMsg\": {"+
                    "\"function\":\"" + function + "\"," +
                        "\"args\":" + args +
                "},"+
                "\"secureContext\": \"jim\""+
            "},"+
            "\"id\":"+ id +
        "}";
        return jsonString;
    }
}
