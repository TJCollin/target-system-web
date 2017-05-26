package cn.collin.service;


import cn.collin.entity.TestUtils;
import cn.collin.utils.JsonData;
import cn.collin.utils.PayLoadData;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by collin on 17-5-3.
 */
public class PostService {
    JsonData jsonData = new JsonData();

    @Autowired
    RestTemplate restTemplate;

    private PayLoadData payLoadData;

    //kafka URL
    private String kafkaURL = "";

    //chaincode serverURL
    private String url = "";
    //test frequency
    private int testFrq = 0;

    //payload to post
    private String payLoad = "localhost:8080/hello";
    private String args = "";//args in payload
    private String chaincodeID = "";//chaincodeID in payload

    private OutputStreamWriter out = null;
    private BufferedReader reader = null;
    private String response="";

    //sendTS --- send timestamp
    //recvTS --- receive timestamp
    String sendTS = "";
    String recvTS = "";

    int interval = 0;
    int fstLetter = (int)'a';

    SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss|SSS");

    //post request
    /*public  String sendPost(TestUtils testUtils, String CCID) {
        this.url = testUtils.getTestUrl();
        this.testFrq = testUtils.getTestFrq();
        System.out.println("testFrq = [" + testFrq + "], CCID = [" + CCID + "]");
        interval = 1000/testFrq;
        if(testFrq >= 1000 || testFrq == 0){

            //the post frequency < 1000
            return "test fail";
        } else {
            try {

                //connect the server
                URL httpUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("connection", "keep-alive");
                conn.setUseCaches(false);//no cache
                conn.setInstanceFollowRedirects(true);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                //post request
                out = new OutputStreamWriter(
                        conn.getOutputStream());
                for(int i=0; i<testFrq; i++) {
                    //a->b b->c c->d d->e e->a; invoke_value : 10
                    String invokeA = (char) (fstLetter + i % 5) + "";
                    String invokeB = "";
                    if(invokeA .equals("e")){
                        invokeB = "a";
                    }else {
                        invokeB = (char)(fstLetter + 1 + i%5) + "";
                    }
                    //form the data to post
                    args = "[" + "\"" + invokeA + "\"" + "," + "\"" + invokeB + "\"" + "," + "\"" + "10" + "\"" + "]";
                    chaincodeID = "\"name\":\"" + CCID + "\"";
                    payLoad = jsonData.getJsonData("invoke", chaincodeID, "invoke", args, "3");
                    System.out.println("payload:"+payLoad);

                    //post the data to chaincode server
                    out.write(payLoad);
                    out.flush();

                    //post the send time to kafka
                    //to complete
                    sendTS = formatter.format(new Date(System.currentTimeMillis()));
                    System.out.println("sendTime:"+sendTS);
                    *//*HttpEntity<String> entity = payLoadData.getPayLoad(sendTS);
                    Object object = restTemplate.postForObject(kafkaURL, entity, String.class);*//*

                    //record the response
                    reader = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String lines = "";
                    while ((lines = reader.readLine()) != null) {
                        lines = new String(lines.getBytes(), "utf-8");
                        response+=lines;
                    }

                    //see whether the response is null and record the receive time
                    if (response.equals(null)) {
                        response = "no response";
                    } else {

                        //status --- show whether the invoke succeed
                        //invokeId --- invokeId
                        recvTS = formatter.format(new Date(System.currentTimeMillis()));
                        System.out.println("receiveTime:"+recvTS);
                        String result = JSONObject.fromObject(response).getString("result");
                        String status = JSONObject.fromObject(result).getString("status");
                        String invokeId = JSONObject.fromObject(result).getString("message");
                        //post the receive and the result time to kafka
                        //to complete
                        System.out.println("status:"+status+"   invokeId:"+invokeId);
                    }


                    reader.close();
                    Thread.sleep(interval);
                }
                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //use finally to close the reader stream and out stream
            finally {
                try{
                    if(out!=null){
                        out.close();
                    }
                    if(reader!=null){
                        reader.close();
                    }
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
            }
            return response;
        }

    }*/
    public String sendPost(String url,String Params)throws IOException{
        OutputStreamWriter out = null;
        BufferedReader reader = null;
        String response="";
        try {
            URL httpUrl = null; //HTTP URL类 用这个类来创建连接
            //创建URL
            httpUrl = new URL(url);
            //建立连接
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("connection", "keep-alive");
            conn.setUseCaches(false);//设置不要缓存
            conn.setInstanceFollowRedirects(true);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            //POST请求
            out = new OutputStreamWriter(
                    conn.getOutputStream());
            out.write(Params);
            out.flush();
            //读取响应
            reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                response+=lines;
            }
            reader.close();
            // 断开连接
            conn.disconnect();

        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(reader!=null){
                    reader.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }

        return response;

    }

    public String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            /*for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }*/
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
}
