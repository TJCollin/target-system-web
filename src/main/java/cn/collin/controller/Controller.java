package cn.collin.controller;

import cn.collin.entity.TestUtils;
import cn.collin.entity.Users;
import cn.collin.service.ConnDB;
import cn.collin.service.PostService;
import cn.collin.utils.JsonData;
import cn.collin.utils.PayLoadData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Collin on 2017/4/14.
 */
@RestController
@RequestMapping("/chaincode")
public class Controller {
//    private PostService postService = new PostService();
    private JsonData jsonData = new JsonData();
    private PayLoadData payLoadData = new PayLoadData();
    private String postUrl = "http://202.120.167.86:7050/chaincode";
    private String CCID = "";
    private String invokeId = "";
    private String status = "";
    private JSONObject jsonObject = new JSONObject();

    //
    boolean success = false;

    //data send to kafka when the invoke starts and ends
    private String startData = "";
    private String endData = "";

    //sendTS send timestamp
    //recvTS receive timestamp
    private String sendTS, recvTS;
    //timestamp format
    private SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss:SSS");
    
    
    private String startInvokeURL = "http://localhost:8080/startInvoke";
    private String endInvokeURL = "http://localhost:8080/endInvoke";
    private String endTestURL = "http://localhost:8080/endTest";


    @Autowired
    private ConnDB connDB;

    @Autowired
    private RestTemplate restTemplate;

