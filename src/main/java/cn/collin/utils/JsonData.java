package cn.collin.utils;

import net.sf.json.JSONObject;

/**
 * Created by Collin on 2017/4/16.
 */
public class JsonData {
    private JSONObject jsonObject = new JSONObject();
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

    public String getTransData (String serverId, String invokeId, int dataType, String timestamp, boolean result, String id) {
        jsonObject.put("serverId", serverId);
        jsonObject.put("invokeId", invokeId);
        jsonObject.put("dataType", dataType);
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("result", result);
        jsonObject.put("id", id);
        System.out.println(jsonObject.toString());
        return jsonObject.toString();
    }
}
