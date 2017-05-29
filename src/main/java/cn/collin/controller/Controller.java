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
import java.util.*;

/**
 * Created by Collin on 2017/4/14.
 */
@RestController
@RequestMapping("/chaincode")
public class Controller {
//    private PostService postService = new PostService();
    private JsonData jsonData = new JsonData();
    private PayLoadData payLoadData = new PayLoadData();
    private String postUrl = "https://e4297151a2854808930738f32de27ed4-vp0.us.blockchain.ibm.com:5002/chaincode";
    private String contractAD = "";
    private String CCID = "";
    private String invokeId = "";
    private String status = "";
    private JSONObject jsonObject = new JSONObject();
    static int i;

    //
    boolean success = false;

    //data send to kafka when the invoke starts and ends
    private String startData = "";
    private String endData = "";
    private String checkData = "";

    private HttpEntity<String> sendEntity;
    private HttpEntity<String> recvEntity;
    private HttpEntity<String> checkEntity;
    private HttpEntity<String> entity;
    private Timer timer;
    //response to vue
    JSONObject response = new JSONObject();

    //sendTS send timestamp
    //recvTS receive timestamp
    private Long sendTS, recvTS;
    //timestamp format
    private SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd-HH:mm:ss:SSS");
    
    
    private String invokeURL = "http://localhost:8080/invoke";
//    private String endInvokeURL = "http://localhost:8080/endInvoke";
    private String endTestURL = "http://localhost:8080/endTest";
    private String searchResultURL = "http://localhost:8080/searchResult";
    int testFrq;
    Users users = new Users();
    int fstLetter = (int)'a';
    boolean finish = false;


    @Autowired
    private ConnDB connDB;

    @Autowired
    private RestTemplate restTemplate;

    //set the chaincode
    @RequestMapping(value = "/set", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String set(@RequestBody Users users) {
        this.postUrl = users.getServerAD();
        this.contractAD = users.getContractAD();
        System.out.println("serverAD:"+this.postUrl);
        System.out.println("contractAD:"+this.contractAD);
        response.put("result","ok");
        return response.toString();
    }

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
        chaincodeID = " \"path\":\""+contractAD+"\" ";
        payLoad = jsonData.getJsonData("deploy", chaincodeID, "init", args, "1");
        entity = payLoadData.getPayLoad(payLoad);

        System.out.println("initPayload:"+payLoad);
        Object object = restTemplate.postForObject(postUrl+"/chaincode", entity, String.class);
//        System.out.println(object.toString());

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
            entity = payLoadData.getPayLoad(payLoad);
            Object object = restTemplate.postForObject(postUrl+"/chaincode", entity, String.class);

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
        finish = false;
        Object resp;
//        System.out.println("invokeUsers = [" + invokeUsers.getInvokeA() + "]");
        if (CCID.equals("fail") || CCID.equals("")) {
            System.out.println("query fail");
            return "query fail";
        } else {
            //form the json data for invoke
            String args = "[" + "\"" + invokeUsers.getInvokeA() + "\"" + "," + "\"" + invokeUsers.getInvokeB() + "\"" + "," + "\"" + invokeUsers.getAmount() + "\"" + "]";
            String chaincodeID = "\"name\":\"" + CCID + "\"";
            String payLoad = jsonData.getJsonData("invoke", chaincodeID, "invoke", args, "3");
//            System.out.println(payLoad.toString());

            //form the post entity
            entity = payLoadData.getPayLoad(payLoad);
//            System.out.println("invokeA:" + invokeUsers.getInvokeA() + "  invokeB:" + invokeUsers.getInvokeB());

            //record the start data
//            sendTS = formatter.format(new Date(System.currentTimeMillis()));
            sendTS = System.currentTimeMillis();
            startData = jsonData.getTransData(postUrl, CCID, "none", 0, sendTS, false, sendTS);
//            System.out.println("startData:" + startData);
            sendEntity = payLoadData.getPayLoad(startData);
            Object kafkaResponse = restTemplate.postForObject(invokeURL, sendEntity, String.class);
//            System.out.println("startData:"+sendTS+"  "+kafkaResponse.toString());

            //post data and analyse the response to get the invokeId
            Object object = restTemplate.postForObject(postUrl + "/chaincode", entity, String.class);
//            System.out.println(object.toString());

            //record the receive time
//            recvTS = formatter.format(new Date(System.currentTimeMillis()));
            recvTS = System.currentTimeMillis();
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
            endData = jsonData.getTransData(postUrl, CCID, invokeId, 1, recvTS, success, sendTS);
//            System.out.println("endData:" + endData);
            recvEntity = payLoadData.getPayLoad(endData);
            restTemplate.postForObject(invokeURL, recvEntity, String.class);
//            System.out.println("kafkaResponse:" + kafkaResponse.toString());

            connDB.insert(postUrl, CCID, invokeId, sendTS, recvTS);
            finish = true;
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
        i = 0;
        Users users = new Users("888", "888", "888", "888", "888");
        init(users);
        testFrq = testUtils.getTestFrq();
        String type = testUtils.getType();
        long interval = testUtils.getInterval();
//        timer = new Timer();
//        timer.schedule(new MyTask(), 200, interval*1000);


//            int interval = 1000/testFrq;
            int fstLetter = (int)'a';
            for(int i=0; i<testFrq; ++i){
//                System.out.println("i:"+i);
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
                invoke(users);
                while (finish && i<testFrq){
                    try {
                        Thread.sleep(interval*1000);
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//                System.out.println("a:"+invokeA+"  b:"+invokeB);
            }


            //end the test


            List<Map<String, Object>> list = connDB.queryInitData();
            for(Map<String, Object> map : list){
                String getUrl = this.postUrl + "/transactions/" + map.get("invoke_id").toString();
                /*try {

                } catch ()*/
                Object object = restTemplate.getForObject(getUrl, String.class);
                String timestamp = JSONObject.fromObject(object).getString("timestamp");
                String seconds = JSONObject.fromObject(timestamp).getString("seconds");
                String nanos = JSONObject.fromObject(timestamp).getString("nanos");
                String invokeStart = map.get("invoke_begin").toString();
                seconds = seconds + nanos.substring(0,3);
                connDB.updateTime(map.get("invoke_id").toString(),seconds);

                checkData = jsonData.getTransData(postUrl, CCID, invokeId, 2, Long.parseLong(seconds), true, Long.parseLong(invokeStart));
                checkEntity = payLoadData.getPayLoad(checkData);
                restTemplate.postForObject(searchResultURL, checkEntity, String.class);

//                System.out.println(seconds);
            }

            Object endTag = restTemplate.getForObject(endTestURL, String.class);
            if (endTag.toString().equals("ok")){
                System.out.println("endTag:"+endTag.toString());
            } else {
                System.out.println("fail");
            }

            response.put("result",endTag);

            return response.toString();

    }

   /* private class MyTask extends TimerTask {
        @Override
        public void run() {

            if (i< testFrq){
                i++;
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
                invoke(users);
            } else {
                timer.cancel();
                System.gc();
            }
        }
    }*/
}