    //init the chaincode
    @RequestMapping(value = "/init", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String init(@RequestBody Users users) {
        String valA, valB, valC, valD, valE, args, chaincodeID, payLoad;

        //get the value to init
        ArrayList<String> arrayList = new ArrayList<>();
        valA = users.getValA();
        valB = users.getValB();
        valC = users.getValC();
        valD = users.getValD();
        valE = users.getValE();
        arrayList.add("\"a\"");
        arrayList.add("\"" + valA + "\"");
        arrayList.add(" \"b\" ");
        arrayList.add("\"" + valB + "\"");
        arrayList.add("\"c\"");
        arrayList.add("\"" + valC + "\"");
        arrayList.add("\"d\"");
        arrayList.add("\"" + valD + "\"");
        arrayList.add("\"e\"");
        arrayList.add("\"" + valE + "\"");

        //form the data to post
        args = arrayList.toString();
        System.out.println(args);
        chaincodeID = " \"path\":\"github.com/hyperledger/fabric/examples/chaincode/go/zhangcong\" ";
        payLoad = jsonData.getJsonData("deploy", chaincodeID, "init", args, "1");
        HttpEntity<String> entity = payLoadData.getPayLoad(payLoad);
        Object object = restTemplate.postForObject(postUrl, entity, String.class);

        //analyse the response
        jsonObject = JSONObject.fromObject(object);
        String result = JSONObject.fromObject(jsonObject.getString("result")).toString();
        String status = JSONObject.fromObject(result).getString("status");

        if (status.equals("OK")) {
            CCID = JSONObject.fromObject(result).getString("message");
            System.out.println(CCID);
            return CCID;
        } else {
            CCID = "fail";
            return CCID;
        }
    }

    //query one account at a time
    @RequestMapping(value = "/singleQuery", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String singleQuery(@RequestBody Users user) {
        if (CCID.equals("fail") || CCID.equals("")) {
            return "query fail";
        } else {
            //form data
            String args = "[\"" + user.getQueryUser() + "\"]";
            String chaincodeID = "\"name\":\"" + CCID + "\"";
            String payLoad = jsonData.getJsonData("query", chaincodeID, "query", args, "5");
            System.out.println(payLoad.toString());

            //post data
            HttpEntity<String> entity = payLoadData.getPayLoad(payLoad);
            Object object = restTemplate.postForObject(postUrl, entity, String.class);

            //analyse the response
            String result = JSONObject.fromObject(object).getString("result");
            String queryData = JSONObject.fromObject(result).getString("message");
            System.out.println(object.toString());
            return queryData;
        }
    }

    //query chaincode
    @RequestMapping(value = "/query", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public JSONArray query() {
        Users users = new Users();
        int fstLetter = (int) 'a';
        String[] queryResult = new String[5];

        //query all account
        for (int i = 0; i < 5; i++) {
            String queryUser = (char) (fstLetter + i % 5) + "";
            users.setQueryUser(queryUser);
            queryResult[i] = singleQuery(users);
        }
        JSONArray jsonArray = JSONArray.fromObject(queryResult);
        return jsonArray;
    }

    @RequestMapping(value = "/invoke", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String invoke(@RequestBody Users invokeUsers) {
        System.out.println("invokeUsers = [" + invokeUsers.getInvokeA() + "]");
        if (CCID.equals("fail") || CCID.equals("")) {
            System.out.println("query fail");
            return "query fail";
        } else {
            //form the json data for invoke
            String args = "[" + "\"" + invokeUsers.getInvokeA() + "\"" + "," + "\"" + invokeUsers.getInvokeB() + "\"" + "," + "\"" + invokeUsers.getAmount() + "\"" + "]";
            String chaincodeID = "\"name\":\"" + CCID + "\"";
            String payLoad = jsonData.getJsonData("invoke", chaincodeID, "invoke", args, "3");
            System.out.println(payLoad.toString());

            //form the post entity
            HttpEntity<String> entity = payLoadData.getPayLoad(payLoad);
            System.out.println("invokeA:" + invokeUsers.getInvokeA() + "  invokeB:" + invokeUsers.getInvokeB());

            //record the start data
            sendTS = formatter.format(new Date(System.currentTimeMillis()));
            startData = jsonData.getTransData(postUrl, "none", 0, sendTS, false, sendTS);
            HttpEntity<String> sendEntity = payLoadData.getPayLoad(startData);
            Object kafkaResponse = restTemplate.postForObject(startInvokeURL, sendEntity, String.class);
            System.out.println("startData:"+sendTS+"  "+kafkaResponse.toString());

            //post data and analyse the response to get the invokeId
            Object object = restTemplate.postForObject(postUrl, entity, String.class);
            System.out.println(object.toString());

            //record the receive time
            recvTS = formatter.format(new Date(System.currentTimeMillis()));

            //analyse the response data
            String result = JSONObject.fromObject(object).getString("result");
            status = JSONObject.fromObject(result).getString("status");
            //invoke success
            if (status.equals("OK")){
                invokeId = JSONObject.fromObject(result).getString("message");
                success = true;
            }
            //invoke failure
            else {
                 invokeId = "fail";
                 success = false;
            }

            //record the end data
            endData = jsonData.getTransData(postUrl, invokeId, 1, recvTS, success, sendTS);
            HttpEntity<String> recvEntity = payLoadData.getPayLoad(endData);
            kafkaResponse = restTemplate.postForObject(endInvokeURL, recvEntity, String.class);
            System.out.println("kafkaResponse:" + kafkaResponse.toString());

            //connDB.insert(postUrl, CCID, invokeId, "1995-04-23 00:00:00");
            return "invoke finish";
        }
    }

    /*@RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String test(@RequestBody TestUtils testUtils) {
        //get a chaincodeID by initing the chaincode
        this.postUrl = testUtils.getTestUrl();
        System.out.println("testFrq = [" + testUtils.getTestFrq() + "]");
        Users users = new Users("888", "888", "888", "888", "888");
        String testCCID = init(users);
        if (testCCID.equals("fail")) {
            return "init fail";
        } else {
            //test the chaincode
            String result = postService.sendPost(testUtils, testCCID);
            return result;
        }
    }*/

    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String test (@RequestBody TestUtils testUtils){
        this.postUrl = testUtils.getTestUrl();
        Users users = new Users("888", "888", "888", "888", "888");
        init(users);
        /*this.CCID = testUtils.getChaincodeId();
        System.out.println(testUtils.getChaincodeId());*/
        this.postUrl = testUtils.getTestUrl();
        System.out.println(testUtils.getTestUrl());
        int testFrq = testUtils.getTestFrq();
        if(testFrq >= 1000 || testFrq == 0){
            return "test fail";
        }else {
            int interval = 1000/testFrq;
            int fstLetter = (int)'a';
            for(int i=0; i<testFrq; i++){
                String invokeA = (char)(fstLetter + i%5) + "";
                String invokeB = "";
                if(invokeA .equals("e")){
                    invokeB = "a";
                }else {
                    invokeB = (char)(fstLetter + 1 + i%5) + "";
                }
                users.setInvokeA(invokeA);
                users.setInvokeB(invokeB);
                users.setAmount("5");
                System.out.println("a:"+invokeA+"  b:"+invokeB);
                try {
                    invoke(users);
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //end the test
            Object endTag = restTemplate.getForObject(endTestURL, String.class);
            /*List<Map<String, Object>> list = connDB.queryInitData();
            for(Map<String, Object> map : list){
                String getUrl = this.getUrl + map.get("invoke_id").toString();
                Object object = restTemplate.getForObject(getUrl, String.class);
                String timestamp = JSONObject.fromObject(object).getString("timestamp");
                String seconds = JSONObject.fromObject(timestamp).getString("seconds");
                String date = tranTimestamp.stampToDate(seconds);
                System.out.println(date);
                connDB.updateTime(map.get("invoke_id").toString(),date);

//                System.out.println(seconds);
            }*/

            return interval+"";
        }
    }

}
