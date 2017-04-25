package cn.collin.controller;

import cn.collin.entity.TestUtils;
import cn.collin.entity.Users;
import cn.collin.service.ConnDB;
import cn.collin.utils.JsonData;
import cn.collin.utils.PayLoadData;
import cn.collin.utils.TransTimestamp;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Collin on 2017/4/14.
 */
@RestController
@RequestMapping("/chaincode")
public class Controller {
    JsonData jsonData = new JsonData();
    PayLoadData payLoadData = new PayLoadData();
    TransTimestamp tranTimestamp = new TransTimestamp();
    private String postUrl = "http://202.120.167.86:7050/chaincode";
    private String getUrl = "http://202.120.167.86:7050/transactions/";
    private String CCID = "";
    private String invokeId = "";


    @Autowired
    private ConnDB connDB;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/init", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String init (@RequestBody Users users) {
        String valA, valB, valC, valD, valE, args, chaincodeID, payLoad;
        JSONObject jsonObject = new JSONObject();
        ArrayList<String> arrayList = new ArrayList<>();
        valA = users.getValA();
        valB = users.getValB();
        valC = users.getValC();
        valD = users.getValD();
        valE = users.getValE();
        arrayList.add("\"a\"");
        arrayList.add("\""+ valA +"\"");
        arrayList.add(" \"b\" ");
        arrayList.add("\""+ valB +"\"");
        arrayList.add("\"c\"");
        arrayList.add("\""+ valC +"\"");
        arrayList.add("\"d\"");
        arrayList.add("\""+ valD +"\"");
        arrayList.add("\"e\"");
        arrayList.add("\""+ valE +"\"");
        args = arrayList.toString();
        System.out.println(args);
        chaincodeID = " \"path\":\"github.com/hyperledger/fabric/examples/chaincode/go/zhangcong\" ";
        payLoad = jsonData.getJsonData("deploy", chaincodeID, "init", args, "1");
        HttpEntity<String> entity = payLoadData.getPayLoad(payLoad);
        Object object = restTemplate.postForObject(postUrl, entity, String.class);
        jsonObject = JSONObject.fromObject(object);
        String result = JSONObject.fromObject(jsonObject.getString("result")).toString();
        String status = JSONObject.fromObject(result).getString("status");

        if(status.equals("OK")){
            CCID = JSONObject.fromObject(result).getString("message");
            System.out.println(CCID);
            return "deploy success";
        }else {
            CCID = "fail";
            return "deploy fail";
        }
    }

    @RequestMapping(value = "/singleQuery", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String singleQuery (@RequestBody Users user){
        if(CCID.equals("fail") || CCID.equals("")){
            return "query fail";
        }else {
            String args ="[\""+ user.getQueryUser() +"\"]";
            String chaincodeID = "\"name\":\""+CCID+"\"";
            String payLoad = jsonData.getJsonData("query", chaincodeID, "query", args, "5");
            System.out.println(payLoad.toString());
            HttpEntity<String> entity = payLoadData.getPayLoad(payLoad);
            Object object = restTemplate.postForObject(postUrl, entity, String.class);
            String result = JSONObject.fromObject(object).getString("result");
            String queryData = JSONObject.fromObject(result).getString("message");
            System.out.println(object.toString());
            return queryData;
        }
    }

    @RequestMapping(value = "/query", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public JSONArray query (){
        Users users = new Users();
        int fstLetter = (int)'a';
        String[] queryResult = new String[5];
        for(int i=0; i<5; i++){
            String queryUser = (char)(fstLetter + i%5) + "";
            users.setQueryUser(queryUser);
            queryResult[i] = singleQuery(users);
        }
        JSONArray jsonArray = JSONArray.fromObject(queryResult);
        return jsonArray;
    }

    @RequestMapping(value = "/invoke", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String invoke (@RequestBody Users invokeUsers){
        if(CCID.equals("fail") || CCID.equals("")){
            return "query fail";
        }else {
            String args ="["+"\""+ invokeUsers.getInvokeA() + "\""+","+"\""+ invokeUsers.getInvokeB() + "\""+","+"\""+ invokeUsers.getAmount() + "\""+"]";
            String chaincodeID = "\"name\":\""+CCID+"\"";
            String payLoad = jsonData.getJsonData("invoke", chaincodeID, "invoke", args, "3");
            System.out.println(payLoad.toString());
            HttpEntity<String> entity = payLoadData.getPayLoad(payLoad);
            System.out.println("invokeA:"+ invokeUsers.getInvokeA()+"  invokeB:"+invokeUsers.getInvokeB());
            Object object = restTemplate.postForObject(postUrl, entity, String.class);
            System.out.println(object.toString());
            String result = JSONObject.fromObject(object).getString("result");
            invokeId = JSONObject.fromObject(result).getString("message");
            connDB.insert(postUrl, CCID, invokeId, "1995-04-23 00:00:00");
            return "invoke success";
        }
    }

    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String test (@RequestBody TestUtils testUtils){
        Users users = new Users();
        this.CCID = testUtils.getChaincodeId();
        System.out.println(testUtils.getChaincodeId());
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
            List<Map<String, Object>> list = connDB.queryInitData();
            for(Map<String, Object> map : list){
                String getUrl = this.getUrl + map.get("invoke_id").toString();
                Object object = restTemplate.getForObject(getUrl, String.class);
                String timestamp = JSONObject.fromObject(object).getString("timestamp");
                String seconds = JSONObject.fromObject(timestamp).getString("seconds");
                String date = tranTimestamp.stampToDate(seconds);
                System.out.println(date);
                connDB.updateTime(map.get("invoke_id").toString(),date);

//                System.out.println(seconds);
            }

            return interval+"";
        }
    }

}
